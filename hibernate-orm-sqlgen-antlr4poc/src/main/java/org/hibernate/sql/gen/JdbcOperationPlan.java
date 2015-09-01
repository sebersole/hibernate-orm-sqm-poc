/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.sql.gen;

import java.util.List;

/**
 * Represents the information needed to perform a JDBC operation.
 *
 * @author Steve Ebersole
 * @author John O'Hara
 */
public interface JdbcOperationPlan {
	/**
	 * The SQL to be performed.
	 *
	 * @return The SQL.
	 */
	String getSql();

	List<ParameterBinder> getParameterBinders();
}
