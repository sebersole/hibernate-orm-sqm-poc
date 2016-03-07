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
public abstract class AbstractAggregateFunction implements AggregateFunction {
	private final Expression argument;
	private final boolean distinct;
	private final BasicTypeImpl resultSqmType;

	public AbstractAggregateFunction(Expression argument, boolean distinct, BasicTypeImpl resultSqmType) {
		this.argument = argument;
		this.distinct = distinct;
		this.resultSqmType = resultSqmType;
	}

	@Override
	public Expression getArgument() {
		return argument;
	}

	@Override
	public boolean isDistinct() {
		return distinct;
	}

	@Override
	public Type getType() {
		return resultSqmType.getOrmType();
	}
}
