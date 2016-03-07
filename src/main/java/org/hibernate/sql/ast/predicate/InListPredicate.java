/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.ast.predicate;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.sql.ast.expression.Expression;
import org.hibernate.sqm.Helper;

/**
 * @author Steve Ebersole
 */
public class InListPredicate implements Predicate {
	private final Expression testExpression;
	private final List<Expression> listExpressions;

	public InListPredicate(Expression testExpression) {
		this( testExpression, new ArrayList<Expression>() );
	}

	public InListPredicate(Expression testExpression, Expression... listExpressions) {
		this( testExpression, Helper.toExpandableList( listExpressions ) );
	}

	public InListPredicate(
			Expression testExpression,
			List<Expression> listExpressions) {
		this( testExpression, listExpressions, false );
	}

	public InListPredicate(
			Expression testExpression,
			List<Expression> listExpressions,
			boolean negated) {
		this.testExpression = testExpression;
		this.listExpressions = listExpressions;
	}

	public Expression getTestExpression() {
		return testExpression;
	}

	public List<Expression> getListExpressions() {
		return listExpressions;
	}

	public void addExpression(Expression expression) {
		listExpressions.add( expression );
	}
}
