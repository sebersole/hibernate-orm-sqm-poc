/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.exec.results.process.internal;

import java.sql.ResultSet;
import java.util.List;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.query.proposed.QueryOptions;
import org.hibernate.sql.exec.results.process.internal.caching.QueryCacheDataAccessImplementor;
import org.hibernate.sql.exec.results.process.spi.ResultSetProcessingOptions;
import org.hibernate.sql.exec.results.process.spi.ResultSetProcessingState;
import org.hibernate.sql.exec.results.process.spi.RowProcessingState;
import org.hibernate.sql.exec.results.spi.ResolvedReturn;

/**
 * @author Steve Ebersole
 */
public class ResultSetProcessingStateStandardImpl implements ResultSetProcessingState {
	private final ResultSet resultSet;
	private final SharedSessionContractImplementor session;

	private RowProcessingState currentRowState;

	public ResultSetProcessingStateStandardImpl(
			ResultSet resultSet,
			QueryCacheDataAccessImplementor queryCacheDataAccess,
			QueryOptions queryOptions,
			ResultSetProcessingOptions processingOptions,
			List<ResolvedReturn> returns,
			SharedSessionContractImplementor session) {
		this.resultSet = resultSet;
		this.session = session;

		currentRowState = new RowProcessingStateStandardImpl( this, returns, queryOptions, processingOptions, queryCacheDataAccess );
	}

	@Override
	public ResultSet getResultSet() {
		return resultSet;
	}

	@Override
	public SharedSessionContractImplementor getSession() {
		return session;
	}

	@Override
	public RowProcessingState getCurrentRowProcessingState() {
		return currentRowState;
	}

	@Override
	public void finishResultSetProcessing() {
		// for now, nothing to do...
	}

	@Override
	public void release() {
		// for now, nothing to do...
	}
}
