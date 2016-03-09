/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.sql.ast.expression;

import org.hibernate.sql.ast.from.ColumnBinding;
import org.hibernate.sql.gen.NotYetImplementedException;
import org.hibernate.sql.gen.Return;
import org.hibernate.sql.gen.SqlTreeWalker;
import org.hibernate.type.Type;

/**
 * @author Andrea Boriero
 */
public class EntityReference extends ExpressionAsReturnSupport {
	private final Type ormType;
	private final ColumnBinding[]  columnBindings;

	public EntityReference(Type ormType, ColumnBinding[] columnBindings) {
		this.ormType = ormType;
		this.columnBindings = columnBindings;
	}

	@Override
	public Type getType() {
		return ormType;
	}

	public ColumnBinding[] getColumnBindings() {
		return columnBindings;
	}

	@Override
	public void accept(SqlTreeWalker sqlTreeWalker) {
		sqlTreeWalker.visitEntityExpression( this );
	}
}
