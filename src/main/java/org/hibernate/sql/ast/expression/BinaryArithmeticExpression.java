/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.ast.expression;

import org.hibernate.type.BasicType;
import org.hibernate.type.Type;

/**
 * @author Steve Ebersole
 */
public class BinaryArithmeticExpression implements Expression {
	private final Operation operation;
	private final Expression lhsOperand;
	private final Expression rhsOperand;
	private final BasicType resultType;

	public BinaryArithmeticExpression(
			Operation operation,
			Expression lhsOperand,
			Expression rhsOperand,
			BasicType resultType) {
		this.operation = operation;
		this.lhsOperand = lhsOperand;
		this.rhsOperand = rhsOperand;
		this.resultType = resultType;
	}

	@Override
	public BasicType getType() {
		return resultType;
	}

	public enum Operation {
		ADD {
			@Override
			String apply(String lhs, String rhs) {
				return applyPrimitive( lhs, '+', rhs );
			}
		},
		SUBTRACT {
			@Override
			String apply(String lhs, String rhs) {
				return applyPrimitive( lhs, '-', rhs );
			}
		},
		MULTIPLY {
			@Override
			String apply(String lhs, String rhs) {
				return applyPrimitive( lhs, '*', rhs );
			}
		},
		DIVIDE {
			@Override
			String apply(String lhs, String rhs) {
				return applyPrimitive( lhs, '/', rhs );
			}
		},
		QUOT {
			@Override
			String apply(String lhs, String rhs) {
				return applyPrimitive( lhs, '/', rhs );
			}
		},
		MODULO {
			@Override
			String apply(String lhs, String rhs) {
//				return lhs + " % " + rhs;
				return "mod(" + lhs + "," + rhs + ")";
			}
		};

		abstract String apply(String lhs, String rhs);

		private static String applyPrimitive(String lhs, char operator, String rhs) {
			return '(' + lhs + operator + rhs + ')';
		}
	}

	/**
	 * Get the left-hand operand.
	 *
	 * @return The left-hand operand.
	 */
	public Expression getLeftHandOperand() {
		return lhsOperand;
	}

	/**
	 * Get the operation
	 *
	 * @return The operation
	 */
	public Operation getOperation() {
		return operation;
	}

	/**
	 * Get the right-hand operand.
	 *
	 * @return The right-hand operand.
	 */
	public Expression getRightHandOperand() {
		return rhsOperand;
	}
}
