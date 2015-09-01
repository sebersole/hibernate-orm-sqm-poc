package org.hibernate.sql.gen.internal.predicate;

import org.hibernate.sql.gen.NotYetImplementedException;
import org.hibernate.sqm.query.predicate.OrPredicate;

/**
 * Created by John O'Hara on 27/08/15.
 */
public class OrPredicateSqlGenerator extends AbstractPredicateSqlGenerator<OrPredicate>{

	@Override
	public String interpret(OrPredicate predicate) {
		throw new NotYetImplementedException();
	}
}
