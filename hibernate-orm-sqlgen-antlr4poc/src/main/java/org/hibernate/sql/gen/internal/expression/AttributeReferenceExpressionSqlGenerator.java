package org.hibernate.sql.gen.internal.expression;

import org.hibernate.sql.gen.internal.InvalidExpressionException;
import org.hibernate.sqm.query.expression.AttributeReferenceExpression;
import org.hibernate.sqm.query.expression.Expression;

/**
 * Created by johara on 27/08/15.
 */
public class AttributeReferenceExpressionSqlGenerator {
	public static String generateSql(Expression expression) {
		if ( !(expression instanceof AttributeReferenceExpression) ) {
			throw new InvalidExpressionException(expression.getClass(), AttributeReferenceExpression.class);
		}

		return ((AttributeReferenceExpression) expression).getSource().getTypeDescriptor().getTypeName();
	}
}
