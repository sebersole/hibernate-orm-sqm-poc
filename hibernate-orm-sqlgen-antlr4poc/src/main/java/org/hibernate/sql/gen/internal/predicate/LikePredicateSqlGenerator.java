package org.hibernate.sql.gen.internal.predicate;

import org.hibernate.sql.gen.NotYetImplementedException;
import org.hibernate.sqm.query.predicate.LikePredicate;

/**
 * Created by John O'Hara on 27/08/15.
 */
public class LikePredicateSqlGenerator extends AbstractPredicateSqlGenerator<LikePredicate>{

	@Override
	public String interpret(LikePredicate predicate) {
		throw new NotYetImplementedException();
	}
}
