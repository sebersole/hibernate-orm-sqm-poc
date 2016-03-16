/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.ast.expression.instantiation;

import java.beans.BeanInfo;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.internal.util.ReflectHelper;
import org.hibernate.internal.util.beans.BeanInfoHelper;
import org.hibernate.internal.util.type.PrimitiveWrapperHelper;
import org.hibernate.sql.exec.results.spi.ResultSetProcessingOptions;
import org.hibernate.sql.exec.results.spi.ReturnReader;
import org.hibernate.sql.exec.results.spi.RowProcessingState;
import org.hibernate.sql.gen.NotYetImplementedException;

import org.jboss.logging.Logger;

/**
 * @author Steve Ebersole
 */
public class ReturnReaderDynamicInstantiationClassImpl<T> implements ReturnReader<T> {
	private static final Logger log = Logger.getLogger( ReturnReaderDynamicInstantiationClassImpl.class );

	private final Instantiator<T> instantiator;

	@SuppressWarnings("unchecked")
	public ReturnReaderDynamicInstantiationClassImpl(
			Class<T> target,
			boolean areAllArgumentsAliased,
			List<DynamicInstantiationArgument> arguments) {
		// todo : we could (i believe) make this determination up front based on Expression Java types
		// 		the one concern is that some Expressions (DynamicInstantiation itself is a great example) do
		// 		not define a Type return

		// find a constructor matching types
		constructor_loop: for ( Constructor constructor : target.getDeclaredConstructors() ) {
			if ( constructor.getParameterTypes().length != arguments.size() ) {
				continue;
			}

			for ( int i = 0; i < arguments.size(); i++ ) {
				final boolean assignmentCompatible = areAssignmentCompatible(
						constructor.getParameterTypes()[i],
						arguments.get( i ).getExpression().getReturnReader().getReturnedJavaType()
				);
				if ( !assignmentCompatible ) {
					log.debugf(
							"Skipping constructor for dynamic-instantiation match due to argument mismatch [%s] : %s -> %s",
							i,
							constructor.getParameterTypes()[i],
							arguments.get( i ).getExpression().getReturnReader().getReturnedJavaType()
					);
					continue constructor_loop;
				}
			}

			constructor.setAccessible( true );
			this.instantiator = new ConstructorInstantiator( constructor, arguments );
			return;
		}

		// if we get here we did not find a matching constructor.  See if we can use bean-injection...
		if ( !areAllArgumentsAliased ) {
			throw new InstantiationException(
					"Could not locate appropriate constructor for dynamic instantiation of class [" +
							target.getName() + "], and not all arguments were aliased so bean-injection could not be used"
			);
		}

		this.instantiator = new BeanInjectionInstantiator( target, arguments );
	}

	@SuppressWarnings("unchecked")
	private static boolean areAssignmentCompatible(Class memberType, Class expressionType) {
		if ( memberType.isAssignableFrom( expressionType ) ) {
			return true;
		}

		// we may have to look a little deeper
		if ( memberType.isPrimitive() && !expressionType.isPrimitive() ) {
			if ( PrimitiveWrapperHelper.isWrapper( expressionType ) ) {
				return memberType.isAssignableFrom(
						PrimitiveWrapperHelper.getDescriptorByWrapperType( expressionType ).getPrimitiveClass()
				);
			}
		}
		else if ( !memberType.isPrimitive() && expressionType.isPrimitive() ) {
			if ( PrimitiveWrapperHelper.isWrapper( memberType ) ) {
				return PrimitiveWrapperHelper.getDescriptorByWrapperType( memberType )
						.getPrimitiveClass()
						.isAssignableFrom( expressionType );
			}
		}

		return false;
	}

	@Override
	public void readBasicValues(
			RowProcessingState processingState,
			ResultSetProcessingOptions options) throws SQLException {
		throw new NotYetImplementedException();
	}

	@Override
	public void resolveBasicValues(
			RowProcessingState processingState,
			ResultSetProcessingOptions options) throws SQLException {
		throw new NotYetImplementedException();
	}

	@Override
	public T assemble(
			RowProcessingState processingState,
			ResultSetProcessingOptions options) throws SQLException {
		throw new NotYetImplementedException();
	}

	@Override
	public Class<T> getReturnedJavaType() {
		return instantiator.getInstantiatedJavaType();
	}

	@Override
	public T readResult(
			RowProcessingState processingState,
			ResultSetProcessingOptions options,
			int position,
			Object owner) throws SQLException {
		return instantiator.instantiate( processingState, options, position, owner );
	}

	@Override
	public int getNumberOfColumnsRead(RowProcessingState processingState) {
		return instantiator.getNumberOfColumnsRead( processingState );
	}

	private interface Instantiator<T> {
		Class<T> getInstantiatedJavaType();

		T instantiate(
				RowProcessingState processingState,
				ResultSetProcessingOptions options,
				int position,
				Object owner) throws SQLException;

		int getNumberOfColumnsRead(RowProcessingState processingState);
	}

	private static class ConstructorInstantiator<T> implements Instantiator<T> {
		private final Constructor<T> constructor;
		private final ReturnReader[] argumentReaders;

		ConstructorInstantiator(Constructor<T> constructor, List<DynamicInstantiationArgument> arguments) {
			this.constructor = constructor;
			this.argumentReaders = new ReturnReader[ arguments.size() ];
			for ( int i = 0; i < arguments.size(); i++ ) {
				argumentReaders[i] = arguments.get( i ).getExpression().getReturnReader();
			}
		}

		@Override
		public Class<T> getInstantiatedJavaType() {
			return constructor.getDeclaringClass();
		}

		@Override
		public T instantiate(
				RowProcessingState processingState,
				ResultSetProcessingOptions options,
				int startPosition,
				Object owner) throws SQLException {
			int position = startPosition;
			Object[] args = new Object[argumentReaders.length];
			for ( int i = 0; i < argumentReaders.length; i++ ) {
				args[i] = argumentReaders[i].readResult( processingState, options, position, owner );
				position += argumentReaders[i].getNumberOfColumnsRead( processingState );
			}

			try {
				return constructor.newInstance( args );
			}
			catch (InvocationTargetException e) {
				throw new InstantiationException( "Error performing dynamic instantiation : " + constructor.getDeclaringClass().getName(), e.getCause() );
			}
			catch (Exception e) {
				throw new InstantiationException( "Error performing dynamic instantiation : " + constructor.getDeclaringClass().getName(), e );
			}
		}

		@Override
		public int getNumberOfColumnsRead(RowProcessingState processingState) {
			int i = 0;
			for ( ReturnReader argumentReader : argumentReaders ) {
				i += argumentReader.getNumberOfColumnsRead( processingState );
			}
			return i;
		}
	}

	private static class BeanInjectionInstantiator<T> implements Instantiator<T> {
		private final Class<T> target;
		private final List<ReturnReader> readers;
		private final List<BeanInjector> beanInjectors;

		BeanInjectionInstantiator(final Class<T> target, final List<DynamicInstantiationArgument> arguments) {
			this.target = target;

			this.readers = new ArrayList<ReturnReader>();
			this.beanInjectors = new ArrayList<BeanInjector>();

			BeanInfoHelper.visitBeanInfo(
					target,
					new BeanInfoHelper.BeanInfoDelegate() {
						@Override
						public void processBeanInfo(BeanInfo beanInfo) throws Exception {
							// needs to be ordered by argument order!
							for ( DynamicInstantiationArgument argument : arguments ) {
								final ReturnReader argumentReader = argument.getExpression().getReturnReader();
								readers.add( argumentReader );

								if ( argument.getAlias() == null ) {
									throw new InstantiationException( "dynamic instantiation via bean injection requires alias for each argument" );
								}
								boolean found = false;
								for ( PropertyDescriptor propertyDescriptor : beanInfo.getPropertyDescriptors() ) {
									if ( argument.getAlias().equals( propertyDescriptor.getName() ) ) {
										if ( propertyDescriptor.getWriteMethod() != null ) {
											final boolean assignmentCompatible = areAssignmentCompatible(
													propertyDescriptor.getWriteMethod().getParameterTypes()[0],
													argumentReader.getReturnedJavaType()
											);
											if ( assignmentCompatible ) {
												propertyDescriptor.getWriteMethod().setAccessible( true );
												beanInjectors.add( new BeanInjectorSetter( propertyDescriptor.getWriteMethod() ) );
												found = true;
												break;
											}
										}
									}
								}
								if ( found ) {
									continue;
								}

								// see if we can find a Field with the given name...
								final Field field = findField( target, argument.getAlias(), argumentReader.getReturnedJavaType() );
								if ( field != null ) {
									beanInjectors.add( new BeanInjectorField( field ) );
								}
								else {
									throw new InstantiationException(
											"Unable to determine dynamic instantiation injection strategy for " +
													target.getName() + "#" + argument.getAlias()
									);
								}
							}
						}
					}
			);

			assert readers.size() == beanInjectors.size();
		}

		private Field findField(Class<T> declaringClass, String name, Class javaType) {
			try {
				Field field = declaringClass.getDeclaredField( name );
				// field should never be null
				if ( areAssignmentCompatible( field.getType(), javaType ) ) {
					field.setAccessible( true );
					return field;
				}
			}
			catch (NoSuchFieldException ignore) {
			}

			return null;
		}

		@Override
		public Class<T> getInstantiatedJavaType() {
			return target;
		}

		@Override
		public T instantiate(
				RowProcessingState processingState,
				ResultSetProcessingOptions options,
				int startPosition,
				Object owner) throws SQLException {
			try {
				final T result = target.newInstance();

				int position = startPosition;
				for ( int i = 0; i < readers.size(); i++ ) {
					beanInjectors.get( i ).inject(
							result,
							readers.get( i ).readResult( processingState, options, position, owner )
					);
					position += readers.get( i ).getNumberOfColumnsRead( processingState );
				}
				return result;
			}
			catch (Exception e) {
				throw new InstantiationException( "Error performing dynamic instantiation : " + target.getName(), e );
			}
		}

		@Override
		public int getNumberOfColumnsRead(RowProcessingState processingState) {
			return 0;
		}
	}

	private interface BeanInjector<T> {
		void inject(T target, Object value);
	}

	private static class BeanInjectorField<T> implements BeanInjector<T> {
		private final Field field;

		public BeanInjectorField(Field field) {
			this.field = field;
		}

		@Override
		public void inject(T target, Object value) {
			try {
				field.set( target, value );
			}
			catch (Exception e) {
				throw new InstantiationException( "Error performing the dynamic instantiation", e );
			}
		}
	}

	private static class BeanInjectorSetter<T> implements BeanInjector<T> {
		private final Method setter;

		public BeanInjectorSetter(Method setter) {
			this.setter = setter;
		}

		@Override
		public void inject(T target, Object value) {
			try {
				setter.invoke( target, value );
			}
			catch (InvocationTargetException e) {
				throw new InstantiationException( "Error performing the dynamic instantiation", e.getCause() );
			}
			catch (Exception e) {
				throw new InstantiationException( "Error performing the dynamic instantiation", e );
			}
		}
	}
}
