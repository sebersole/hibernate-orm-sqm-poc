package org.hibernate.sql.gen.internal.expression;

import org.hibernate.sql.gen.NotYetImplementedException;
import org.hibernate.sqm.query.expression.LiteralExpression;

/**
 * Created by johara on 27/08/15.
 */
public class LiteralExpressionSqlGenerator extends AbstractExpressionSqlGenerator<LiteralExpression>{

	@Override
	public String interpret(LiteralExpression expression) {
		throw new NotYetImplementedException();
	}
}
