/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.exec.internal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.hibernate.engine.jdbc.spi.JdbcServices;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.query.spi.QueryParameterBindings;
import org.hibernate.resource.jdbc.spi.LogicalConnectionImplementor;
import org.hibernate.sql.ast.SelectQuery;
import org.hibernate.sql.exec.spi.ExecutionOptions;
import org.hibernate.sql.exec.spi.PreparedStatementCreator;
import org.hibernate.sql.exec.spi.PreparedStatementExecutor;
import org.hibernate.sql.exec.spi.RowTransformer;
import org.hibernate.sql.exec.spi.SqlTreeExecutor;
import org.hibernate.sql.gen.Callback;
import org.hibernate.sql.gen.NotYetImplementedException;
import org.hibernate.sql.gen.ParameterBinder;
import org.hibernate.sql.gen.SqlTreeWalker;

/**
 * Standard SqlTreeExecutor implementation
 *
 * @author Steve Ebersole
 */
public class SqlTreeExecutorImpl implements SqlTreeExecutor {
	@Override
	public <R, T> R executeSelect(
			SelectQuery sqlTree,
			PreparedStatementCreator statementCreator,
			PreparedStatementExecutor<R, T> preparedStatementExecutor,
			ExecutionOptions executionOptions,
			QueryParameterBindings queryParameterBindings,
			RowTransformer<T> rowTransformer,
			Callback callback,
			SessionImplementor session) {
		// Walk the SQL AST.  This produces:
		//		* SQL string
		//		* ParameterBinders
		//		* Returns
		final SqlTreeWalker sqlTreeWalker = new SqlTreeWalker( session.getFactory(), queryParameterBindings );
		sqlTreeWalker.visitSelectQuery( sqlTree );

		// Now start the execution
		final LogicalConnectionImplementor logicalConnection = session.getJdbcCoordinator().getLogicalConnection();
		final Connection connection = logicalConnection.getPhysicalConnection();

		final JdbcServices jdbcServices = session.getFactory().getServiceRegistry().getService( JdbcServices.class );

		final String sql = sqlTreeWalker.getSql();
		try {
			jdbcServices.getSqlStatementLogger().logStatement( sql );

			// prepare the query
			final PreparedStatement ps = statementCreator.create( connection, sql );
			logicalConnection.getResourceRegistry().register( ps, true );

			// set options
			if ( executionOptions.getFetchSize() != null ) {
				ps.setFetchSize( executionOptions.getFetchSize() );
			}
			if ( executionOptions.getTimeout() != null ) {
				ps.setQueryTimeout( executionOptions.getTimeout() );
			}

			// bind parameters
			int position = 1;
			for ( ParameterBinder parameterBinder : sqlTreeWalker.getParameterBinders() ) {
				position += parameterBinder.bindParameterValue(
						ps,
						position,
						queryParameterBindings,
						session
				);
			}

			return preparedStatementExecutor.execute( ps, sqlTreeWalker.getReturns(), rowTransformer, session );
		}
		catch (SQLException e) {
			throw jdbcServices.getSqlExceptionHelper().convert(
					e,
					"JDBC exception executing SQL [" + sql + "]"
			);
		}
		finally {
			logicalConnection.afterStatement();
		}
	}

	@Override
	public Object[] executeInsert(
			Object sqlTree,
			PreparedStatementCreator statementCreator,
			ExecutionOptions executionOptions,
			QueryParameterBindings queryParameterBindings,
			SessionImplementor session) {
		throw new NotYetImplementedException( "DML execution is not yet implemented" );
	}

	@Override
	public int executeUpdate(
			Object sqlTree,
			PreparedStatementCreator statementCreator,
			ExecutionOptions executionOptions,
			QueryParameterBindings queryParameterBindings,
			SessionImplementor session) {
		throw new NotYetImplementedException( "DML execution is not yet implemented" );
	}

	@Override
	public int executeDelete(
			Object sqlTree,
			PreparedStatementCreator statementCreator,
			ExecutionOptions executionOptions,
			QueryParameterBindings queryParameterBindings,
			SessionImplementor session) {
		throw new NotYetImplementedException( "DML execution is not yet implemented" );
	}
}
