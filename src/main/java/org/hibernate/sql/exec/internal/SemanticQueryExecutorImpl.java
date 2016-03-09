/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.exec.internal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.engine.jdbc.spi.JdbcServices;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.resource.jdbc.spi.LogicalConnectionImplementor;
import org.hibernate.sql.ast.InterpretationOptions;
import org.hibernate.sql.ast.SelectQuery;
import org.hibernate.sql.exec.spi.ExecutionOptions;
import org.hibernate.sql.exec.spi.RowTransformer;
import org.hibernate.sql.exec.spi.SemanticQueryExecutor;
import org.hibernate.sql.gen.Callback;
import org.hibernate.sql.gen.NotYetImplementedException;
import org.hibernate.sql.gen.ParameterBinder;
import org.hibernate.sql.gen.SqlTreeWalker;
import org.hibernate.sqm.query.NonSelectStatement;
import org.hibernate.sqm.query.SelectStatement;

import static org.hibernate.sql.ast.SelectStatementInterpreter.interpret;

/**
 * @author Steve Ebersole
 */
public class SemanticQueryExecutorImpl implements SemanticQueryExecutor {
	@Override
	public <T> List<T> executeSelect(
			SelectStatement sqm,
			InterpretationOptions interpretationOptions,
			ExecutionOptions executionOptions,
			RowTransformer<T> rowTransformer,
			Callback callback,
			SessionImplementor session) {
		// first interpret the SQM into a "SQL AST"
		final SelectQuery sqlTree = interpret( sqm, interpretationOptions, callback );

		// then delegate to the "SQL AST" execution form
		return executeSelect( sqlTree, executionOptions, rowTransformer, session );
	}

	@Override
	public <T> List<T> executeSelect(
			SelectQuery sqlTree,
			ExecutionOptions executionOptions,
			RowTransformer<T> rowTransformer,
			SessionImplementor session) {
		// Walk the SQL AST.  This produces:
		//		* SQL string
		//		* ParameterBinders
		//		* Returns
		final SqlTreeWalker sqlTreeWalker = new SqlTreeWalker( session.getFactory(), executionOptions.getParameterBindings() );
		sqlTreeWalker.visitSelectQuery( sqlTree );

		// Now start the execution
		final LogicalConnectionImplementor logicalConnection = session.getJdbcCoordinator().getLogicalConnection();
		final Connection connection = logicalConnection.getPhysicalConnection();

		final JdbcServices jdbcServices = session.getFactory().getServiceRegistry().getService( JdbcServices.class );

		final String sql = sqlTreeWalker.getSql();
		try {
			jdbcServices.getSqlStatementLogger().logStatement( sql );

			// prepare the query
			final PreparedStatement ps;
			if ( executionOptions.getScrollMode() != null ) {
				ps = connection.prepareStatement(
						sql,
						executionOptions.getScrollMode().toResultSetType(),
						ResultSet.CLOSE_CURSORS_AT_COMMIT
				);
			}
			else {
				ps = connection.prepareStatement( sql );
			}
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
						executionOptions.getParameterBindings(),
						session
				);
			}

			// Execute the query
			final ResultSet resultSet = ps.executeQuery();
			logicalConnection.getResourceRegistry().register( resultSet, ps );

			// read results
			final List<T> results = new ArrayList<T>();
			final int returnCount = sqlTreeWalker.getReturns().size();
			while ( resultSet.next() ) {
				position = 1;
				final Object[] row = new Object[returnCount];
				for ( int i = 0; i < returnCount; i++ ) {
					row[i] = sqlTreeWalker.getReturns().get( i ).readResult(
							resultSet,
							position,
							session,
							null
					);
					position += sqlTreeWalker.getReturns().get( i ).getNumberOfColumnsRead( session );
				}
				results.add( rowTransformer.transformRow( row ) );
			}

			logicalConnection.getResourceRegistry().release( resultSet, ps );
			logicalConnection.getResourceRegistry().release( ps );

			return results;
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
	public int executeDml(
			NonSelectStatement sqm,
			InterpretationOptions interpretationOptions,
			ExecutionOptions executionOptions,
			SessionImplementor session) {
		throw new NotYetImplementedException( "DML execution is not yet implemented" );
	}
}
