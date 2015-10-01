package org.hibernate.sql.gen;

import org.hibernate.sql.orm.QueryOptions;
import org.hibernate.sqm.query.NonSelectStatement;
import org.hibernate.sqm.query.SelectStatement;

/**
 * Interprets a Semantic Query Model (SQM) into a group of JdbcOperationPlan objects.
 *
 * @author Steve Ebersole
 * @author John O'Hara
 */
public class SqmJdbcInterpreter {
	/**
	 * Responsible for interpreting a SelectStatement into a JdbcSelectPlan describing how to perform
	 * the query and load the results.
	 *
	 * @param statement The SelectStatement to interpret
	 * @param queryOptions Any options (lock-modes, etc)
	 * @param callback Callback for various
	 *
	 * @return The JdbcSelectPlan describing the select query execution
	 */
	public static JdbcSelectPlan interpret(SelectStatement statement, QueryOptions queryOptions, Callback callback) {
		throw new NotYetImplementedException();
	}

	public static JdbcOperationPlan[] interpret(NonSelectStatement statement, QueryOptions queryOptions, Callback callback) {
		throw new NotYetImplementedException();
	}

}
