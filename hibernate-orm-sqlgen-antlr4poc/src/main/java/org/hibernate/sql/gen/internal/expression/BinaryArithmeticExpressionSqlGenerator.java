package org.hibernate.sql.gen.internal.expression;

import org.hibernate.sql.gen.NotYetImplementedException;
import org.hibernate.sqm.query.expression.BinaryArithmeticExpression;

/**
 * Created by John O'Hara on 27/08/15.
 */
public class BinaryArithmeticExpressionSqlGenerator extends AbstractExpressionSqlGenerator<BinaryArithmeticExpression> {

	@Override
	public String interpret(BinaryArithmeticExpression expression) {
		throw new NotYetImplementedException();
	}
}
