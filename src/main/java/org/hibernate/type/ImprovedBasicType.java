/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.type;

import javax.persistence.AttributeConverter;

import org.hibernate.type.descriptor.java.JavaTypeDescriptor;
import org.hibernate.type.descriptor.sql.SqlTypeDescriptor;

/**
 * @author Steve Ebersole
 */
public interface ImprovedBasicType<T> extends ImprovedType, org.hibernate.sqm.domain.BasicType<T> {
	SqlTypeDescriptor getSqlTypeDescriptor();
	JavaTypeDescriptor<T> getJavaTypeDescriptor();
	AttributeConverter<T,?> getAttributeConverter();
}
