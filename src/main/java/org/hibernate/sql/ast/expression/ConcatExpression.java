/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.ast.expression;

import org.hibernate.sql.ast.select.SqlSelectable;
import org.hibernate.sql.convert.results.internal.ReturnScalarImpl;
import org.hibernate.sql.exec.results.process.internal.SqlSelectionReaderImpl;
import org.hibernate.sql.exec.results.process.spi2.SqlSelectionReader;
import org.hibernate.sql.exec.spi.SqlAstSelectInterpreter;
import org.hibernate.type.BasicType;

/**
 * @author Steve Ebersole
 */
public class ConcatExpression implements Expression, SqlSelectable {
	private final Expression lhsOperand;
	private final Expression rhsOperand;
	private final BasicType type;

	public ConcatExpression(Expression lhsOperand, Expression rhsOperand) {
		this( lhsOperand, rhsOperand, (BasicType) lhsOperand.getType() );
	}

	public ConcatExpression(Expression lhsOperand, Expression rhsOperand, BasicType type) {
		this.lhsOperand = lhsOperand;
		this.rhsOperand = rhsOperand;
		this.type = type;
	}

	public Expression getLeftHandOperand() {
		return lhsOperand;
	}

	public Expression getRightHandOperand() {
		return rhsOperand;
	}

	@Override
	public BasicType getType() {
		return type;
	}

	@Override
	public void accept(SqlAstSelectInterpreter walker, boolean shallow) {
		walker.visitConcatExpression( this );
	}

	@Override
	public org.hibernate.sql.convert.results.spi.Return toQueryReturn(String resultVariable) {
		return new ReturnScalarImpl( this, getType(), resultVariable );
	}

	@Override
	public SqlSelectionReader getSqlSelectionReader() {
		return new SqlSelectionReaderImpl( getType() );
	}
}
