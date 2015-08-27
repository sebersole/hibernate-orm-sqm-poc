package org.hibernate.sql.gen.internal.predicate;

import org.hibernate.sql.gen.internal.InvalidPredicateException;
import org.hibernate.sql.gen.internal.expression.ExpressionGenerator;
import org.hibernate.sqm.query.predicate.Predicate;
import org.hibernate.sqm.query.predicate.RelationalPredicate;

/**
 * Created by johara on 27/08/15.
 */
public class RelationalPredicateSqlGenerator {

	public static String generateSql(Predicate predicate) {
		if ( !(predicate instanceof RelationalPredicate) ) {
			throw new InvalidPredicateException(predicate.getClass(), RelationalPredicate.class);
		}
		RelationalPredicate relationalPredicate = (RelationalPredicate) predicate;

		StringBuilder predicateString  = new StringBuilder(  );

//		TODO: tidy this up
		predicateString.append( ExpressionGenerator.generateExpressionSql( relationalPredicate.getLeftHandExpression() ) );
		predicateString.append(" ");
		predicateString.append(relationalPredicate.getType().toString());
		predicateString.append(" ");
		predicateString.append( ExpressionGenerator.generateExpressionSql( relationalPredicate.getRightHandExpression() ));

		return predicateString.toString();
	}
}
