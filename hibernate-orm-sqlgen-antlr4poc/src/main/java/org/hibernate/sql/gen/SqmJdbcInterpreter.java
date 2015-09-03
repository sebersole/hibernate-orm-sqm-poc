package org.hibernate.sql.gen;

import org.hibernate.sql.gen.internal.JdbcSelectPlanImpl;
import org.hibernate.sql.orm.QueryOptions;
import org.hibernate.sqm.query.NonSelectStatement;
import org.hibernate.sqm.query.SelectStatement;

/**
 * Interprets a Semantic Query Model (SQM) into a group of JdbcOperationPlan objects.
 *
 * @author John O'Hara
 * @author Steve Ebersole
 */
public class SqmJdbcInterpreter {
	public static JdbcSelectPlan interpret(SelectStatement statement, QueryOptions queryOptions, Callback callback) {
		// todo : this needs serious work
		return new JdbcSelectPlanImpl(statement);
	}

	public static JdbcOperationPlan[] interpret(NonSelectStatement statement, QueryOptions queryOptions, Callback callback) {
		// todo : add a NonSelectStatement to SQM for grouping UPDATE, DELETE and INSERT queries.
		throw new NotYetImplementedException();
	}

}
