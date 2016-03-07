package org.hibernate.sql.gen;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.sql.orm.QueryOptions;

/**
 * Performs parameter value binding to a JDBC PreparedStatement.
 *
 * @author Steve Ebersole
 * @author John O'Hara
 */
public interface ParameterBinder {
	int bindParameterValue(
			PreparedStatement statement,
			int startPosition,
			QueryOptions queryOptions,
			SessionImplementor session) throws SQLException;
}
