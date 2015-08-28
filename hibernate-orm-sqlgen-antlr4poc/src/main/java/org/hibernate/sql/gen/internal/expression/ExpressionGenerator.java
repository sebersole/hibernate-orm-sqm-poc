package org.hibernate.sql.gen.internal.expression;

import org.hibernate.sql.gen.internal.ExpressionGeneratorNotImplementedException;
import org.hibernate.sqm.query.expression.AttributeReferenceExpression;
import org.hibernate.sqm.query.expression.Expression;
import org.hibernate.sqm.query.expression.LiteralExpression;

/**
 * Created by johara on 27/08/15.
 */
public class ExpressionGenerator {
	//TODO: redo dispatch for this call
	public String generateExpressionSql(Expression expression) {

		if(expression instanceof LiteralExpression ){
			return new LiteralExpressionSqlGenerator().interpret( (LiteralExpression) expression );
		}
		if(expression instanceof AttributeReferenceExpression){
			return new AttributeReferenceExpressionSqlGenerator().interpret( (AttributeReferenceExpression) expression );
		}
		else{
			throw new ExpressionGeneratorNotImplementedException();
		}

	}

}
