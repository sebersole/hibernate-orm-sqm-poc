/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.type;

import java.sql.ResultSet;
import java.sql.SQLException;
import javax.persistence.AttributeConverter;

import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.engine.jdbc.Size;
import org.hibernate.engine.spi.Mapping;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.internal.WrapperOptionsImpl;
import org.hibernate.type.descriptor.java.JavaTypeDescriptor;
import org.hibernate.type.descriptor.sql.SqlTypeDescriptor;

/**
 * Redefines the Type contract in terms of "basic" or "value" types.  All Type methods are implemented
 * using delegation with the bundled SqlTypeDescriptor, JavaTypeDescriptor and AttributeConverter.
 *
 * @todo: Maybe a good candidate for Java 8 "default method" of the Type methods here to delegate to SqlTypeDescriptor, JavaTypeDescriptor and AttributeConverter
 *
 * @author Steve Ebersole
 */
public interface ImprovedBasicType<T> extends ImprovedType, org.hibernate.sqm.domain.BasicType<T> {
	@Override
	JavaTypeDescriptor<T> getJavaTypeDescriptor();

	/**
	 * The JDBC/SQL type descriptor.
	 *
	 * @return
	 */
	SqlTypeDescriptor getSqlTypeDescriptor();

	/**
	 * The converter applied to this type, if one.
	 *
	 * @return The applied converter.
	 */
	AttributeConverter<T,?> getAttributeConverter();


	@Override
	default Classification getClassification() {
		return Classification.BASIC;
	}

	@Override
	default String getName() {
		return getTypeName();
	}

	@Override
	default Class<T> getJavaType() {
		return getJavaTypeDescriptor().getJavaTypeClass();
	}

	@Override
	@SuppressWarnings("unchecked")
	default String toLoggableString(Object value, SessionFactoryImplementor factory) {
		return getJavaTypeDescriptor().extractLoggableRepresentation( (T) value );
	}

	@Override
	default ImprovedType getSemiResolvedType(SessionFactoryImplementor factory) {
		return this;
	}

	@Override
	default int getColumnSpan(Mapping mapping) throws MappingException {
		return 1;
	}

	@Override
	default int[] sqlTypes(Mapping mapping) throws MappingException {
		return new int[] {
				getSqlTypeDescriptor().getSqlType()
		};
	}

	Size[] NO_SIZES = new Size[0];

	@Override
	default Size[] dictatedSizes(Mapping mapping) throws MappingException {
		return NO_SIZES;
	}

	@Override
	default Size[] defaultSizes(Mapping mapping) throws MappingException {
		return NO_SIZES;
	}

	@Override
	default Object hydrate(
			ResultSet rs,
			String[] names,
			SessionImplementor session,
			Object owner) throws HibernateException, SQLException {
		return getSqlTypeDescriptor().getExtractor( getJavaTypeDescriptor() ).extract(
				rs,
				names[0],
				new WrapperOptionsImpl( session )
		);
	}

	@Override
	default Object resolve(Object value, SessionImplementor session, Object owner) {
		return value;
	}

	@Override
	default Object semiResolve(Object value, SessionImplementor session, Object owner) {
		return value;
	}
}
