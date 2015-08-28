package org.hibernate.sql.gen.internal.expression;

import org.hibernate.sql.gen.NotYetImplementedException;
import org.hibernate.sqm.query.expression.SubQueryExpression;

/**
 * Created by johara on 27/08/15.
 */
public class SubQueryExpressionSqlGenerator extends AbstractExpressionSqlGenerator<SubQueryExpression>{

	@Override
	public String interpret(SubQueryExpression expression) {
		throw new NotYetImplementedException();
	}
}
