package org.hibernate.sql.gen.internal.predicate;

import org.hibernate.sql.gen.NotYetImplementedException;
import org.hibernate.sqm.query.predicate.BetweenPredicate;

/**
 * Created by johara on 27/08/15.
 */
public class BetweenPredicateSqlGenerator extends AbstractPredicateSqlGenerator<BetweenPredicate>{

	@Override
	public String interpret(BetweenPredicate predicate) {
		throw new NotYetImplementedException();
	}
}
