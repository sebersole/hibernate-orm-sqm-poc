package org.hibernate.sql.gen.internal.expression;

import org.hibernate.sqm.query.expression.Expression;

/**
 * Created by John O'Hara on 28/08/15.
 */
public interface ExpressionSqlGenerator<T extends Expression> {
	String interpret(T expression);
}
