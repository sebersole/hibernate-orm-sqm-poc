package org.hibernate.sql.gen.internal;

/**
 * Created by John O'Hara on 27/08/15.
 */
public class InvalidPredicateException extends RuntimeException {
	public InvalidPredicateException(Class<?> predicateClass, Class<?> expectedClass) {
		super( "Invalid predicate class. Was expecting "
				.concat( expectedClass.getCanonicalName() )
				.concat( "; received " )
				.concat( predicateClass.getCanonicalName() )
		);
	}
}
