/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.sql.ast.from;

import org.hibernate.sql.ast.expression.ColumnReference;

/**
 * Represents an individual part of a TableSpace.  Might be a {@link PhysicalTable}
 * or a {@link DerivedTable} (in-line view).
 *
 * @author Steve Ebersole
 */
public interface Table {
	String getTableExpression();
	String getCorrelationName();

	ColumnReference getColumnReference(String name);
}
