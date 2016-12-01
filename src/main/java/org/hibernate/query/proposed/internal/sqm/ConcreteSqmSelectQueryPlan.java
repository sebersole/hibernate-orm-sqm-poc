/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.proposed.internal.sqm;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.persistence.Tuple;
import javax.persistence.TupleElement;

import org.hibernate.ScrollMode;
import org.hibernate.cfg.NotYetImplementedException;
import org.hibernate.engine.query.spi.EntityGraphQueryHint;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.internal.util.collections.streams.StingArrayCollector;
import org.hibernate.persister.common.spi.OrmTypeExporter;
import org.hibernate.query.proposed.IllegalQueryOperationException;
import org.hibernate.query.proposed.QueryOptions;
import org.hibernate.query.proposed.TupleBuilder;
import org.hibernate.query.proposed.spi.ExecutionContext;
import org.hibernate.query.proposed.spi.QueryParameterBindings;
import org.hibernate.query.proposed.spi.ScrollableResultsImplementor;
import org.hibernate.query.proposed.spi.SelectQueryPlan;
import org.hibernate.sql.ast.SelectQuery;
import org.hibernate.sql.convert.spi.Callback;
import org.hibernate.sql.convert.spi.SqmSelectToSqlAstConverter;
import org.hibernate.sql.exec.internal.PreparedStatementCreatorScrollableForwardOnlyImpl;
import org.hibernate.sql.exec.internal.PreparedStatementCreatorScrollableInsensitiveImpl;
import org.hibernate.sql.exec.internal.PreparedStatementCreatorScrollableSensitiveImpl;
import org.hibernate.sql.exec.internal.PreparedStatementCreatorStandardImpl;
import org.hibernate.sql.exec.internal.PreparedStatementExecutorNormalImpl;
import org.hibernate.sql.exec.internal.PreparedStatementExecutorScrollableImpl;
import org.hibernate.sql.exec.internal.RowTransformerPassThruImpl;
import org.hibernate.sql.exec.internal.RowTransformerSingularReturnImpl;
import org.hibernate.sql.exec.internal.RowTransformerTupleImpl;
import org.hibernate.sql.exec.internal.RowTransformerTupleTransformerAdapter;
import org.hibernate.sql.exec.internal.SqlTreeExecutorImpl;
import org.hibernate.sql.exec.internal.TupleElementImpl;
import org.hibernate.sql.exec.spi.PreparedStatementCreator;
import org.hibernate.sql.exec.spi.RowTransformer;
import org.hibernate.sqm.domain.DomainMetamodel;
import org.hibernate.sqm.query.SqmSelectStatement;
import org.hibernate.sqm.query.select.SqmSelection;

/**
 * @author Steve Ebersole
 */
public class ConcreteSqmSelectQueryPlan<R> implements SelectQueryPlan<R> {
	private final SqmSelectStatement sqm;
	private final DomainMetamodel domainMetamodel;
	private final EntityGraphQueryHint entityGraphHint;
	private final RowTransformer<R> rowTransformer;

	public ConcreteSqmSelectQueryPlan(
			SqmSelectStatement sqm,
			DomainMetamodel domainMetamodel,
			EntityGraphQueryHint entityGraphHint,
			Class<R> resultType,
			QueryOptions queryOptions) {
		this.sqm = sqm;
		this.domainMetamodel = domainMetamodel;
		this.entityGraphHint = entityGraphHint;

		this.rowTransformer = determineRowTransformer( sqm, resultType, queryOptions );
	}

	@SuppressWarnings("unchecked")
	private RowTransformer<R> determineRowTransformer(
			SqmSelectStatement sqm,
			Class<R> resultType,
			QueryOptions queryOptions) {
		if ( resultType == null || resultType.isArray() ) {
			if ( queryOptions.getTupleTransformer() != null ) {
				return makeRowTransformerTupleTransformerAdapter( sqm, queryOptions );
			}
			else {
				return (RowTransformer<R>) RowTransformerPassThruImpl.INSTANCE;
			}
		}

		// NOTE : if we get here, a result-type of some kind (other than Object[].class) was specified

		if ( Tuple.class.isAssignableFrom( resultType ) ) {
			// resultType is Tuple..
			if ( queryOptions.getTupleTransformer() == null ) {
				final List<TupleElement<?>> tupleElementList = new ArrayList<>();
				for ( SqmSelection selection : sqm.getQuerySpec().getSelectClause().getSelections() ) {
					tupleElementList.add(
							new TupleElementImpl(
									( (OrmTypeExporter) selection.getExpression().getExpressionType() ).getOrmType().getReturnedClass(),
									selection.getAlias()
							)
					);
				}
				return (RowTransformer<R>) new RowTransformerTupleImpl( tupleElementList );
//				return (RowTransformer<R>) new RowTransformerTupleImpl(
//						sqm.getQuerySpec().getSelectClause().getSelections()
//								.stream()
//								.map( selection -> (TupleElement<?>) new TupleElementImpl(
//										( (SqmTypeImplementor) selection.getExpression().getExpressionType() ).getOrmType().getReturnedClass(),
//										selection.getAlias()
//								) )
//								.collect( Collectors.toList() )
//				);
			}

			// there can be a TupleTransformer IF it is a TupleBuilder,
			// otherwise this is considered an error
			if ( queryOptions.getTupleTransformer() instanceof TupleBuilder ) {
				return makeRowTransformerTupleTransformerAdapter( sqm, queryOptions );
			}

			throw new IllegalArgumentException(
					"Illegal combination of Tuple resultType and (non-TupleBuilder) TupleTransformer : " +
							queryOptions.getTupleTransformer()
			);
		}

		// NOTE : if we get here we have a resultType of some kind

		if ( queryOptions.getTupleTransformer() != null ) {
			// aside from checking the type parameters for the given TupleTransformer
			// there is not a decent way to verify that the TupleTransformer returns
			// the same type.  We rely on the API here and assume the best
			return makeRowTransformerTupleTransformerAdapter( sqm, queryOptions );
		}
		else if ( sqm.getQuerySpec().getSelectClause().getSelections().size() > 1 ) {
			throw new IllegalQueryOperationException( "Query defined multiple selections, return cannot be typed (other that Object[] or Tuple)" );
		}
		else {
			return (RowTransformer<R>) RowTransformerSingularReturnImpl.INSTANCE;
		}
	}

	@SuppressWarnings("unchecked")
	private RowTransformer makeRowTransformerTupleTransformerAdapter(
			SqmSelectStatement sqm,
			QueryOptions queryOptions) {
		return new RowTransformerTupleTransformerAdapter<>(
				sqm.getQuerySpec().getSelectClause().getSelections()
						.stream()
						.map( SqmSelection::getAlias )
						.collect( StingArrayCollector.INSTANCE ),
				queryOptions.getTupleTransformer()
		);
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<R> performList(
			SharedSessionContractImplementor persistenceContext,
			ExecutionContext executionContext,
			QueryOptions queryOptions,
			QueryParameterBindings inputParameterBindings) {
		verifyQueryIsSelect();

		final Callback callback = new Callback() {};

		// todo : SelectStatementInterpreter needs to account for the EntityGraph hint
		final SelectQuery sqlTree = SqmSelectToSqlAstConverter.interpret(
				sqm,
				persistenceContext.getFactory(),
				domainMetamodel,
				queryOptions,
				callback
		);
		return (List<R>) new SqlTreeExecutorImpl().executeSelect(
				sqlTree,
				PreparedStatementCreatorStandardImpl.INSTANCE,
				PreparedStatementExecutorNormalImpl.INSTANCE,
				queryOptions,
				inputParameterBindings,
				rowTransformer,
				callback,
				persistenceContext,
				executionContext
		);
	}

	private void verifyQueryIsSelect() {
		if ( !SqmSelectStatement.class.isInstance( sqm ) ) {
			throw new IllegalQueryOperationException(
					"Query is not a SELECT statement [" + sqm.getClass().getSimpleName() + "]"
			);
		}
	}

	@Override
	public Iterator<R> performIterate(
			SharedSessionContractImplementor persistenceContext,
			ExecutionContext executionContext,
			QueryOptions queryOptions,
			QueryParameterBindings inputParameterBindings) {
		verifyQueryIsSelect();

		// todo : implement
		throw new NotYetImplementedException( "Query#iterate not yet implemented" );
	}

	@Override
	@SuppressWarnings("unchecked")
	public ScrollableResultsImplementor performScroll(
			SharedSessionContractImplementor persistenceContext,
			ExecutionContext executionContext,
			QueryOptions queryOptions,
			QueryParameterBindings inputParameterBindings,
			ScrollMode scrollMode) {
		verifyQueryIsSelect();

		final Callback callback = new Callback() {};

		// todo : SelectStatementInterpreter needs to account for the EntityGraph hint
		final SelectQuery sqlTree = SqmSelectToSqlAstConverter.interpret(
				sqm,
				persistenceContext.getFactory(),
				domainMetamodel,
				queryOptions,
				callback
		);

		final PreparedStatementCreator creator;
		if ( scrollMode == ScrollMode.FORWARD_ONLY ) {
			creator = PreparedStatementCreatorScrollableForwardOnlyImpl.INSTANCE;
		}
		else if ( scrollMode == ScrollMode.SCROLL_SENSITIVE ) {
			creator = PreparedStatementCreatorScrollableSensitiveImpl.INSTANCE;
		}
		else {
			creator = PreparedStatementCreatorScrollableInsensitiveImpl.INSTANCE;
		}

		return (ScrollableResultsImplementor) new SqlTreeExecutorImpl().executeSelect(
				sqlTree,
				creator,
				PreparedStatementExecutorScrollableImpl.INSTANCE,
				queryOptions,
				inputParameterBindings,
				rowTransformer,
				callback,
				persistenceContext,
				executionContext
		);
	}
}
