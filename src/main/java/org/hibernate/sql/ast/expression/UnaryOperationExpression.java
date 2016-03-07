/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.ast.expression;

import org.hibernate.type.Type;

/**
 * @author Steve Ebersole
 */
public class UnaryOperationExpression implements Expression {
	public enum Operation {
		PLUS,
		MINUS
	}

	private final Operation operation;
	private final Expression operand;
	private final Type type;

	public UnaryOperationExpression(Operation operation, Expression operand, Type type) {
		this.operation = operation;
		this.operand = operand;
		this.type = type;
	}

	@Override
	public Type getType() {
		return type;
	}

	public Expression getOperand() {
		return operand;
	}

	public Operation getOperation() {
		return operation;
	}
}
