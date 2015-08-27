package org.hibernate.sql.gen;

import org.hibernate.sql.gen.internal.JdbcSelectPlanImpl;
import org.hibernate.sql.orm.QueryOptions;
import org.hibernate.sqm.query.SelectStatement;
import org.hibernate.sqm.query.Statement;

/**
 * Interprets a Semantic Query Model (SQM) into a group of JdbcOperationPlan objects.
 *
 * @author johara
 * @author Steve Ebersole
 */
public class SqmJdbcInterpreter {
	public static JdbcSelectPlan interpret(SelectStatement statement, QueryOptions queryOptions, Callback callback) {
		// todo : this needs serious work
		return new JdbcSelectPlanImpl();
	}

	public static JdbcOperationPlan[] interpret(Statement statement, QueryOptions queryOptions, Callback callback) {
		// todo : add a NonSelectStatement to SQM for grouping UPDATE, DELETE and INSERT queries.
		throw new NotYetImplementedException();
	}
}
