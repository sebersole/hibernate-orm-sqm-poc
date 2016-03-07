/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.ast.expression;


import org.hibernate.sql.orm.internal.sqm.model.BasicTypeImpl;
import org.hibernate.type.Type;

/**
 * @author Steve Ebersole
 */
public class CountStarFunction extends AbstractAggregateFunction {
	public CountStarFunction(boolean distinct, BasicTypeImpl resultSqmType) {
		super( STAR, distinct, resultSqmType );
	}

	private static Expression STAR = new Expression() {
		@Override
		public Type getType() {
			return null;
		}
	};
}
