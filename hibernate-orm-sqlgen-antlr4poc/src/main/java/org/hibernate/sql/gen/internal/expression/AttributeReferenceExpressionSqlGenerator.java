package org.hibernate.sql.gen.internal.expression;

import org.hibernate.sql.gen.NotYetImplementedException;
import org.hibernate.sqm.query.expression.AttributeReferenceExpression;

/**
 * Created by John O'Hara on 27/08/15.
 */
public class AttributeReferenceExpressionSqlGenerator extends AbstractExpressionSqlGenerator<AttributeReferenceExpression>{

	@Override
	public String interpret(AttributeReferenceExpression expression) {
		throw new NotYetImplementedException();
	}
}
