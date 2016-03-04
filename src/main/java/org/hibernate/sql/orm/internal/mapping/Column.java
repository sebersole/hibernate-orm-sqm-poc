/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.orm.internal.mapping;

/**
 * @author Steve Ebersole
 */
public class Column implements Value {
	private final AbstractTable table;
	private final String name;
	private final int jdbcType;

	public Column(AbstractTable table, String name, int jdbcType) {
		this.table = table;
		this.name = name;
		this.jdbcType = jdbcType;
	}

	public String getName() {
		return name;
	}

	@Override
	public TableReference getSourceTable() {
		return table;
	}

	@Override
	public int getJdbcType() {
		return jdbcType;
	}

	@Override
	public String toLoggableString() {
		return null;
	}
}
