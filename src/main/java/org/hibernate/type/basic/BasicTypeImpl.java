/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.type.basic;

import java.sql.ResultSet;
import java.sql.SQLException;
import javax.persistence.AttributeConverter;

import org.hibernate.HibernateException;
import org.hibernate.boot.spi.AttributeConverterDescriptor;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.type.ImprovedBasicType;
import org.hibernate.type.descriptor.java.JavaTypeDescriptor;
import org.hibernate.type.descriptor.java.JavaTypeDescriptorRegistry;
import org.hibernate.type.descriptor.sql.SqlTypeDescriptor;

/**
 * @author Steve Ebersole
 */
public class BasicTypeImpl<T,D> implements ImprovedBasicType<T> {
	@SuppressWarnings("unchecked")
	public static BasicTypeImpl from(
			JavaTypeDescriptor domainJavaType,
			SqlTypeDescriptor sqlType,
			AttributeConverterDescriptor converterDescriptor) {
		if ( converterDescriptor == null ) {
			return new BasicTypeImpl( domainJavaType, sqlType );
		}
		else {
			final JavaTypeDescriptor intermediateJavaType = JavaTypeDescriptorRegistry.INSTANCE.getDescriptor( converterDescriptor.getJdbcType() );
			return new BasicTypeImpl( domainJavaType, sqlType, converterDescriptor.getAttributeConverter(), intermediateJavaType );
		}
	}

	private final SqlTypeDescriptor sqlType;
	private final JavaTypeDescriptor<T> domainJavaType;

	private final AttributeConverter<T,D> converter;
	private final JavaTypeDescriptor intermediateJavaType;

	public BasicTypeImpl(JavaTypeDescriptor<T> domainJavaType, SqlTypeDescriptor sqlType) {
		this( domainJavaType, sqlType, null, null );
	}

	public BasicTypeImpl(
			JavaTypeDescriptor<T> domainJavaType,
			SqlTypeDescriptor sqlType,
			AttributeConverter<T,D> attributeConverter,
			JavaTypeDescriptor intermediateJavaType) {
		this.domainJavaType = domainJavaType;
		this.sqlType = sqlType;
		this.converter = attributeConverter;
		this.intermediateJavaType = intermediateJavaType;
	}

	@Override
	public String getTypeName() {
		return null;
	}

	@Override
	public JavaTypeDescriptor<T> getJavaTypeDescriptor() {
		return domainJavaType;
	}

	@Override
	public SqlTypeDescriptor getSqlTypeDescriptor() {
		return sqlType;
	}

	public AttributeConverter<T, D> getAttributeConverter() {
		return converter;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Object hydrate(
			ResultSet rs,
			String[] names,
			SessionImplementor session,
			Object owner) throws HibernateException, SQLException {
		final D databaseValue = (D) getSqlTypeDescriptor().getExtractor( intermediateJavaType ).extract(
				rs,
				names[0],
				null
		);

		return converter.convertToEntityAttribute( databaseValue );
	}
}
