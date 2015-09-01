package org.hibernate.sql.gen.internal.expression;

import org.hibernate.sql.gen.NotYetImplementedException;
import org.hibernate.sqm.query.expression.ConstantFieldExpression;

/**
 * Created by John O'Hara on 27/08/15.
 */
public class ConstantFieldExpressionSqlGenerator extends AbstractExpressionSqlGenerator<ConstantFieldExpression>{

	@Override
	public String interpret(ConstantFieldExpression expression) {
		throw new NotYetImplementedException();
	}
}
