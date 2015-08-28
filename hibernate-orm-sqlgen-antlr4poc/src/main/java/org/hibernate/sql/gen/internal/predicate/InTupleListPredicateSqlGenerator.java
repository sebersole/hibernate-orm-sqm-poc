package org.hibernate.sql.gen.internal.predicate;

import org.hibernate.sql.gen.NotYetImplementedException;
import org.hibernate.sqm.query.predicate.InTupleListPredicate;

/**
 * Created by johara on 27/08/15.
 */
public class InTupleListPredicateSqlGenerator extends AbstractPredicateSqlGenerator<InTupleListPredicate>{

	@Override
	public String interpret(InTupleListPredicate predicate) {
		throw new NotYetImplementedException();
	}
}
