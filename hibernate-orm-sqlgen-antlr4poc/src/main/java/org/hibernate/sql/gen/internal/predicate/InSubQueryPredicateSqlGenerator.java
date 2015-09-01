package org.hibernate.sql.gen.internal.predicate;

import org.hibernate.sql.gen.NotYetImplementedException;
import org.hibernate.sqm.query.predicate.InSubQueryPredicate;

/**
 * Created by John O'Hara on 27/08/15.
 */
public class InSubQueryPredicateSqlGenerator extends AbstractPredicateSqlGenerator<InSubQueryPredicate>{

	@Override
	public String interpret(InSubQueryPredicate predicate) {
		throw new NotYetImplementedException();
	}
}
