package org.hibernate.sql.gen.internal.expression;

import org.hibernate.sql.gen.NotYetImplementedException;
import org.hibernate.sqm.query.expression.AggregateFunction;

/**
 * Created by John O'Hara on 27/08/15.
 */
public class AggregateFunctionSqlGenerator extends AbstractExpressionSqlGenerator<AggregateFunction>{

	@Override
	public String interpret(AggregateFunction expression) {
		throw new NotYetImplementedException();
	}
}
