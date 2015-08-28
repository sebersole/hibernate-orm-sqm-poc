package org.hibernate.sql.gen.internal.predicate;

import org.hibernate.sql.gen.internal.InvalidExpressionException;
import org.hibernate.sqm.query.predicate.Predicate;


/**
 * Created by johara on 27/08/15.
 */
public abstract class AbstractPredicateSqlGenerator<T extends Predicate> implements PredicateSqlGenerator<T> {

	//if using generics, do we need this?
	protected void checkType(T predicate){
		Class<T> type = null;
		if ( !(type.isInstance( predicate )) ) {
			throw new InvalidExpressionException(predicate.getClass(), type);
		}
	}

}
