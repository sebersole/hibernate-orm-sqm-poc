package org.hibernate.sql.gen.internal.predicate;

import org.hibernate.sql.gen.NotYetImplementedException;
import org.hibernate.sqm.query.predicate.IsNullPredicate;

/**
 * Created by John O'Hara on 27/08/15.
 */
public class IsNullPredicateSqlGenerator extends AbstractPredicateSqlGenerator<IsNullPredicate>{

	@Override
	public String interpret(IsNullPredicate predicate) {
		throw new NotYetImplementedException();
	}
}
