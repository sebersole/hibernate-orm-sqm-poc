/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.exec.results.process.internal;

import java.sql.SQLException;

import org.hibernate.engine.spi.EntityKey;
import org.hibernate.loader.plan.spi.EntityFetch;
import org.hibernate.loader.plan.spi.Fetch;
import org.hibernate.persister.entity.spi.EntityReference;
import org.hibernate.query.proposed.QueryOptions;
import org.hibernate.sql.ast.expression.domain.DomainReferenceExpression;
import org.hibernate.sql.exec.results.process.internal.values.JdbcValuesSource;
import org.hibernate.sql.exec.results.process.spi.EntityReferenceProcessingState;
import org.hibernate.sql.exec.results.process.spi.JdbcValuesSourceProcessingState;
import org.hibernate.sql.exec.results.process.spi.ResultSetProcessingOptions;
import org.hibernate.sql.exec.results.process.spi.RowProcessingState;

/**
 * @author Steve Ebersole
 */
public class RowProcessingStateStandardImpl implements RowProcessingState {
	private final JdbcValuesSourceProcessingStateStandardImpl resultSetProcessingState;
	private final QueryOptions queryOptions;
	private final ResultSetProcessingOptions resultSetProcessingOptions;

	private final JdbcValuesSource jdbcValuesSource;
	private Object[] currentRowJdbcValues;

	public RowProcessingStateStandardImpl(
			JdbcValuesSourceProcessingStateStandardImpl resultSetProcessingState,
			QueryOptions queryOptions,
			ResultSetProcessingOptions resultSetProcessingOptions,
			JdbcValuesSource jdbcValuesSource) {
		this.resultSetProcessingState = resultSetProcessingState;
		this.queryOptions = queryOptions;
		this.resultSetProcessingOptions = resultSetProcessingOptions;
		this.jdbcValuesSource = jdbcValuesSource;
	}

	@Override
	public JdbcValuesSourceProcessingState getJdbcValuesSourceProcessingState() {
		return resultSetProcessingState;
	}

	@Override
	public boolean next() throws SQLException {
		if ( jdbcValuesSource.next( this, resultSetProcessingOptions ) ) {
			currentRowJdbcValues = jdbcValuesSource.getCurrentRowJdbcValues();
			return true;
		}
		else {
			currentRowJdbcValues = null;
			return false;
		}
	}

	@Override
	public Object[] getJdbcValues() throws SQLException {
		return currentRowJdbcValues;
	}

	@Override
	public void registerNonExists(EntityFetch fetch) {
	}

	@Override
	public void registerHydratedEntity(EntityReference entityReference, EntityKey entityKey, Object entityInstance) {
	}

	@Override
	public EntityReferenceProcessingState getProcessingState(DomainReferenceExpression expression) {
		return null;
	}

//	@Override
//	public EntityReferenceProcessingState getProcessingState(EntityReference entityReference) {
//		return null;
//	}

	@Override
	public EntityReferenceProcessingState getOwnerProcessingState(Fetch fetch) {
		return null;
	}

	@Override
	public void finishRowProcessing() {
		currentRowJdbcValues = null;
	}
}
