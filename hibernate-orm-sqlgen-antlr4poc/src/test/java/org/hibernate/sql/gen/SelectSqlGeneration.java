package org.hibernate.sql.gen;

import org.hibernate.query.parser.SemanticQueryInterpreter;
import org.hibernate.sql.gen.sqm.ConsumerContextTestingImpl;
import org.hibernate.sqm.query.SelectStatement;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

/**
 * Created by johara on 27/08/15.
 */
public class SelectSqlGeneration {

	@Test
	public void simpleSelectTest() {
		final String query = "from DTO d where d.id = 1";
		final SelectStatement selectStatement = (SelectStatement) SemanticQueryInterpreter.interpret(
				query,
				new ConsumerContextTestingImpl()
		);

		JdbcSelectPlan jdbcSelectPlan = SqmJdbcInterpreter.interpret( selectStatement, null, null );

		assertNotNull( jdbcSelectPlan );
	}
}
