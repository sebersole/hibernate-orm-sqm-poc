package org.hibernate.sql.gen.internal.predicate;

import org.hibernate.sql.gen.NotYetImplementedException;
import org.hibernate.sqm.query.predicate.IsEmptyPredicate;

/**
 * Created by johara on 27/08/15.
 */
public class IsEmptyPredicateSqlGenerator extends AbstractPredicateSqlGenerator<IsEmptyPredicate>{

	@Override
	public String interpret(IsEmptyPredicate predicate) {
		throw new NotYetImplementedException();
	}
}
