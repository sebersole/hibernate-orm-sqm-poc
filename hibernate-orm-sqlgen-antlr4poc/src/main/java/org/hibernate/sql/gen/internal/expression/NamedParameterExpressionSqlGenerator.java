package org.hibernate.sql.gen.internal.expression;

import org.hibernate.sql.gen.NotYetImplementedException;
import org.hibernate.sqm.query.expression.NamedParameterExpression;

/**
 * Created by John O'Hara on 27/08/15.
 */
public class NamedParameterExpressionSqlGenerator extends AbstractExpressionSqlGenerator<NamedParameterExpression>{

	@Override
	public String interpret(NamedParameterExpression expression) {
		throw new NotYetImplementedException();
	}
}
