package org.hibernate.sql.gen.internal.expression;

import org.hibernate.sql.gen.NotYetImplementedException;
import org.hibernate.sqm.query.expression.PositionalParameterExpression;

/**
 * Created by John O'Hara on 27/08/15.
 */
public class PositionalParameterExpressionSqlGenerator extends AbstractExpressionSqlGenerator<PositionalParameterExpression>{

	@Override
	public String interpret(PositionalParameterExpression expression) {
		throw new NotYetImplementedException();
	}
}
