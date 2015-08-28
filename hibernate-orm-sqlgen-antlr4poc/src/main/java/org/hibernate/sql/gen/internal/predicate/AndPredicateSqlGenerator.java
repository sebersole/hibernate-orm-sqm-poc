package org.hibernate.sql.gen.internal.predicate;

import org.hibernate.sql.gen.NotYetImplementedException;
import org.hibernate.sqm.query.predicate.AndPredicate;

/**
 * Created by johara on 27/08/15.
 */
public class AndPredicateSqlGenerator extends AbstractPredicateSqlGenerator<AndPredicate>{

	@Override
	public String interpret(AndPredicate predicate) {
		throw new NotYetImplementedException();
	}
}
