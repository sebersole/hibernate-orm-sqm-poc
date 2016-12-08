/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.exec.results.process.internal;

import org.hibernate.sql.ast.from.ColumnBinding;
import org.hibernate.sql.ast.select.SqlSelectable;
import org.hibernate.sql.ast.select.SqlSelectionDescriptor;

/**
 * @author Steve Ebersole
 */
public class SqlSelectionDescriptorImpl implements SqlSelectionDescriptor {
	private final SqlSelectable sqlSelectable;
	private final int position;

	public SqlSelectionDescriptorImpl(SqlSelectable sqlSelectable, int position) {
		this.sqlSelectable = sqlSelectable;
		this.position = position;

		if ( sqlSelectable instanceof ColumnBinding ) {

		}
	}

	@Override
	public SqlSelectable getSqlSelectable() {
		return sqlSelectable;
	}

	@Override
	public int getValuesArrayPosition() {
		return position;
	}
}
