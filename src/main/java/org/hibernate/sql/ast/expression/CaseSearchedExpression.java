/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.ast.expression;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.sql.ast.predicate.Predicate;
import org.hibernate.type.Type;

/**
 * @author Steve Ebersole
 */
public class CaseSearchedExpression implements Expression {
	private final Type type;

	private List<WhenFragment> whenFragments = new ArrayList<WhenFragment>();
	private Expression otherwise;

	public CaseSearchedExpression(Type type) {
		this.type = type;
	}

	public List<WhenFragment> getWhenFragments() {
		return whenFragments;
	}

	public Expression getOtherwise() {
		return otherwise;
	}

	public void when(Predicate predicate, Expression result) {
		whenFragments.add( new WhenFragment( predicate, result ) );
	}

	public void otherwise(Expression otherwiseExpression) {
		this.otherwise = otherwiseExpression;
		// todo : inject implied type?
	}

	@Override
	public Type getType() {
		return type;
	}

	public static class WhenFragment {
		private final Predicate predicate;
		private final Expression result;

		public WhenFragment(Predicate predicate, Expression result) {
			this.predicate = predicate;
			this.result = result;
		}

		public Predicate getPredicate() {
			return predicate;
		}

		public Expression getResult() {
			return result;
		}
	}
}
