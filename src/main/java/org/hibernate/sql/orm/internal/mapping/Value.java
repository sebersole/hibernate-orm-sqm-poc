/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.orm.internal.mapping;

/**
 * Represents the commonality between {@link Column} and {@link Formula}
 *
 * @author Steve Ebersole
 */
public interface Value {
	TableReference getSourceTable();
	// todo : SqlTypeDescriptor would be better, along with nullable, etc information
	int getJdbcType();
	String toLoggableString();
}
