/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.exec.results.process.internal;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.engine.spi.EntityKey;
import org.hibernate.loader.plan.spi.EntityFetch;
import org.hibernate.loader.plan.spi.Fetch;
import org.hibernate.persister.entity.spi.EntityReference;
import org.hibernate.query.proposed.QueryOptions;
import org.hibernate.sql.ast.expression.domain.DomainReferenceExpression;
import org.hibernate.sql.ast.select.SqlSelectionDescriptor;
import org.hibernate.sql.convert.ConversionException;
import org.hibernate.sql.exec.results.process.internal.caching.QueryCacheDataAccessImplementor;
import org.hibernate.sql.exec.results.process.spi.EntityReferenceProcessingState;
import org.hibernate.sql.exec.results.process.spi.ResultSetProcessingOptions;
import org.hibernate.sql.exec.results.process.spi.ResultSetProcessingState;
import org.hibernate.sql.exec.results.process.spi.RowProcessingState;
import org.hibernate.sql.exec.results.spi.ResolvedReturn;

/**
 * @author Steve Ebersole
 */
public class RowProcessingStateStandardImpl implements RowProcessingState {
	private final ResultSetProcessingStateStandardImpl resultSetProcessingState;
	private final List<ResolvedReturn> returns;
	private final QueryOptions queryOptions;
	private final ResultSetProcessingOptions resultSetProcessingOptions;

	private final QueryCacheDataAccessImplementor queryCacheDataAccess;

	private List<SqlSelectionDescriptor> sqlSelectionDescriptors = new ArrayList<>();
	private Object[] currentRowJdbcValues;

	public RowProcessingStateStandardImpl(
			ResultSetProcessingStateStandardImpl resultSetProcessingState,
			List<ResolvedReturn> returns,
			QueryOptions queryOptions,
			ResultSetProcessingOptions resultSetProcessingOptions,
			QueryCacheDataAccessImplementor queryCacheDataAccess) {
		this.resultSetProcessingState = resultSetProcessingState;
		this.returns = returns;
		this.queryOptions = queryOptions;
		this.resultSetProcessingOptions = resultSetProcessingOptions;
		this.queryCacheDataAccess = queryCacheDataAccess;

		for ( ResolvedReturn resolvedReturn : returns ) {
			sqlSelectionDescriptors.addAll( resolvedReturn.getSqlSelectionDescriptors() );
		}
	}

	@Override
	public ResultSetProcessingState getResultSetProcessingState() {
		return resultSetProcessingState;
	}

	@Override
	public Object[] getJdbcValues() {
		if ( currentRowJdbcValues == null ) {
				currentRowJdbcValues = readJdbcValues();
		}
		return currentRowJdbcValues;
	}

	private Object[] readJdbcValues() {
		Object[] jdbcValues = queryCacheDataAccess.getQueryCacheGetManager().getCurrentRow();
		if ( jdbcValues == null ) {
			// read them from ResultSet
			final int size = sqlSelectionDescriptors.size();
			jdbcValues = new Object[ size ];
			for ( int i = 0; i < size; i++ ) {
				final SqlSelectionDescriptor descriptor = sqlSelectionDescriptors.get( i );
				try {
					jdbcValues[i] = readJdbcValue( descriptor );
				}
				catch (SQLException e) {
					throw resultSetProcessingState.getSession().getJdbcServices()
							.getSqlExceptionHelper()
							.convert( e, "Extracting JDBC values for SQL selection [" + descriptor.getSqlSelectable().toString() + "]" );
				}
			}

			// add them to cache collector
			queryCacheDataAccess.getQueryCachePutManager().registerJdbcRow( jdbcValues );
		}
		else {
			if ( jdbcValues.length != sqlSelectionDescriptors.size() ) {
				throw new ConversionException(
						"Number of cached JDBC values [" +
								jdbcValues.length +
								" did not match the number of SqlSelectionDescriptors [" +
								sqlSelectionDescriptors.size() + "]"
				);
			}
		}

		return jdbcValues;
	}

	private Object readJdbcValue(SqlSelectionDescriptor sqlSelectionDescriptor) throws SQLException {
		return sqlSelectionDescriptor.getSqlSelectable().getSqlSelectionReader().read(
				this,
				resultSetProcessingOptions,
				sqlSelectionDescriptor.getIndex()
		);
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
		queryCacheDataAccess.getQueryCacheGetManager().next();
	}
}
