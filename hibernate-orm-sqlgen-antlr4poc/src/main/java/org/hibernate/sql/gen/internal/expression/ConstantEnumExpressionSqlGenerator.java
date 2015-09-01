package org.hibernate.sql.gen.internal.expression;

import org.hibernate.sql.gen.NotYetImplementedException;
import org.hibernate.sqm.query.expression.ConstantEnumExpression;

/**
 * Created by John O'Hara on 27/08/15.
 */
public class ConstantEnumExpressionSqlGenerator extends AbstractExpressionSqlGenerator<ConstantEnumExpression>{

	@Override
	public String interpret(ConstantEnumExpression expression) {
		throw new NotYetImplementedException();
	}
}
