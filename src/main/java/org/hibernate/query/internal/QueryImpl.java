/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.internal;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
import org.hibernate.query.IllegalQueryOperationException;
import org.hibernate.query.Query;
import org.hibernate.query.QueryParameter;
import org.hibernate.query.spi.QueryParameterBindings;
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
import org.hibernate.sqm.ConsumerContext;
import org.hibernate.sqm.SemanticQueryInterpreter;
import org.hibernate.sqm.parser.NotYetImplementedException;
import org.hibernate.sqm.query.NonSelectStatement;
import org.hibernate.sqm.query.SelectStatement;
import org.hibernate.sqm.query.Statement;
import org.hibernate.sqm.query.select.Selection;
import org.hibernate.type.Type;

import static org.hibernate.sql.ast.SelectStatementInterpreter.interpret;

/**
 * @author Steve Ebersole
 */
public class QueryImpl<R> extends AbstractBasicQueryContract<Query> implements Query<Query,R> {
	private final String queryString;
	private final SessionImplementor session;

	private final Statement sqm;
	private final RowTransformer<R> implicitRowTransformer;

	private final InterpretationOptionsImpl interpretationOptions = new InterpretationOptionsImpl();
	private final QueryParameterBindings queryParameterBindings;

	// todo : this contract will change as we integrate this into ORM
	// 	ultimately the SessionFactory will implement (or provide access to) ConsumerContext

	public QueryImpl(
			String queryString,
			Class<R> resultType,
			SessionImplementor session,
			ConsumerContext consumerContext) {
		this.queryString = queryString;
		this.session = session;

		// todo : reconsider this aspect of building QueryParameterBindings
		//		the concern here is performance only.  What happens is that we end up walking the
		//		SQM specifically to extract query parameter information to prepare the bindings
		//		container.  The more times we walk the tree, the worse the performance.  So while
		//		this approach for collecting the query parameters is natural, we do need to keep an
		//		eye on the performance.
		//
		//		An alternative would be to define an `ExpressionListener` contract in SQM that
		//		we could pass into the semantic analysis.  The idea being that as the SQM is built
		//		we would call out the the `ExpressionListener` for any Expression objects built.
		//		We could also generalize this to apply as a specialized case of `SemanticQueryWalker`
		//		for consumers of the SQM
		this.sqm = SemanticQueryInterpreter.interpret(
				queryString,
				consumerContext
		);
		this.queryParameterBindings = QueryParameterBindingsBuilder.buildQueryParameterBindings( sqm );

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
				List<TupleElement<?>> tupleElements = new ArrayList<TupleElement<?>>();
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
	public String getQueryString() {
		return queryString;
	}

	@Override
	public Integer getMaxResults() {
		return interpretationOptions.getMaxRows();
	}

	@Override
	public Query setMaxResults(int maxResults) {
		interpretationOptions.setMaxRows( maxResults );
		return this;
	}

	@Override
	public Integer getFirstResult() {
		return interpretationOptions.getFirstRow();
	}

	@Override
	public Query setFirstResult(int firstResult) {
		interpretationOptions.setFirstRow( firstResult );
		return this;
	}

	@Override
	public LockOptions getLockOptions() {
		return interpretationOptions.getLockOptions();
	}

	@Override
	public Query setLockOptions(LockOptions lockOptions) {
		interpretationOptions.setLockOptions( lockOptions );
		return this;
	}

	@Override
	public Query setLockMode(String alias, LockMode lockMode) {
		interpretationOptions.getLockOptions().setAliasSpecificLockMode( alias, lockMode );
		return this;
	}

	@Override
	public String getComment() {
		return interpretationOptions.getComment();
	}

	@Override
	public Query setComment(String comment) {
		interpretationOptions.setComment( comment );
		return this;
	}

	@Override
	public Query addQueryHint(String hint) {
		interpretationOptions.addSqlHint( hint );
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
		return scroll( session.getFactory().getDialect().defaultScrollMode() );
	}

	@Override
	@SuppressWarnings("unchecked")
	public ScrollableResults scroll(ScrollMode scrollMode) {
		verifyQueryIsSelect();

		final SelectQuery sqlTree = interpret( (SelectStatement) sqm, interpretationOptions, new Callback() {} );
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
				executionOptions(),
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

		final SelectQuery sqlTree = interpret( (SelectStatement) sqm, interpretationOptions, new Callback() {} );
		return (List<R>) new SqlTreeExecutorImpl().executeSelect(
				sqlTree,
				PreparedStatementCreatorStandardImpl.INSTANCE,
				PreparedStatementExecutorNormalImpl.INSTANCE,
				executionOptions(),
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
