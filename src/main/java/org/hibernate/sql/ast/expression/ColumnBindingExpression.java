/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.ast.expression;

import org.hibernate.sql.ast.from.ColumnBinding;
import org.hibernate.sql.gen.Return;
import org.hibernate.sql.gen.SqlTreeWalker;
import org.hibernate.type.Type;

/**
 * @author Steve Ebersole
 */
public class ColumnBindingExpression implements Expression {
	private final ColumnBinding columnBinding;

	public ColumnBindingExpression(ColumnBinding columnBinding) {
		this.columnBinding = columnBinding;
	}

	@Override
	public Type getType() {
		return null;
	}

	@Override
	public Return getReturn() {
		throw new UnsupportedOperationException( "ColumnBindingExpression cannot be used in select-clause" );
	}

	@Override
	public void accept(SqlTreeWalker sqlTreeWalker) {
		sqlTreeWalker.visitColumnBindingExpression( this );
	}

	public ColumnBinding getColumnBinding() {
		return columnBinding;
	}
}
