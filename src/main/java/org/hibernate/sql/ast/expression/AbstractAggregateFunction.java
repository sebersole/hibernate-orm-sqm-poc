/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.ast.expression;

import org.hibernate.sql.ast.select.SqlSelectable;
import org.hibernate.sql.convert.results.internal.ReturnScalarImpl;
import org.hibernate.sql.convert.results.spi.Return;
import org.hibernate.sql.exec.results.process.internal.SqlSelectionReaderImpl;
import org.hibernate.sql.exec.results.process.spi2.SqlSelectionReader;
import org.hibernate.type.BasicType;

/**
 * @author Steve Ebersole
 */
public abstract class AbstractAggregateFunction
		implements AggregateFunction, SqlSelectable {
	private final Expression argument;
	private final boolean distinct;
	private final BasicType resultType;

	public AbstractAggregateFunction(Expression argument, boolean distinct, BasicType resultType) {
		this.argument = argument;
		this.distinct = distinct;
		this.resultType = resultType;
	}

	@Override
	public Expression getArgument() {
		return argument;
	}

	@Override
	public Return toQueryReturn(String resultVariable) {
		return new ReturnScalarImpl( this, getType(), resultVariable );
	}

	@Override
	public boolean isDistinct() {
		return distinct;
	}

	@Override
	public BasicType getType() {
		return resultType;
	}

	@Override
	public SqlSelectionReader getSqlSelectionReader() {
		return new SqlSelectionReaderImpl( getType() );
	}
}
