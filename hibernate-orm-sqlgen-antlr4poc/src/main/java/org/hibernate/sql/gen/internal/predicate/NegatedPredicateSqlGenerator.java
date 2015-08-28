package org.hibernate.sql.gen.internal.predicate;

import org.hibernate.sql.gen.NotYetImplementedException;
import org.hibernate.sqm.query.predicate.NegatedPredicate;

/**
 * Created by johara on 27/08/15.
 */
public class NegatedPredicateSqlGenerator extends AbstractPredicateSqlGenerator<NegatedPredicate>{

	@Override
	public String interpret(NegatedPredicate predicate) {
		throw new NotYetImplementedException();
	}
}
