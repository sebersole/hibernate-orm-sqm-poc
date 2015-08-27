package org.hibernate.sql.gen.internal.predicate;

import org.hibernate.sql.gen.internal.PredicateGeneratorNotImplementedException;
import org.hibernate.sqm.query.predicate.Predicate;
import org.hibernate.sqm.query.predicate.RelationalPredicate;

/**
 * Created by johara on 27/08/15.
 */
public class PredicateGenerator {
	public static String generatePredicateClause(Predicate predicate) {
		if(predicate instanceof RelationalPredicate ){
			return RelationalPredicateSqlGenerator.generateSql(predicate);
		}
		else{
			throw new PredicateGeneratorNotImplementedException();
		}

	}
}
