package org.hibernate.sql.gen.internal.expression;

import org.hibernate.sql.gen.NotYetImplementedException;
import org.hibernate.sqm.query.expression.CountStarFunction;

/**
 * Created by johara on 27/08/15.
 */
public class CountStarFunctionSqlGenerator extends AbstractExpressionSqlGenerator<CountStarFunction>
{

	@Override
	public String interpret(CountStarFunction expression) {
		throw new NotYetImplementedException();
	}
}
