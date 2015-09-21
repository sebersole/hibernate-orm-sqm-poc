/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.sql.ast.expression;

import org.hibernate.sql.ast.from.TableSpecification;

/**
 * Models a reference to a column in the query.  Roughly equivalent to what ANSI SQL (92)
 * terms {@code <column reference>}
 *
 * @todo : would LOVE to see this relate back to the mapping notion of a Column (access to type, nullable, etc) - similar to AttributeDescriptor
 *
 * @author Steve Ebersole
 */
public interface ColumnReference extends Expression {
	TableSpecification getTable();
	String getName();
}
