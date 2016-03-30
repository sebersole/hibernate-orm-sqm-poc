/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.type.basic;

import java.util.HashMap;
import java.util.Map;

import org.hibernate.boot.spi.AttributeConverterDescriptor;
import org.hibernate.type.ImprovedBasicType;
import org.hibernate.type.descriptor.java.JavaTypeDescriptor;
import org.hibernate.type.descriptor.sql.SqlTypeDescriptor;

/**
 * Redesign of {@link org.hibernate.type.BasicTypeRegistry} based on idea of "composing"
 * a BasicType from JavaTypeDescriptor, SqlTypeDescriptor and AttributeConverter.
 *
 * @author Steve Ebersole
 */
public class BasicTypeFactory {
	private final Map<RegistryKey,ImprovedBasicType> registry = new HashMap<>();

	@SuppressWarnings("unchecked")
	public <T> ImprovedBasicType<T> resolveBasicType(
			JavaTypeDescriptor<T> javaTypeDescriptor,
			SqlTypeDescriptor sqlTypeDescriptor) {
		final RegistryKey key = new RegistryKey(
				javaTypeDescriptor.getJavaTypeClass(),
				sqlTypeDescriptor.getSqlType(),
				null
		);

		ImprovedBasicType impl = registry.get( key );
		if ( impl == null ) {
			impl = new BasicTypeImpl( javaTypeDescriptor, sqlTypeDescriptor );
			registry.put( key, impl );
		}
		return impl;
	}

	@SuppressWarnings("unchecked")
	public <T> ImprovedBasicType<T> resolveBasicType(
			JavaTypeDescriptor<T> javaTypeDescriptor,
			SqlTypeDescriptor sqlTypeDescriptor,
			AttributeConverterDescriptor converterDescriptor) {
		final RegistryKey key = RegistryKey.from( javaTypeDescriptor, sqlTypeDescriptor, converterDescriptor );

		ImprovedBasicType impl = registry.get( key );
		if ( impl == null ) {
			impl = BasicTypeImpl.from( javaTypeDescriptor, sqlTypeDescriptor, converterDescriptor );
			registry.put( key, impl );
		}
		return impl;
	}

	private static class RegistryKey {
		private final Class javaTypeClass;
		private final int jdbcCode;
		private final Class attributeConverterClass;

		public static RegistryKey from(
				JavaTypeDescriptor javaTypeDescriptor,
				SqlTypeDescriptor sqlTypeDescriptor,
				AttributeConverterDescriptor converterDescriptor) {
			return new RegistryKey(
					javaTypeDescriptor.getJavaTypeClass(),
					sqlTypeDescriptor.getSqlType(),
					converterDescriptor == null ? null : converterDescriptor.getAttributeConverter().getClass()
			);
		}

		private RegistryKey(Class javaTypeClass, int jdbcCode, Class attributeConverterClass) {
			assert javaTypeClass != null;

			this.javaTypeClass = javaTypeClass;
			this.jdbcCode = jdbcCode;
			this.attributeConverterClass = attributeConverterClass;
		}

		@Override
		public boolean equals(Object o) {
			if ( this == o ) {
				return true;
			}
			if ( !( o instanceof RegistryKey ) ) {
				return false;
			}

			final RegistryKey that = (RegistryKey) o;
			return jdbcCode == that.jdbcCode
					&& javaTypeClass.equals( that.javaTypeClass )
					&& sameConversion( attributeConverterClass, that.attributeConverterClass );
		}

		private boolean sameConversion(Class mine, Class yours) {
			if ( mine == null ) {
				return yours == null;
			}
			else {
				return mine.equals( yours );
			}
		}

		@Override
		public int hashCode() {
			int result = javaTypeClass.hashCode();
			result = 31 * result + jdbcCode;
			result = 31 * result + ( attributeConverterClass != null ? attributeConverterClass.hashCode() : 0 );
			return result;
		}
	}

}
