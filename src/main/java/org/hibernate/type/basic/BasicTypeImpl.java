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
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.type.ImprovedBasicType;
import org.hibernate.type.descriptor.java.JavaTypeDescriptor;
import org.hibernate.type.descriptor.sql.SqlTypeDescriptor;

/**
 * @author Steve Ebersole
 */
public class BasicTypeImpl<T,D> implements ImprovedBasicType<T> {
	private final SqlTypeDescriptor sqlType;
	private final JavaTypeDescriptor<T> domainJavaType;

	private final AttributeConverter<T,D> converter;
	private final JavaTypeDescriptor intermediateJavaType;

	/**
	 * Constructor form for building a basic type without an AttributeConverter
	 *
	 * @param domainJavaType The descriptor for the domain model Java type.
	 * @param sqlType The descriptor for the JDBC type.
	 */
	public BasicTypeImpl(JavaTypeDescriptor<T> domainJavaType, SqlTypeDescriptor sqlType) {
		this( domainJavaType, sqlType, null, null );
	}

	/**
	 * Constructor form for building a basic type with an AttributeConverter.
	 * <p/>
	 * Notice that 2 different JavaTypeDescriptor instances are passed in here.  {@code domainJavaType} represents
	 * the Java type in the user's domain model.  {@code intermediateJavaType} represens the Java type expressed
	 * by the AttributeConverter as the "database type".  We will read the database value initially using the
	 * {@code sqlType} + {@code intermediateJavaType}.  We then pass that value along to the AttributeConverter
	 * to convert to the domain Java type.
	 *
	 * @param domainJavaType The descriptor for the domain model Java type.
	 * @param sqlType The descriptor for the JDBC type.
	 */
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
