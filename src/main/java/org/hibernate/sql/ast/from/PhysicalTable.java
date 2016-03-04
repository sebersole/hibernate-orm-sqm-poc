/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.sql.ast.from;

/**
 * Represents a TableSpecification naming a physical table (or view)m, as opposed to a DerivedTableSpecification
 *
 * @author Steve Ebersole
 */
public class PhysicalTable extends AbstractTable implements Table {
	private final String tableName;

	public PhysicalTable(String tableName, String alias) {
		super( alias );
		this.tableName = tableName;
	}

	public String getTableName() {
		return tableName;
	}

	@Override
	public String getTableExpression() {
		return getTableName();
	}
}
