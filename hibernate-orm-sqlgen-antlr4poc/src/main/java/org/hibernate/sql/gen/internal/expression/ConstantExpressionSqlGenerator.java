package org.hibernate.sql.gen.internal.expression;

import org.hibernate.sql.gen.NotYetImplementedException;
import org.hibernate.sqm.query.expression.ConstantExpression;

/**
 * Created by John O'Hara on 27/08/15.
 */
public class ConstantExpressionSqlGenerator extends AbstractExpressionSqlGenerator<ConstantExpression>{

	@Override
	public String interpret(ConstantExpression expression) {
		throw new NotYetImplementedException();
	}
}
