/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.sql.ast.from;

import org.hibernate.sql.ast.expression.ColumnReference;

/**
 * @author Steve Ebersole
 */
public class ColumnReferenceImpl implements ColumnReference {
	private final Table table;
	private final String name;

	public ColumnReferenceImpl(Table table, String name) {
		this.table = table;
		this.name = name;
	}

	@Override
	public Table getTable() {
		return table;
	}

	@Override
	public String getName() {
		return name;
	}
}
