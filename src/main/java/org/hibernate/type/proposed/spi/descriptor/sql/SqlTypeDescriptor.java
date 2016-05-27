/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.type.proposed.spi.descriptor.sql;

import org.hibernate.type.descriptor.ValueBinder;
import org.hibernate.type.descriptor.ValueExtractor;

/**
 * @author Steve Ebersole
 */
public interface SqlTypeDescriptor {

	/**
	 * Return the {@linkplain java.sql.Types JDBC type-code} for the column mapped by this type.
	 *
	 * @return typeCode The JDBC type-code
	 */
	int getSqlType();

	/**
	 * Is this descriptor available for remapping?
	 *
	 * @return {@code true} indicates this descriptor can be remapped; otherwise, {@code false}
	 *
	 * @see org.hibernate.type.descriptor.WrapperOptions#remapSqlTypeDescriptor
	 * @see org.hibernate.dialect.Dialect#remapSqlTypeDescriptor
	 */
	boolean canBeRemapped();

	/**
	 * Get the JavaTypeDescriptor for the Java type recommended by the JDBC spec for mapping the
	 * given JDBC/SQL type.  The standard implementations honor the JDBC recommended mapping as per
	 * http://docs.oracle.com/javase/1.5.0/docs/guide/jdbc/getstart/mapping.html
	 *
	 * @return the recommended Java type descriptor.
	 */
	org.hibernate.type.proposed.spi.descriptor.java.JavaTypeDescriptor getJdbcRecommendedJavaTypeMapping();

	/**
	 * Get the binder (setting JDBC in-going parameter values) capable of handling values of the type described by the
	 * passed descriptor.
	 *
	 * @param javaTypeDescriptor The descriptor describing the types of Java values to be bound
	 *
	 * @return The appropriate binder.
	 */
	<X> ValueBinder<X> getBinder(org.hibernate.type.proposed.spi.descriptor.java.JavaTypeDescriptor<X> javaTypeDescriptor);

	/**
	 * Get the extractor (pulling out-going values from JDBC objects) capable of handling values of the type described
	 * by the passed descriptor.
	 *
	 * @param javaTypeDescriptor The descriptor describing the types of Java values to be extracted
	 *
	 * @return The appropriate extractor
	 */
	<X> ValueExtractor<X> getExtractor(org.hibernate.type.proposed.spi.descriptor.java.JavaTypeDescriptor<X> javaTypeDescriptor);
}
