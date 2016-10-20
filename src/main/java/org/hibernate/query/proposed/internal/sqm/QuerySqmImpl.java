/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.proposed.internal.sqm;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.persistence.Parameter;
import javax.persistence.PersistenceException;

import org.hibernate.LockMode;
import org.hibernate.ScrollMode;
import org.hibernate.cfg.NotYetImplementedException;
import org.hibernate.engine.query.spi.EntityGraphQueryHint;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.jpa.graph.internal.EntityGraphImpl;
import org.hibernate.query.proposed.ParameterMetadata;
import org.hibernate.query.proposed.Query;
import org.hibernate.query.proposed.QueryOptions;
import org.hibernate.query.proposed.QueryParameter;
import org.hibernate.query.proposed.internal.ParameterMetadataImpl;
import org.hibernate.query.proposed.internal.QueryParameterBindingsImpl;
import org.hibernate.query.proposed.internal.QueryParameterNamedImpl;
import org.hibernate.query.proposed.internal.AbstractQuery;
import org.hibernate.query.proposed.internal.QueryOptionsImpl;
import org.hibernate.query.proposed.internal.QueryParameterPositionalImpl;
import org.hibernate.query.proposed.spi.ExecutionContext;
import org.hibernate.query.proposed.spi.MutableQueryOptions;
import org.hibernate.query.proposed.spi.NonSelectQueryPlan;
import org.hibernate.query.proposed.spi.QueryInterpretations;
import org.hibernate.query.proposed.spi.QueryParameterBindings;
import org.hibernate.query.proposed.spi.QueryProducerImplementor;
import org.hibernate.query.proposed.spi.ScrollableResultsImplementor;
import org.hibernate.query.proposed.spi.SelectQueryPlan;
import org.hibernate.sqm.QuerySplitter;
import org.hibernate.sqm.domain.DomainMetamodel;
import org.hibernate.sqm.query.SqmSelectStatement;
import org.hibernate.sqm.query.SqmStatement;
import org.hibernate.sqm.query.SqmStatementNonSelect;

/**
 * {@link Query} implementation based on an SQM
 *
 * @author Steve Ebersole
 */
public class QuerySqmImpl<R> extends AbstractQuery<R> {
	private final String sourceQueryString;
	private final SqmStatement sqmStatement;
	private final Class resultType;

	private final ParameterMetadataImpl parameterMetadata;
	private final QueryParameterBindingsImpl parameterBindings;

	private final QueryOptionsImpl queryOptions = new QueryOptionsImpl();
	private final SharedSessionContractImplementor persistenceContext;
	private final DomainMetamodel domainMetamodel;

	private EntityGraphQueryHint entityGraphQueryHint;

	public QuerySqmImpl(
			String sourceQueryString,
			SqmStatement sqmStatement,
			Class resultType,
			SharedSessionContractImplementor persistenceContext,
			DomainMetamodel domainMetamodel,
			QueryProducerImplementor producer,
			ExecutionContext executionContext) {
		super( producer, executionContext );
		this.persistenceContext = persistenceContext;
		this.domainMetamodel = domainMetamodel;

		if ( resultType != null ) {
			if ( sqmStatement instanceof SqmStatementNonSelect ) {
				throw new IllegalArgumentException( "Non-select queries cannot be typed" );
			}
		}

		this.sourceQueryString = sourceQueryString;
		this.sqmStatement = sqmStatement;
		this.resultType = resultType;

		this.parameterMetadata = buildParameterMetadata( sqmStatement );
		this.parameterBindings = QueryParameterBindingsImpl.from( parameterMetadata, executionContext );
	}

	private static ParameterMetadataImpl buildParameterMetadata(SqmStatement sqm) {
		Map<String, QueryParameter> namedQueryParameters = null;
		Map<Integer, QueryParameter> positionalQueryParameters = null;

		for ( org.hibernate.sqm.query.Parameter parameter : sqm.getQueryParameters() ) {
			if ( parameter.getName() != null ) {
				if ( namedQueryParameters == null ) {
					namedQueryParameters = new HashMap<>();
				}
				namedQueryParameters.put(
						parameter.getName(),
						QueryParameterNamedImpl.fromSqm( parameter )
				);
			}
			else if ( parameter.getPosition() != null ) {
				if ( positionalQueryParameters == null ) {
					positionalQueryParameters = new HashMap<>();
				}
				positionalQueryParameters.put(
						parameter.getPosition(),
						QueryParameterPositionalImpl.fromSqm( parameter )
				);
			}
		}

		return new ParameterMetadataImpl( namedQueryParameters, positionalQueryParameters );
	}

	private boolean isSelect() {
		return sqmStatement instanceof SqmSelectStatement;
	}

	@Override
	public String getQueryString() {
		return sourceQueryString;
	}

	public SqmStatement getSqmStatement() {
		return sqmStatement;
	}

	@SuppressWarnings("unchecked")
	public Class<R> getResultType() {
		return resultType;
	}

	@Override
	public MutableQueryOptions getQueryOptions() {
		return queryOptions;
	}

	public EntityGraphQueryHint getEntityGraphHint() {
		return entityGraphQueryHint;
	}

	@Override
	public ParameterMetadata getParameterMetadata() {
		return parameterMetadata;
	}

	public QueryParameterBindings getQueryParameterBindings() {
		return parameterBindings;
	}

	@Override
	public Set<Parameter<?>> getParameters() {
		return parameterMetadata.collectAllParametersJpa();
	}

	@Override
	protected QueryParameterBindings queryParameterBindings() {
		return parameterBindings;
	}

	@Override
	protected boolean canApplyAliasSpecificLockModes() {
		return isSelect();
	}

	@Override
	protected void verifySettingLockMode() {
		if ( !isSelect() ) {
			throw new IllegalStateException( "Illegal attempt to set lock mode on a non-SELECT query" );
		}
	}

	@Override
	protected void verifySettingAliasSpecificLockModes() {
		// todo : add a specific Dialect check as well? - if not supported, maybe that can trigger follow-on locks?
		verifySettingLockMode();
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T unwrap(Class<T> cls) {
		if ( cls.isInstance( this ) ) {
			return (T) this;
		}

		if ( cls.isInstance( parameterMetadata ) ) {
			return (T) parameterMetadata;
		}

		if ( cls.isInstance( parameterBindings ) ) {
			return (T) parameterBindings;
		}

		if ( cls.isInstance( sqmStatement ) ) {
			return (T) sqmStatement;
		}

		if ( cls.isInstance( queryOptions ) ) {
			return (T) queryOptions;
		}

		if ( cls.isInstance( entityGraphQueryHint ) ) {
			return (T) entityGraphQueryHint;
		}

		throw new PersistenceException( "Unrecognized unwrap type [" + cls.getName() + "]" );
	}

	protected boolean applyNativeQueryLockMode(Object value) {
		throw new IllegalStateException(
				"Illegal attempt to set lock mode on non-native query via hint; use Query#setLockMode instead"
		);
	}

	@Override
	protected void applyEntityGraphQueryHint(String hintName, EntityGraphImpl entityGraph) {
		this.entityGraphQueryHint = new EntityGraphQueryHint( hintName, entityGraph );
	}


	@Override
	protected void collectHints(Map<String, Object> hints) {
		super.collectHints( hints );

		if ( entityGraphQueryHint != null ) {
			hints.put( entityGraphQueryHint.getHintName(), entityGraphQueryHint.getOriginEntityGraph() );
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	protected List<R> doList() {
		getExecutionContext().prepareForQueryExecution( requiresTxn( getLockOptions().findGreatestLockMode() ) );

		return resolveSelectQueryPlan().performList(
				persistenceContext,
				getExecutionContext(),
				getQueryOptions(),
				getQueryParameterBindings()
		);
	}

	private boolean requiresTxn(LockMode lockMode) {
		return lockMode != null && lockMode.greaterThan( LockMode.READ );
	}

	@SuppressWarnings("unchecked")
	private SelectQueryPlan<R> resolveSelectQueryPlan() {
		// resolve (or make) the QueryPlan.  This QueryPlan might be an
		// aggregation of multiple plans.  QueryPlans can be cached, except
		// for in certain circumstances, the determination of which occurs in
		// SqmInterpretationsKey#generateFrom - if SqmInterpretationsKey#generateFrom
		// returns null the query is not cacheable

		SelectQueryPlan<R> queryPlan = null;

		final QueryInterpretations.Key cacheKey = SqmInterpretationsKey.generateFrom( this );
		if ( cacheKey != null ) {
			queryPlan = getExecutionContext().getQueryInterpretations().getSelectQueryPlan( cacheKey );
		}

		if ( queryPlan == null ) {
			queryPlan = buildSelectQueryPlan();
			if ( cacheKey != null ) {
				getExecutionContext().getQueryInterpretations().cacheSelectQueryPlan( cacheKey, queryPlan );
			}
		}

		return queryPlan;
	}

	private SelectQueryPlan<R> buildSelectQueryPlan() {
		final SqmSelectStatement[] concreteSqmStatements = QuerySplitter.split( (SqmSelectStatement) getSqmStatement() );
		if ( concreteSqmStatements.length > 1 ) {
			return buildAggregatedSelectQueryPlan( concreteSqmStatements );
		}
		else {
			return buildConcreteSelectQueryPlan(
					concreteSqmStatements[0],
					getResultType(),
					getEntityGraphHint(),
					getQueryOptions()
			);
		}
	}

	@SuppressWarnings("unchecked")
	private SelectQueryPlan<R> buildAggregatedSelectQueryPlan(SqmSelectStatement[] concreteSqmStatements) {
		final SelectQueryPlan[] aggregatedQueryPlans = new SelectQueryPlan[ concreteSqmStatements.length ];

		// todo : we want to make sure that certain thing (ResultListTransformer, etc) only get applied at the aggregator-level

		for ( int i = 0, x = concreteSqmStatements.length; i < x; i++ ) {
			aggregatedQueryPlans[i] = buildConcreteSelectQueryPlan(
					concreteSqmStatements[i],
					getResultType(),
					getEntityGraphHint(),
					getQueryOptions()
			);
		}

		return new AggregatedSelectQueryPlanImpl( aggregatedQueryPlans );
	}

	private SelectQueryPlan<R> buildConcreteSelectQueryPlan(
			SqmSelectStatement concreteSqmStatement,
			Class<R> resultType,
			EntityGraphQueryHint entityGraphHint,
			QueryOptions queryOptions) {
		return new ConcreteSqmSelectQueryPlan<>(
				concreteSqmStatement,
				domainMetamodel,
				entityGraphHint,
				resultType,
				queryOptions
		);
	}

	@Override
	@SuppressWarnings("unchecked")
	protected Iterator<R> doIterate() {
		getExecutionContext().prepareForQueryExecution( requiresTxn( getLockOptions().findGreatestLockMode() ) );

		return resolveSelectQueryPlan().performIterate(
				persistenceContext,
				getExecutionContext(),
				getQueryOptions(),
				getQueryParameterBindings()
		);
	}

	@Override
	protected ScrollableResultsImplementor doScroll(ScrollMode scrollMode) {
		getExecutionContext().prepareForQueryExecution( requiresTxn( getLockOptions().findGreatestLockMode() ) );

		return resolveSelectQueryPlan().performScroll(
				persistenceContext,
				getExecutionContext(),
				getQueryOptions(),
				getQueryParameterBindings(),
				scrollMode
		);
	}

	@Override
	protected int doExecuteUpdate() {
		getExecutionContext().prepareForQueryExecution( true );

		return resolveNonSelectQueryPlan().executeUpdate(
				persistenceContext,
				getExecutionContext(),
				getQueryOptions(),
				getQueryParameterBindings()
		);
	}

	private NonSelectQueryPlan resolveNonSelectQueryPlan() {
		// resolve (or make) the QueryPlan.  This QueryPlan might be an
		// aggregation of multiple plans.  QueryPlans can be cached, unless either:
		//		1) the query declared multi-valued parameter(s)
		//		2) an EntityGraph hint is attached.

		NonSelectQueryPlan queryPlan = null;

		final QueryInterpretations.Key cacheKey = SqmInterpretationsKey.generateFrom( this );
		if ( cacheKey != null ) {
			queryPlan = getExecutionContext().getQueryInterpretations().getNonSelectQueryPlan( cacheKey );
		}

		if ( queryPlan == null ) {
			queryPlan = buildNonSelectQueryPlan();
			if ( cacheKey != null ) {
				getExecutionContext().getQueryInterpretations().cacheNonSelectQueryPlan( cacheKey, queryPlan );
			}
		}

		return queryPlan;
	}

	private NonSelectQueryPlan buildNonSelectQueryPlan() {
		throw new NotYetImplementedException( "Query#executeUpdate not yet implemented" );
	}
}
