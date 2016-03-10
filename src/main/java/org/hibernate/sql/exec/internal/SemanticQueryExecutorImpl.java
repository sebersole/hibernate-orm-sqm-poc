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

import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.engine.jdbc.spi.JdbcServices;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.query.QueryParameterBindings;
import org.hibernate.query.internal.PreparedStatementCreator;
import org.hibernate.query.internal.ResultSetConsumer;
import org.hibernate.resource.jdbc.spi.LogicalConnectionImplementor;
import org.hibernate.sql.ast.InterpretationOptions;
import org.hibernate.sql.ast.SelectQuery;
import org.hibernate.sql.exec.spi.ExecutionOptions;
import org.hibernate.sql.exec.spi.RowTransformer;
import org.hibernate.sql.exec.spi.SemanticQueryExecutor;
import org.hibernate.sql.gen.Callback;
import org.hibernate.sql.gen.NotYetImplementedException;
import org.hibernate.sql.gen.ParameterBinder;
import org.hibernate.sql.gen.Return;
import org.hibernate.sql.gen.SqlTreeWalker;
import org.hibernate.sqm.query.NonSelectStatement;
import org.hibernate.sqm.query.SelectStatement;

import static org.hibernate.sql.ast.SelectStatementInterpreter.interpret;

/**
 * @author Steve Ebersole
 */
public class SemanticQueryExecutorImpl implements SemanticQueryExecutor {
	@Override
	@SuppressWarnings("unchecked")
	public <T> List<T> executeSelect(
			SelectStatement sqm,
			InterpretationOptions interpretationOptions,
			ExecutionOptions executionOptions,
			QueryParameterBindings queryParameterBindings,
			RowTransformer<T> rowTransformer,
			Callback callback,
			SessionImplementor session) {
		// first interpret the SQM into a "SQL AST"
		final SelectQuery sqlTree = interpret( sqm, interpretationOptions, callback );

		return executeSelect(
				sqlTree,
				StandardCreator.INSTANCE,
				(ImmediateConsumer<T>) ImmediateConsumer.INSTANCE,
				queryParameterBindings,
				executionOptions,
				rowTransformer,
				session
		);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> List<T> executeSelect(
			SelectQuery sqlTree,
			ExecutionOptions executionOptions,
			QueryParameterBindings queryParameterBindings,
			RowTransformer<T> rowTransformer,
			SessionImplementor session) {
		return executeSelect(
				sqlTree,
				StandardCreator.INSTANCE,
				(ImmediateConsumer<T>) ImmediateConsumer.INSTANCE,
				queryParameterBindings,
				executionOptions,
				rowTransformer,
				session
		);
	}

	protected <T,R> T executeSelect(
			SelectQuery sqlTree,
			PreparedStatementCreator statementCreator,
			ResultSetConsumer<T,R> resultSetConsumer,
			QueryParameterBindings queryParameterBindings,
			ExecutionOptions executionOptions,
			RowTransformer<R> rowTransformer,
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

			// Execute the query
			final ResultSet resultSet = ps.executeQuery();
			logicalConnection.getResourceRegistry().register( resultSet, ps );

			return resultSetConsumer.consume( ps, resultSet, sqlTreeWalker.getReturns(), rowTransformer, session );
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
	@SuppressWarnings("unchecked")
	public <T> ScrollableResults executeSelect(
			SelectStatement sqm,
			ScrollMode scrollMode,
			InterpretationOptions interpretationOptions,
			ExecutionOptions executionOptions,
			QueryParameterBindings queryParameterBindings,
			RowTransformer<T> rowTransformer,
			Callback callback,
			SessionImplementor session) {
		// first interpret the SQM into a "SQL AST"
		final SelectQuery sqlTree = interpret( sqm, interpretationOptions, callback );
		return executeSelect(
				sqlTree,
				StandardCreator.INSTANCE,
				(ScrollableResultsConsumer<T>) ScrollableResultsConsumer.INSTANCE,
				queryParameterBindings,
				executionOptions,
				rowTransformer,
				session
		);
	}

	@Override
	public int executeDml(
			NonSelectStatement sqm,
			InterpretationOptions interpretationOptions,
			ExecutionOptions executionOptions,
			SessionImplementor session) {
		throw new NotYetImplementedException( "DML execution is not yet implemented" );
	}


	private static class StandardCreator implements PreparedStatementCreator {
		/**
		 * Singleton access
		 */
		public static final StandardCreator INSTANCE = new StandardCreator();

		@Override
		public PreparedStatement create(Connection connection, String sql) throws SQLException {
			return connection.prepareStatement( sql );
		}
	};

	private static class ForwardOnlyScrollableCreator implements PreparedStatementCreator {
		/**
		 * Singleton access
		 */
		public static final ForwardOnlyScrollableCreator INSTANCE = new ForwardOnlyScrollableCreator();

		@Override
		public PreparedStatement create(Connection connection, String sql) throws SQLException {
			return connection.prepareStatement(
					sql,
					ResultSet.TYPE_FORWARD_ONLY,
					ResultSet.CONCUR_READ_ONLY,
					ResultSet.CLOSE_CURSORS_AT_COMMIT
			);
		}
	}

	private static class InsensitiveScrollableCreator implements PreparedStatementCreator {
		/**
		 * Singleton access
		 */
		public static final InsensitiveScrollableCreator INSTANCE = new InsensitiveScrollableCreator();

		@Override
		public PreparedStatement create(Connection connection, String sql) throws SQLException {
			return connection.prepareStatement(
					sql,
					ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_READ_ONLY,
					ResultSet.CLOSE_CURSORS_AT_COMMIT
			);
		}
	}

	private static class SensitiveScrollableCreator implements PreparedStatementCreator {
		/**
		 * Singleton access
		 */
		public static final SensitiveScrollableCreator INSTANCE = new SensitiveScrollableCreator();

		@Override
		public PreparedStatement create(Connection connection, String sql) throws SQLException {
			return connection.prepareStatement(
					sql,
					ResultSet.TYPE_SCROLL_SENSITIVE,
					ResultSet.CONCUR_READ_ONLY,
					ResultSet.CLOSE_CURSORS_AT_COMMIT
			);
		}
	}

	private static class ImmediateConsumer<T> implements ResultSetConsumer<List<T>,T> {
		/**
		 * Singleton access
		 */
		public static final ImmediateConsumer INSTANCE = new ImmediateConsumer();

		public List<T> consume(
				PreparedStatement ps,
				ResultSet resultSet,
				List<Return> returns,
				RowTransformer<T> rowTransformer,
				SessionImplementor session) throws SQLException {
			int position = 1;

			final List<T> results = new ArrayList<T>();
			final int returnCount = returns.size();
			while ( resultSet.next() ) {
				final Object[] row = new Object[returnCount];
				for ( int i = 0; i < returnCount; i++ ) {
					row[i] = returns.get( i ).readResult(
							resultSet,
							position,
							session,
							null
					);
					position += returns.get( i ).getNumberOfColumnsRead( session );
				}
				results.add( rowTransformer.transformRow( row ) );
			}

			final LogicalConnectionImplementor logicalConnection = session.getJdbcCoordinator().getLogicalConnection();
			logicalConnection.getResourceRegistry().release( resultSet, ps );
			logicalConnection.getResourceRegistry().release( ps );

			return results;
		}
	}

	private static class ScrollableResultsConsumer<T> implements ResultSetConsumer<ScrollableResults,T> {
		/**
		 * Singleton access
		 */
		public static final ScrollableResultsConsumer INSTANCE = new ScrollableResultsConsumer();

		@Override
		public ScrollableResults consume(
				PreparedStatement ps,
				ResultSet resultSet,
				List<Return> returns,
				RowTransformer<T> rowTransformer,
				SessionImplementor session) throws SQLException {
//			return new ScrollableResultsImpl(
//
//			);
			throw new NotYetImplementedException();
		}
	}
}
