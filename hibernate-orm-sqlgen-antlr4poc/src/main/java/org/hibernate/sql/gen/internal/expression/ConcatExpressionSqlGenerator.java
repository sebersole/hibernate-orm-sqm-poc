package org.hibernate.sql.gen.internal.expression;

import org.hibernate.sql.gen.NotYetImplementedException;
import org.hibernate.sqm.query.expression.ConcatExpression;

/**
 * Created by John O'Hara on 27/08/15.
 */
public class ConcatExpressionSqlGenerator extends AbstractExpressionSqlGenerator<ConcatExpression>  {

	@Override
	public String interpret(ConcatExpression expression) {
		throw new NotYetImplementedException();
	}
}
