package org.hibernate.sql.gen.internal.expression;

import org.hibernate.sql.gen.NotYetImplementedException;
import org.hibernate.sqm.query.expression.FromElementReferenceExpression;

/**
 * Created by johara on 27/08/15.
 */
public class FromElementReferenceExpressionSqlGenerator extends AbstractExpressionSqlGenerator<FromElementReferenceExpression>{

	@Override
	public String interpret(FromElementReferenceExpression expression) {
		throw new NotYetImplementedException();
	}
}
