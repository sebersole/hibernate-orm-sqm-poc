package org.hibernate.sql.gen.internal.expression;

import org.hibernate.sql.gen.internal.InvalidExpressionException;
import org.hibernate.sqm.query.expression.Expression;

/**
 * Created by johara on 27/08/15.
 */
public abstract class AbstractExpressionSqlGenerator<T extends Expression> implements ExpressionSqlGenerator<T> {

	//if using generics, do we need this?
	protected void checkType(T expression){
		Class<T> type = null;
		if ( !(type.isInstance( expression )) ) {
			throw new InvalidExpressionException(expression.getClass(), type);
		}
	}

}
