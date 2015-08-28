package org.hibernate.sql.gen.internal.expression;

import org.hibernate.sql.gen.NotYetImplementedException;
import org.hibernate.sqm.query.expression.EntityTypeExpression;

/**
 * Created by johara on 27/08/15.
 */
public class EntityTypeExpressionSqlGenerator extends AbstractExpressionSqlGenerator<EntityTypeExpression>{

	@Override
	public String interpret(EntityTypeExpression expression) {
		throw new NotYetImplementedException();
	}
}
