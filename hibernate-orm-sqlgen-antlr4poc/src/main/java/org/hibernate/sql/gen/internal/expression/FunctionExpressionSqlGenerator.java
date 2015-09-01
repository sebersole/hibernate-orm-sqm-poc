package org.hibernate.sql.gen.internal.expression;

import org.hibernate.sql.gen.NotYetImplementedException;
import org.hibernate.sqm.query.expression.FunctionExpression;

/**
 * Created by John O'Hara on 27/08/15.
 */
public class FunctionExpressionSqlGenerator extends AbstractExpressionSqlGenerator<FunctionExpression> {

	@Override
	public String interpret(FunctionExpression expression) {
		throw new NotYetImplementedException();
	}
}
