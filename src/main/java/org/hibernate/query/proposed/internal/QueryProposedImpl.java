/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.proposed.internal;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.persistence.TemporalType;
import javax.persistence.Tuple;
import javax.persistence.TupleElement;

import org.hibernate.LockMode;
import org.hibernate.LockOptions;
import org.hibernate.QueryException;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.persister.common.spi.SqmTypeImplementor;
import org.hibernate.query.proposed.IllegalQueryOperationException;
import org.hibernate.query.proposed.Query;
import org.hibernate.query.proposed.QueryParameter;
import org.hibernate.query.proposed.spi.QueryParameterBindings;
import org.hibernate.query.proposed.spi.QueryPlan;
import org.hibernate.sql.ast.SelectQuery;
import org.hibernate.sql.exec.internal.PreparedStatementCreatorScrollableForwardOnlyImpl;
import org.hibernate.sql.exec.internal.PreparedStatementCreatorScrollableInsensitiveImpl;
import org.hibernate.sql.exec.internal.PreparedStatementCreatorScrollableSensitiveImpl;
import org.hibernate.sql.exec.internal.PreparedStatementCreatorStandardImpl;
import org.hibernate.sql.exec.internal.PreparedStatementExecutorNormalImpl;
import org.hibernate.sql.exec.internal.PreparedStatementExecutorScrollableImpl;
import org.hibernate.sql.exec.internal.SqlTreeExecutorImpl;
import org.hibernate.sql.exec.spi.PreparedStatementCreator;
import org.hibernate.sql.exec.spi.RowTransformer;
import org.hibernate.sql.gen.Callback;
import org.hibernate.sql.gen.NotYetImplementedException;
import org.hibernate.sqm.ConsumerContext;
import org.hibernate.sqm.query.NonSelectStatement;
import org.hibernate.sqm.query.SelectStatement;
import org.hibernate.sqm.query.Statement;
import org.hibernate.sqm.query.select.Selection;
import org.hibernate.type.Type;

import org.jboss.logging.Logger;

import static org.hibernate.sql.ast.SelectStatementInterpreter.interpret;

/**
 * @author Steve Ebersole
 */
public class QueryProposedImpl<R> extends AbstractBasicQueryContract<Query> implements Query<Query,R> {
	private static final Logger log = Logger.getLogger( QueryProposedImpl.class );

	private final String queryString;
	private final SessionImplementor session;

	private final Statement sqm;
	private final RowTransformer<R> implicitRowTransformer;

	private final QueryParameterBindings queryParameterBindings;

	// todo : these ctor contracts will change as we integrate this into ORM
	// 	ultimately the SessionFactory will implement (or provide access to) ConsumerContext

	public QueryProposedImpl(
			String queryString,
			SessionImplementor session,
			ConsumerContext consumerContext) {
		this( queryString, null, session, consumerContext );
	}

	public QueryProposedImpl(
			String queryString,
			Class<R> resultType,
			SessionImplementor session,
			ConsumerContext consumerContext) {
		this.queryString = queryString;
		this.session = session;

		// todo : QueryPlanCache
		//		for now, just always rebuild the QueryPlan
		final QueryPlan queryPlan = QueryPlanImpl.prepare( queryString, consumerContext );

		this.sqm = queryPlan.getSqm();
		this.queryParameterBindings = new QueryParameterBindings( queryPlan.getQueryParameters() );

		if ( sqm.getType() == Statement.Type.SELECT ) {
			this.implicitRowTransformer = determineRowTransformer( (SelectStatement) sqm, resultType );
		}
		else {
			this.implicitRowTransformer = null;
		}
	}

	@SuppressWarnings("unchecked")
	private RowTransformer<R> determineRowTransformer(SelectStatement sqm, Class<R> resultType) {
		if ( resultType != null ) {
			// an explicit return Type was requested
			if ( resultType.isArray() ) {
				return (RowTransformer<R>) RowTransformerPassThruImpl.INSTANCE;
			}

			if ( Tuple.class.isAssignableFrom( resultType ) ) {
				List<TupleElement<?>> tupleElements = new ArrayList<>();
				int i = 0;
				for ( Selection selection : sqm.getQuerySpec().getSelectClause().getSelections() ) {
					tupleElements.add(
							new RowTransformerTupleImpl.HqlTupleElementImpl(
									( (SqmTypeImplementor) selection.getExpression().getExpressionType() ).getOrmType().getReturnedClass(),
									selection.getAlias()
							)
					);
				}

				return (RowTransformer<R>) new RowTransformerTupleImpl( tupleElements );
			}

			if ( sqm.getQuerySpec().getSelectClause().getSelections().size() > 1 ) {
				throw new IllegalQueryOperationException( "Query defined multiple selections, return cannot be typed (other that Object[] or Tuple)" );
			}
			else {
				return (RowTransformer<R>) RowTransformerSingularReturnImpl.INSTANCE;
			}
		}

		// otherwise just pass through the result row (as if Object[] were specified as return type)
		return (RowTransformer<R>) RowTransformerPassThruImpl.INSTANCE;
	}

	@Override
	protected boolean allowSelectOptions() {
		return sqm.getType() == Statement.Type.SELECT;
	}

	@Override
	public String getQueryString() {
		return queryString;
	}

	@Override
	public Integer getFirstResult() {
		return queryOptions().getLimit().getFirstRow();
	}

	@Override
	public Query setFirstResult(int firstResult) {
		if ( !allowSelectOptions() ) {
			log.debug( "Attempt to set first-result option on non-select (or native) query" );
		}
		queryOptions().getLimit().setFirstRow( firstResult );
		return this;
	}

	@Override
	public Integer getMaxResults() {
		if ( !allowSelectOptions() ) {
			log.debug( "Attempt to set max-results option on non-select (or native) query" );
		}
		return queryOptions().getLimit().getMaxRows();
	}

	@Override
	public Query setMaxResults(int maxResults) {
		queryOptions().getLimit().setMaxRows( maxResults );
		return this;
	}

	@Override
	public LockOptions getLockOptions() {
		return queryOptions().getLockOptions();
	}

	@Override
	@SuppressWarnings("unchecked")
	public Query setLockOptions(LockOptions lockOptions) {
		if ( !allowSelectOptions() ) {
			log.debug( "Attempt to set lock-options option on non-select (or native) query" );
		}
		queryOptions().getLockOptions().setLockMode( lockOptions.getLockMode() );
		queryOptions().getLockOptions().setScope( lockOptions.getScope() );
		queryOptions().getLockOptions().setTimeOut( lockOptions.getTimeOut() );
		Iterator<Map.Entry<String,LockMode>> aliasLockEntries = lockOptions.getAliasLockIterator();
		while ( aliasLockEntries.hasNext() ) {
			final Map.Entry<String,LockMode> aliasLockModeEntry = aliasLockEntries.next();
			queryOptions().getLockOptions().setAliasSpecificLockMode(
					aliasLockModeEntry.getKey(),
					aliasLockModeEntry.getValue()
			);
		}
		return this;
	}

	@Override
	public Query setLockMode(String alias, LockMode lockMode) {
		if ( !allowSelectOptions() ) {
			log.debug( "Attempt to set lock-options option on non-select (or native) query" );
		}
		queryOptions().getLockOptions().setAliasSpecificLockMode( alias, lockMode );
		return this;
	}

	@Override
	public String getComment() {
		return queryOptions().getComment();
	}

	@Override
	public Query setComment(String comment) {
		queryOptions().setComment( comment );
		return this;
	}

	@Override
	public Query addQueryHint(String hint) {
		queryOptions().addSqlHint( hint );
		return this;
	}

	@Override
	public Query setParameter(int position, Object val) {
		queryParameterBindings.getPositionalParameterBinding( position ).setBindValue( val );
		return this;
	}

	@Override
	public Query setParameter(int position, Object val, Type type) {
		queryParameterBindings.getPositionalParameterBinding( position ).setBindValue( val, type );
		return this;
	}

	@Override
	public Query setParameter(int position, Object val, TemporalType temporalType) {
		queryParameterBindings.getPositionalParameterBinding( position ).setBindValue( val, temporalType );
		return this;
	}

	@Override
	public Query setParameter(String name, Object val) {
		queryParameterBindings.getNamedParameterBinding( name ).setBindValue( val );
		return null;
	}

	@Override
	public Query setParameter(String name, Object val, Type type) {
		queryParameterBindings.getNamedParameterBinding( name ).setBindValue( val, type );
		return null;
	}

	@Override
	public Query setParameter(String name, Object val, TemporalType temporalType) {
		queryParameterBindings.getNamedParameterBinding( name ).setBindValue( val, temporalType );
		return this;
	}

	@Override
	public Query setParameter(QueryParameter parameter, Object val) {
		queryParameterBindings.getParameterBinding( parameter ).setBindValue( val );
		return this;
	}

	@Override
	public Query setParameter(QueryParameter parameter, Object val, Type type) {
		queryParameterBindings.getParameterBinding( parameter ).setBindValue( val, type );
		return this;
	}

	@Override
	public Query setParameter(QueryParameter parameter, Object val, TemporalType temporalType) {
		queryParameterBindings.getParameterBinding( parameter ).setBindValue( val, temporalType );
		return this;
	}

	@Override
	public Iterator<R> iterate() {
		verifyQueryIsSelect();

		// todo : implement
		throw new NotYetImplementedException( "Query#iterate not yet implemented" );
	}

	private void verifyQueryIsSelect() {
		if ( !SelectStatement.class.isInstance( sqm ) ) {
			assert sqm.getType() != Statement.Type.SELECT;
			throw new IllegalQueryOperationException(
					"Query is not a SELECT statement [" + sqm.getType().name() + "] : " + queryString
			);
		}
	}

	@Override
	public ScrollableResults scroll() {
		return scroll( session.getFactory().getJdbcServices().getJdbcEnvironment().getDialect().defaultScrollMode() );
	}

	@Override
	@SuppressWarnings("unchecked")
	public ScrollableResults scroll(ScrollMode scrollMode) {
		verifyQueryIsSelect();

		final SelectQuery sqlTree = interpret( (SelectStatement) sqm, queryOptions(), new Callback() {} );
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
		return (ScrollableResults) new SqlTreeExecutorImpl().executeSelect(
				sqlTree,
				creator,
				PreparedStatementExecutorScrollableImpl.INSTANCE,
				queryOptions(),
				queryParameterBindings,
				implicitRowTransformer,
				new Callback() {},
				session
		);
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<R> list() {
		verifyQueryIsSelect();

		final SelectQuery sqlTree = interpret( (SelectStatement) sqm, queryOptions(), new Callback() {} );
		return (List<R>) new SqlTreeExecutorImpl().executeSelect(
				sqlTree,
				PreparedStatementCreatorStandardImpl.INSTANCE,
				PreparedStatementExecutorNormalImpl.INSTANCE,
				queryOptions(),
				queryParameterBindings,
				implicitRowTransformer,
				new Callback() {},
				session
		);
	}

	@Override
	@SuppressWarnings("unchecked")
	public R uniqueResult() {
		verifyQueryIsSelect();

		final List results = list();
		if ( list().size() != 1 ) {
			throw new QueryException( "Query returned more than one result" );
		}

		return (R) results.get( 0 );
	}

	@Override
	public int executeUpdate() {
		if ( !NonSelectStatement.class.isInstance( sqm ) ) {
			assert sqm.getType() == Statement.Type.SELECT;
			throw new IllegalQueryOperationException(
					"Query is not a DML statement [" + sqm.getType().name() + "] : " + queryString
			);
		}

		// todo : implement
		throw new NotYetImplementedException( "Query#executeUpdate not yet implemented" );
	}
}
