/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.ast.expression;


import org.hibernate.sql.gen.Return;
import org.hibernate.sql.gen.SqlTreeWalker;
import org.hibernate.type.BasicType;
import org.hibernate.type.Type;

/**
 * @author Steve Ebersole
 */
public class CountStarFunction extends AbstractAggregateFunction {
	public CountStarFunction(boolean distinct, BasicType resultType) {
		super( STAR, distinct, resultType );
	}

	private static Expression STAR = new Expression() {
		@Override
		public Type getType() {
			return null;
		}

		@Override
		public Return getReturn() {
			throw new UnsupportedOperationException(  );
		}

		@Override
		public void accept(SqlTreeWalker sqlTreeWalker) {
			throw new UnsupportedOperationException(  );
		}
	};

	@Override
	public void accept(SqlTreeWalker sqlTreeWalker) {
		sqlTreeWalker.visitCountStarFunction( this );
	}
}
