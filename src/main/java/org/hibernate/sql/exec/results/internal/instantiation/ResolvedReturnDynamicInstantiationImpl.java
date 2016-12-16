/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.exec.results.internal.instantiation;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.internal.util.StringHelper;
import org.hibernate.sql.ast.select.SqlSelection;
import org.hibernate.sql.exec.results.process.spi2.ReturnAssembler;
import org.hibernate.sql.exec.results.spi.ResolvedReturn;
import org.hibernate.sql.exec.results.spi.ResolvedReturnDynamicInstantiation;
import org.hibernate.sqm.query.expression.Compatibility;

import org.jboss.logging.Logger;

/**
 * @author Steve Ebersole
 */
public class ResolvedReturnDynamicInstantiationImpl implements ResolvedReturnDynamicInstantiation {
	private static final Logger log = Logger.getLogger( ResolvedReturnDynamicInstantiationImpl.class );

	private final Class target;

	private List<SqlSelection> sqlSelections = new ArrayList<>();

	private ReturnAssembler assembler;

	public ResolvedReturnDynamicInstantiationImpl(Class target) {
		this.target = target;

		if ( target == null ) {
			throw new IllegalArgumentException( "Passed `target` cannot be null" );
		}
	}

	@Override
	public Class getInstantiationTarget() {
		return target;
	}

	@Override
	public void setArguments(List<ResolvedArgument> arguments) {
		boolean areAllArgumentsAliased = true;
		boolean areAnyArgumentsAliased = false;
		final Set<String> aliases = new HashSet<>();
		final List<String> duplicatedAliases = new ArrayList<>();

		List<ArgumentReader> argumentReaders = new ArrayList<>();
		int numberOfColumnsConsumedSoFar = 0;

		for ( ResolvedArgument argument : arguments ) {
			sqlSelections.addAll( argument.getSqlSelectionDescriptors() );

			if ( argument.getAlias() == null ) {
				areAllArgumentsAliased = false;
			}
			else {
				if ( ! aliases.add( argument.getAlias() ) ) {
					duplicatedAliases.add( argument.getAlias() );
				}
				areAnyArgumentsAliased = true;
			}

			argumentReaders.add(
					new ArgumentReader( argument.getResolvedArgument().getReturnAssembler(), argument.getAlias() )
			);
		}

		assembler = resolveAssembler(
				target,
				arguments,
				areAllArgumentsAliased,
				areAnyArgumentsAliased,
				duplicatedAliases,
				argumentReaders
		);
	}

	private static ReturnAssembler resolveAssembler(
			Class target,
			List<ResolvedArgument> arguments,
			boolean areAllArgumentsAliased,
			boolean areAnyArgumentsAliased,
			List<String> duplicatedAliases,
			List<ArgumentReader> argumentReaders) {
		if ( List.class.equals( target ) ) {
			if ( log.isDebugEnabled() && areAnyArgumentsAliased ) {
				log.debug( "One or more arguments for List dynamic instantiation (`new list(...)`) specified an alias; ignoring" );
			}
			return new ReturnAssemblerListImpl( argumentReaders );
		}
		else if ( Map.class.equals( target ) ) {
			if ( ! areAllArgumentsAliased ) {
				throw new IllegalStateException( "Map dynamic instantiation contained one or more arguments with no alias" );
			}
			if ( !duplicatedAliases.isEmpty() ) {
				throw new IllegalStateException(
						"Map dynamic instantiation contained arguments with duplicated aliases [" + StringHelper.join( ",", duplicatedAliases ) + "]"
				);
			}
			return new ReturnAssemblerMapImpl( argumentReaders );
		}
		else {
			// find a constructor matching argument types
			constructor_loop:
			for ( Constructor constructor : target.getDeclaredConstructors() ) {
				if ( constructor.getParameterTypes().length != arguments.size() ) {
					continue;
				}

				for ( int i = 0; i < arguments.size(); i++ ) {
					final ArgumentReader argumentReader = argumentReaders.get( i );
					// todo : move Compatibility from SQM into ORM?  It is only used here
					final boolean assignmentCompatible = Compatibility.areAssignmentCompatible(
							constructor.getParameterTypes()[i],
							argumentReader.getReturnedJavaType()
					);
					if ( !assignmentCompatible ) {
						log.debugf(
								"Skipping constructor for dynamic-instantiation match due to argument mismatch [%s] : %s -> %s",
								i,
								constructor.getParameterTypes()[i],
								argumentReader.getReturnedJavaType()
						);
						continue constructor_loop;
					}
				}

				constructor.setAccessible( true );
				return new ReturnAssemblerConstructorImpl( constructor, argumentReaders );
			}

			log.debugf(
					"Could not locate appropriate constructor for dynamic instantiation of [%s]; attempting bean-injection instantiation",
					target.getName()
			);


			if ( ! areAllArgumentsAliased ) {
				throw new IllegalStateException( "Bean-injection dynamic instantiation contained one or more arguments with no alias" );
			}
			if ( !duplicatedAliases.isEmpty() ) {
				throw new IllegalStateException(
						"Bean-injection dynamic instantiation contained arguments with duplicated aliases [" + StringHelper.join( ",", duplicatedAliases ) + "]"
				);
			}

			return new ReturnAssemblerInjectionImpl( target, argumentReaders );
		}
	}


	@Override
	public int getNumberOfSelectablesConsumed() {
		return getSqlSelections().size();
	}

	public List<SqlSelection> getSqlSelections() {
		return sqlSelections;
	}

	@Override
	public ReturnAssembler getReturnAssembler() {
		return assembler;
	}

	@Override
	public Class getReturnedJavaType() {
		return assembler.getReturnedJavaType();
	}

	public static class ResolvedArgumentImpl implements ResolvedArgument {
		// sucks that this needs to be a Return/ResolvedReturn
		// 		todo : contemplate ways to avoid that
		//		already tried ResolvedExpression, but that leads to strange 3x/4x dispatch:
		//			Interpreter -> Expression#accept -> Interpreter -> Expression#resolve

		private final ResolvedReturn resolvedArgument;
		private final String alias;

		public ResolvedArgumentImpl(ResolvedReturn resolvedArgument, String alias) {
			this.resolvedArgument = resolvedArgument;
			this.alias = alias;
		}

		@Override
		public ResolvedReturn getResolvedArgument() {
			return resolvedArgument;
		}

		@Override
		public String getAlias() {
			return alias;
		}

		@Override
		public List<SqlSelection> getSqlSelectionDescriptors() {
			return resolvedArgument.getSqlSelections();
		}
	}
}
