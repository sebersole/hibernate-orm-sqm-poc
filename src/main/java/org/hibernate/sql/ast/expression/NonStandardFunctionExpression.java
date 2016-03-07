/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.ast.expression;

import java.util.List;

import org.hibernate.sql.orm.internal.sqm.model.BasicTypeImpl;
import org.hibernate.type.Type;

/**
 * Represents a call to a function other than one of the standardized ones.
 *
 * @author Steve Ebersole
 */
public class NonStandardFunctionExpression implements Expression {
	private final String functionName;
	private final List<Expression> arguments;
	private final BasicTypeImpl resultSqmType;

	public NonStandardFunctionExpression(
			String functionName,
			List<Expression> arguments,
			BasicTypeImpl resultSqmType) {
		this.functionName = functionName;
		this.arguments = arguments;
		this.resultSqmType = resultSqmType;
	}

	public String getFunctionName() {
		return functionName;
	}

	public List<Expression> getArguments() {
		return arguments;
	}

	public BasicTypeImpl getResultSqmType() {
		return resultSqmType;
	}

	@Override
	public Type getType() {
		return resultSqmType.getOrmType();
	}
}
