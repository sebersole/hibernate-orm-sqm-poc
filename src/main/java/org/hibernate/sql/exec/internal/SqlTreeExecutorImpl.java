/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.exec.internal;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.CacheMode;
import org.hibernate.cache.spi.QueryCache;
import org.hibernate.cache.spi.QueryKey;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.query.proposed.QueryOptions;
import org.hibernate.query.proposed.spi.ExecutionContext;
import org.hibernate.query.proposed.spi.QueryParameterBindings;
import org.hibernate.result.Outputs;
import org.hibernate.sql.NotYetImplementedException;
import org.hibernate.sql.ast.SelectQuery;
import org.hibernate.sql.ast.select.SqlSelectionDescriptor;
import org.hibernate.sql.convert.spi.Callback;
import org.hibernate.sql.exec.results.process.internal.JdbcValuesSourceProcessingStateStandardImpl;
import org.hibernate.sql.exec.results.process.internal.RowProcessingStateStandardImpl;
import org.hibernate.sql.exec.results.process.internal.RowReaderStandardImpl;
import org.hibernate.sql.exec.results.process.internal.values.JdbcValuesSource;
import org.hibernate.sql.exec.results.process.internal.values.JdbcValuesSourceCacheHit;
import org.hibernate.sql.exec.results.process.internal.values.JdbcValuesSourceResultSetImpl;
import org.hibernate.sql.exec.results.process.spi.ResultSetProcessingOptions;
import org.hibernate.sql.exec.results.process.spi.RowProcessingState;
import org.hibernate.sql.exec.results.process.spi.RowReader;
import org.hibernate.sql.exec.results.process.spi2.Initializer;
import org.hibernate.sql.exec.results.process.spi2.InitializerSource;
import org.hibernate.sql.exec.results.process.spi2.ReturnAssembler;
import org.hibernate.sql.exec.results.spi.ResolvedReturn;
import org.hibernate.sql.exec.spi.PreparedStatementCreator;
import org.hibernate.sql.exec.spi.PreparedStatementExecutor;
import org.hibernate.sql.exec.spi.RowTransformer;
import org.hibernate.sql.exec.spi.SqlAstSelectInterpreter;
import org.hibernate.sql.exec.spi.SqlSelectInterpretation;
import org.hibernate.sql.exec.spi.SqlTreeExecutor;

import org.jboss.logging.Logger;

/**
 * Standard SqlTreeExecutor implementation
 *
 * @author Steve Ebersole
 */
public class SqlTreeExecutorImpl implements SqlTreeExecutor {
	private static final Logger log = Logger.getLogger( SqlTreeExecutorImpl.class );

	@SuppressWarnings("unchecked")
	@Override
	public <R, T> R executeSelect(
			SelectQuery sqlTree,
			PreparedStatementCreator statementCreator,
			PreparedStatementExecutor preparedStatementExecutor,
			QueryOptions queryOptions,
			QueryParameterBindings queryParameterBindings,
			RowTransformer<T> rowTransformer,
			Callback callback,
			SharedSessionContractImplementor persistenceContext,
			ExecutionContext executionContext) {
		// Walk the SQL AST.  This produces:
		//		* SQL string
		//		* ParameterBinders
		//		* Returns

		// todo : should also pass in QueryOptions
		// 		as the rendered SQL would depend on first/max results, comment, db-hints, lock-options, entity-graph

		// todo : also need to account for multi-valued param bindings in terms of the generated SQL...

		// todo : actually, why not just pass in the SqlSelectInterpretation rather than SelectQuery (SQL AST)
		//		The only use of the SQL AST here is in building the SqlSelectInterpretation

		// todo : look at making SqlAstSelectInterpreter into an interface and having that be the thing that Dialects can hook into the translation
		//		nice tie-in with Dialect handling follow-on-locking, rendering of the actual SQL, etc


		// todo : I think we also want different SqlTreeExecutor implementors for handling ScrollableResults versus List versus Stream ...

//		if ( interpretNumberOfRowsToProcess( queryOptions ) == 0 ) {
//			return Collections.<R>emptyList();
//		}

		final SqlSelectInterpretation sqlSelectInterpretation = SqlAstSelectInterpreter.interpret(
				sqlTree,
				false,
				persistenceContext.getFactory(),
				queryParameterBindings
		);

		final List<ReturnAssembler> returnAssemblers = new ArrayList<>();
		final List<Initializer> initializers = new ArrayList<>();
		final List<SqlSelectionDescriptor> sqlSelectionDescriptors = new ArrayList<>();
		for ( ResolvedReturn resolvedReturn : sqlSelectInterpretation.getReturns() ) {
			if ( resolvedReturn instanceof InitializerSource ) {
				final Initializer initializer = ( (InitializerSource) resolvedReturn ).getInitializer();
				if ( initializer != null ) {
					initializers.add( initializer );
				}
			}
			returnAssemblers.add( resolvedReturn.getReturnAssembler() );
			for ( SqlSelectionDescriptor descriptor : resolvedReturn.getSqlSelectionDescriptors() ) {
				sqlSelectionDescriptors.add( descriptor );
			}

			// todo : should probably collect Initializers here too
		}

		final JdbcValuesSource jdbcValuesSource = resolveJdbcValuesSource(
				queryOptions,
				persistenceContext,
				sqlSelectInterpretation,
				statementCreator,
				preparedStatementExecutor,
				queryParameterBindings,
				sqlSelectionDescriptors
		);


		/*
		 * Processing options effectively are only used for entity loading.  Here we don't need these values.
		 */
		final ResultSetProcessingOptions processingOptions = new ResultSetProcessingOptions() {
			@Override
			public Object getEffectiveOptionalObject() {
				return null;
			}

			@Override
			public String getEffectiveOptionalEntityName() {
				return null;
			}

			@Override
			public Serializable getEffectiveOptionalId() {
				return null;
			}

			@Override
			public boolean shouldReturnProxies() {
				return true;
			}
		};

		final JdbcValuesSourceProcessingStateStandardImpl jdbcValuesSourceProcessingState = new JdbcValuesSourceProcessingStateStandardImpl(
				jdbcValuesSource,
				queryOptions,
				processingOptions,
				persistenceContext
		);

		final RowReader<T> rowReader = new RowReaderStandardImpl<>(
				returnAssemblers,
				initializers,
				rowTransformer,
				callback
		);
		final RowProcessingState rowProcessingState = new RowProcessingStateStandardImpl(
				jdbcValuesSourceProcessingState,
				queryOptions,
				processingOptions,
				jdbcValuesSource
		);

		try {
			final List<T> results = new ArrayList<T>();
			while ( rowProcessingState.next() ) {
				results.add(
						rowReader.readRow( rowProcessingState, processingOptions )
				);
				rowProcessingState.finishRowProcessing();
			}
			return (R) results;
		}
		catch (SQLException e) {
			throw persistenceContext.getJdbcServices().getSqlExceptionHelper().convert(
					e,
					"Error processing return rows"
			);
		}
		finally {
			rowReader.finishUp( jdbcValuesSourceProcessingState );
			jdbcValuesSourceProcessingState.release();
			jdbcValuesSource.finishUp();
		}
	}

	@SuppressWarnings("unchecked")
	private JdbcValuesSource resolveJdbcValuesSource(
			QueryOptions queryOptions,
			SharedSessionContractImplementor persistenceContext,
			SqlSelectInterpretation sqlSelectInterpretation,
			PreparedStatementCreator statementCreator,
			PreparedStatementExecutor statementExecutor,
			QueryParameterBindings queryParameterBindings,
			List<SqlSelectionDescriptor> sqlSelectionDescriptors) {
		List<Object[]> cachedResults = null;

		final boolean queryCacheEnabled = persistenceContext.getFactory().getSessionFactoryOptions().isQueryCacheEnabled();
		final CacheMode cacheMode = resolveCacheMode( queryOptions.getCacheMode(), persistenceContext );

		if ( queryCacheEnabled && queryOptions.getCacheMode().isGetEnabled() ) {
			log.debugf( "Reading Query result cache data per CacheMode#isGetEnabled [%s]", cacheMode.name() );

			final QueryCache queryCache = persistenceContext.getFactory()
					.getCache()
					.getQueryCache( queryOptions.getResultCacheRegionName() );

			final QueryKey queryResultsCacheKey = null;

			cachedResults = queryCache.get(
					// todo : QueryCache#get takes the `queryResultsCacheKey` see tat discussion above
					queryResultsCacheKey,
					// todo : QueryCache#get also takes a `Type[] returnTypes` argument which ought to be replaced with the Return graph
					// 		(or ResolvedReturn graph)
					null,
					// todo : QueryCache#get also takes a `isNaturalKeyLookup` argument which should go away
					// 		that is no longer the supported way to perform a load-by-naturalId
					false,
					// todo : `querySpaces` and `session` make perfect sense as args, but its odd passing those here
					null,
					null
			);
		}
		else {
			log.debugf( "Skipping reading Query result cache data: cache-enabled = %s, cache-mode = %s",
						queryCacheEnabled,
						cacheMode.name()
			);
		}

		if ( cachedResults == null || cachedResults.isEmpty() ) {
			return new JdbcValuesSourceResultSetImpl(
					persistenceContext,
					sqlSelectInterpretation,
					queryOptions,
					statementCreator,
					statementExecutor,
					queryParameterBindings,
					sqlSelectionDescriptors
			);
		}
		else {
			return new JdbcValuesSourceCacheHit( cachedResults );
		}
	}

	private CacheMode resolveCacheMode(CacheMode cacheMode, SharedSessionContractImplementor persistenceContext) {
		if ( cacheMode != null ) {
			return cacheMode;
		}

		cacheMode = persistenceContext.getCacheMode();
		if ( cacheMode != null ) {
			return cacheMode;
		}

		return CacheMode.NORMAL;
	}

	@Override
	public Object[] executeInsert(
			Object sqlTree,
			PreparedStatementCreator statementCreator,
			QueryOptions queryOptions,
			QueryParameterBindings queryParameterBindings,
			SharedSessionContractImplementor session,
			ExecutionContext executionContext) {
		throw new NotYetImplementedException( "DML execution is not yet implemented" );
	}

	@Override
	public int executeUpdate(
			Object sqlTree,
			PreparedStatementCreator statementCreator,
			QueryOptions queryOptions,
			QueryParameterBindings queryParameterBindings,
			SharedSessionContractImplementor session,
			ExecutionContext executionContext) {
		throw new NotYetImplementedException( "DML execution is not yet implemented" );
	}

	@Override
	public int executeDelete(
			Object sqlTree,
			PreparedStatementCreator statementCreator,
			QueryOptions queryOptions,
			QueryParameterBindings queryParameterBindings,
			SharedSessionContractImplementor session,
			ExecutionContext executionContext) {
		throw new NotYetImplementedException( "DML execution is not yet implemented" );
	}

	@Override
	public <T> Outputs executeCall(
			String callableName,
			QueryOptions queryOptions,
			QueryParameterBindings queryParameterBindings,
			RowTransformer<T> rowTransformer,
			Callback callback,
			SharedSessionContractImplementor session,
			ExecutionContext executionContext) {
		throw new NotYetImplementedException( "Procedure/function call execution is not yet implemented" );
	}
}
