/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.ast.expression;

import java.util.Arrays;
import java.util.List;

import org.hibernate.sql.ast.select.SqlSelectable;
import org.hibernate.sql.convert.results.internal.ReturnScalarImpl;
import org.hibernate.sql.exec.results.process.internal.SqlSelectionReaderImpl;
import org.hibernate.sql.exec.results.process.spi2.SqlSelectionReader;
import org.hibernate.sql.exec.spi.SqlAstSelectInterpreter;
import org.hibernate.type.BasicType;
import org.hibernate.type.Type;

/**
 * Represents a call to a function other than one of the standardized ones.
 *
 * @author Steve Ebersole
 */
public class NonStandardFunctionExpression implements Expression, SqlSelectable {
	private final String functionName;
	private final List<Expression> arguments;
	private final BasicType resultType;

	public NonStandardFunctionExpression(
			String functionName,
			List<Expression> arguments,
			BasicType resultType) {
		this.functionName = functionName;
		this.arguments = arguments;
		this.resultType = resultType;
	}

	public NonStandardFunctionExpression(
			String functionName,
			BasicType resultType,
			Expression... arguments) {
		this(
				functionName,
				Arrays.asList( arguments ),
				resultType
		);
	}

	public String getFunctionName() {
		return functionName;
	}

	public List<Expression> getArguments() {
		return arguments;
	}

	@Override
	public Type getType() {
		return resultType;
	}

	@Override
	public void accept(SqlAstSelectInterpreter walker, boolean shallow) {
		walker.visitNonStandardFunctionExpression( this );
	}

	@Override
	public org.hibernate.sql.convert.results.spi.Return toQueryReturn(String resultVariable) {
		return new ReturnScalarImpl( this, getType(), resultVariable );
	}

	@Override
	public SqlSelectionReader getSqlSelectionReader() {
		return new SqlSelectionReaderImpl( (BasicType) getType() );
	}
}
