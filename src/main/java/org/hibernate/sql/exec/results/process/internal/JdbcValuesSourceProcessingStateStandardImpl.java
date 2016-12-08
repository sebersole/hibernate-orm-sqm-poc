/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.exec.results.process.internal;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.query.proposed.QueryOptions;
import org.hibernate.sql.exec.results.process.internal.values.JdbcValuesSource;
import org.hibernate.sql.exec.results.process.spi.JdbcValuesSourceProcessingState;
import org.hibernate.sql.exec.results.process.spi.ResultSetProcessingOptions;

/**
 * @author Steve Ebersole
 */
public class JdbcValuesSourceProcessingStateStandardImpl implements JdbcValuesSourceProcessingState {
	private final SharedSessionContractImplementor persistenceContext;

	private final JdbcValuesSource jdbcValuesSource;

	public JdbcValuesSourceProcessingStateStandardImpl(
			JdbcValuesSource jdbcValuesSource,
			QueryOptions queryOptions,
			ResultSetProcessingOptions processingOptions,
			SharedSessionContractImplementor persistenceContext) {
		this.jdbcValuesSource = jdbcValuesSource;
		this.persistenceContext = persistenceContext;
	}

	@Override
	public JdbcValuesSource getJdbcValuesSource() {
		return jdbcValuesSource;
	}

	@Override
	public SharedSessionContractImplementor getPersistenceContext() {
		return persistenceContext;
	}

	@Override
	public void release() {
		// for now, nothing to do...
	}
}
