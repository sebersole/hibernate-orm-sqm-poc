/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.sql.ast.from;

/**
 * Represents a TableSpecification derived from a subquery (inline view), as opposed to a PhysicalTableSpecification
 *
 * @author Steve Ebersole
 */
public class DerivedTableSpecification extends AbstractTableSpecification implements TableSpecification {
	private final String query;

	public DerivedTableSpecification(String query, String alias) {
		super( alias );
		this.query = query;
	}

	public String getQuery() {
		return query;
	}

	@Override
	public String getTableExpression() {
		return getQuery();
	}
}
