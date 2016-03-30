/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.type.basic;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.AttributeConverter;

import org.hibernate.boot.spi.AttributeConverterDescriptor;
import org.hibernate.type.ImprovedBasicType;
import org.hibernate.type.descriptor.java.JavaTypeDescriptor;
import org.hibernate.type.descriptor.java.JavaTypeDescriptorRegistry;
import org.hibernate.type.descriptor.java.JdbcRecommendedSqlTypeMappingContext;
import org.hibernate.type.descriptor.sql.SqlTypeDescriptor;

import com.fasterxml.classmate.ResolvedType;
import com.fasterxml.classmate.TypeResolver;

/**
 * Redesign of {@link org.hibernate.type.BasicTypeRegistry} based on idea of "composing"
 * a BasicType from JavaTypeDescriptor, SqlTypeDescriptor and AttributeConverter.
 *
 * @author Steve Ebersole
 */
public class BasicTypeFactory {
	private final TypeResolver classmateTypeResolver = new TypeResolver();
	private final Map<RegistryKey,ImprovedBasicType> registry = new HashMap<>();

	@SuppressWarnings("unchecked")
	public <T> ImprovedBasicType<T> resolveBasicType(
			JavaTypeDescriptor<T> javaTypeDescriptor,
			SqlTypeDescriptor sqlTypeDescriptor,
			JdbcRecommendedSqlTypeMappingContext jdbcTypeResolutionContext) {
		if ( javaTypeDescriptor == null ) {
			assert sqlTypeDescriptor != null;
			javaTypeDescriptor = sqlTypeDescriptor.getJdbcRecommendedJavaTypeMapping();
		}
		if ( sqlTypeDescriptor == null ) {
			assert javaTypeDescriptor != null;
			sqlTypeDescriptor = javaTypeDescriptor.getJdbcRecommendedSqlType( jdbcTypeResolutionContext );
		}

		final RegistryKey key = RegistryKey.from( javaTypeDescriptor, sqlTypeDescriptor, null );

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
			AttributeConverterDescriptor converterDescriptor,
			JdbcRecommendedSqlTypeMappingContext jdbcTypeResolutionContext) {
		if ( converterDescriptor == null ) {
			return resolveBasicType( javaTypeDescriptor, sqlTypeDescriptor, jdbcTypeResolutionContext );
		}
		else {
			return resolveBasicType(
					javaTypeDescriptor,
					sqlTypeDescriptor,
					converterDescriptor.getAttributeConverter(),
					converterDescriptor.getDomainType(),
					converterDescriptor.getJdbcType(),
					jdbcTypeResolutionContext
			);
		}
	}


	@SuppressWarnings("unchecked")
	private  <T> ImprovedBasicType<T> resolveBasicType(
			JavaTypeDescriptor<T> javaTypeDescriptor,
			SqlTypeDescriptor sqlTypeDescriptor,
			AttributeConverter converter,
			Class converterDefinedDomainType,
			Class converterDefinedJdbcType,
			JdbcRecommendedSqlTypeMappingContext jdbcTypeResolutionContext) {

		final JavaTypeDescriptor converterDefinedDomainTypeDescriptor = JavaTypeDescriptorRegistry.INSTANCE.getDescriptor( converterDefinedDomainType );
		final JavaTypeDescriptor converterDefinedJdbcTypeDescriptor = JavaTypeDescriptorRegistry.INSTANCE.getDescriptor( converterDefinedJdbcType );

		if ( javaTypeDescriptor == null ) {
			javaTypeDescriptor = converterDefinedDomainTypeDescriptor;
		}
		else {
			// todo : check that they match?
		}

		if ( sqlTypeDescriptor == null ) {
			sqlTypeDescriptor = converterDefinedJdbcTypeDescriptor.getJdbcRecommendedSqlType( jdbcTypeResolutionContext );
		}

		final RegistryKey key = RegistryKey.from( javaTypeDescriptor, sqlTypeDescriptor, converter );
		final ImprovedBasicType existing = registry.get( key );
		if ( existing != null ) {
			return existing;
		}

		final BasicTypeImpl impl = new BasicTypeImpl(
				javaTypeDescriptor,
				sqlTypeDescriptor,
				converter,
				converterDefinedJdbcTypeDescriptor
		);
		registry.put( key, impl );
		return impl;

	}

	@SuppressWarnings("unchecked")
	public <T> ImprovedBasicType<T> resolveBasicType(
			JavaTypeDescriptor<T> javaTypeDescriptor,
			SqlTypeDescriptor sqlTypeDescriptor,
			AttributeConverter converter,
			JdbcRecommendedSqlTypeMappingContext jdbcTypeResolutionContext) {
		if ( converter == null ) {
			return resolveBasicType( javaTypeDescriptor, sqlTypeDescriptor, jdbcTypeResolutionContext );
		}
		else {
			final ResolvedType resolvedConverterType = classmateTypeResolver.resolve( converter.getClass() );
			final List<ResolvedType> converterParams = resolvedConverterType.typeParametersFor( AttributeConverter.class );
			assert converterParams.size() == 2;

			return resolveBasicType(
					javaTypeDescriptor,
					sqlTypeDescriptor,
					converter,
					converterParams.get( 0 ).getErasedType(),
					converterParams.get( 1 ).getErasedType(),
					jdbcTypeResolutionContext
			);
		}
	}

}
