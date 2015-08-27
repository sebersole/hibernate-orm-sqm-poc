package org.hibernate.sql.gen.internal.expression;

import org.hibernate.sql.gen.internal.InvalidExpressionException;
import org.hibernate.sqm.query.expression.Expression;
import org.hibernate.sqm.query.expression.LiteralExpression;

/**
 * Created by johara on 27/08/15.
 */
public class LiteralExpressionSqlGenerator {
	public static String generateSql(Expression expression) {
		if ( !(expression instanceof LiteralExpression) ) {
			throw new InvalidExpressionException(expression.getClass(), LiteralExpression.class);
		}

		return ((LiteralExpression) expression).getLiteralValue().toString();
	}
}
