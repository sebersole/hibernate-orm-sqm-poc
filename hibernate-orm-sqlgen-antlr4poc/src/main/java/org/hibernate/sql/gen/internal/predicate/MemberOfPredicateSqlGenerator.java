package org.hibernate.sql.gen.internal.predicate;

import org.hibernate.sql.gen.NotYetImplementedException;
import org.hibernate.sqm.query.predicate.MemberOfPredicate;

/**
 * Created by johara on 27/08/15.
 */
public class MemberOfPredicateSqlGenerator extends AbstractPredicateSqlGenerator<MemberOfPredicate>{

	@Override
	public String interpret(MemberOfPredicate predicate) {
		throw new NotYetImplementedException();
	}
}
