package org.hibernate.sql.gen.internal.predicate;

import org.hibernate.sql.gen.NotYetImplementedException;
import org.hibernate.sqm.query.predicate.RelationalPredicate;

/**
 * Created by John O'Hara on 27/08/15.
 */
public class RelationalPredicateSqlGenerator extends AbstractPredicateSqlGenerator<RelationalPredicate>{

//	public static String generateSql(Predicate predicate) {
//		if ( !(predicate instanceof RelationalPredicate) ) {
//			throw new InvalidPredicateException(predicate.getClass(), RelationalPredicate.class);
//		}
//		RelationalPredicate relationalPredicate = (RelationalPredicate) predicate;
//
//		StringBuilder predicateString  = new StringBuilder(  );
//
////		TODO: tidy this up
//		predicateString.append( ExpressionGenerator.generateExpressionSql( relationalPredicate.getLeftHandExpression() ) );
//		predicateString.append(" ");
//		predicateString.append(relationalPredicate.getType().toString());
//		predicateString.append(" ");
//		predicateString.append( ExpressionGenerator.generateExpressionSql( relationalPredicate.getRightHandExpression() ));
//
//		return predicateString.toString();
//	}
//
	@Override
	public String interpret(RelationalPredicate predicate) {
		throw new NotYetImplementedException();
	}
}
