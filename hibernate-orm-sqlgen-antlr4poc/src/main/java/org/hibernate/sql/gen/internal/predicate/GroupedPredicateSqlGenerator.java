package org.hibernate.sql.gen.internal.predicate;

import org.hibernate.sql.gen.NotYetImplementedException;
import org.hibernate.sqm.query.predicate.GroupedPredicate;

/**
 * Created by John O'Hara on 27/08/15.
 */
public class GroupedPredicateSqlGenerator extends AbstractPredicateSqlGenerator<GroupedPredicate>{

	@Override
	public String interpret(GroupedPredicate predicate) {
		throw new NotYetImplementedException();
	}
}
