/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.ast.predicate;

import org.hibernate.sql.ast.QuerySpec;
import org.hibernate.sql.ast.expression.Expression;
import org.hibernate.sql.gen.SqlTreeWalker;

/**
 * @author Steve Ebersole
 */
public class InSubQueryPredicate implements Predicate {
	private final Expression testExpression;
	private final QuerySpec subQuery;

	public InSubQueryPredicate(Expression testExpression, QuerySpec subQuery) {
		this.testExpression = testExpression;
		this.subQuery = subQuery;
	}

	public Expression getTestExpression() {
		return testExpression;
	}

	public QuerySpec getSubQuery() {
		return subQuery;
	}

	@Override
	public boolean isEmpty() {
		return false;
	}

	@Override
	public void accept(SqlTreeWalker sqlTreeWalker) {
		sqlTreeWalker.visitInSubQueryPredicate( this );
	}
}
