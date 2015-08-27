package org.hibernate.sql.gen.internal;

/**
 * Created by johara on 27/08/15.
 */
public class InvalidExpressionException extends RuntimeException {
	public InvalidExpressionException(Class<?> predicateClass, Class<?> expectedClass) {
		super( "Invalid predicate class. Was expecting "
				.concat( expectedClass.getCanonicalName() )
				.concat( "; received " )
				.concat( predicateClass.getCanonicalName() )
		);
	}
}
