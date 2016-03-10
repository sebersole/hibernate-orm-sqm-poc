/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.internal;

import java.util.Iterator;
import java.util.List;

import org.hibernate.LockMode;
import org.hibernate.LockOptions;
import org.hibernate.QueryException;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.query.Query;
import org.hibernate.query.QueryParameterBindings;
import org.hibernate.sql.exec.internal.SemanticQueryExecutorImpl;
import org.hibernate.sql.gen.Callback;
import org.hibernate.sqm.ConsumerContext;
import org.hibernate.sqm.SemanticQueryInterpreter;
import org.hibernate.sqm.parser.NotYetImplementedException;
import org.hibernate.sqm.query.SelectStatement;
import org.hibernate.sqm.query.Statement;
import org.hibernate.type.Type;

/**
 * @author Steve Ebersole
 */
public class QueryImpl extends AbstractBasicQueryContract<Query> implements Query<Query> {
	private final String queryString;
	private final SessionImplementor session;

	private final Statement sqm;

	private final InterpretationOptionsImpl interpretationOptions = new InterpretationOptionsImpl();
	private final QueryParameterBindings queryParameterBindings;

	// todo : this contract will change as we integrate this into ORM
	// 	ultimately the SessionFactory will implement (or provide access to) ConsumerContext

	public QueryImpl(String queryString, SessionImplementor session, ConsumerContext consumerContext) {
		this.queryString = queryString;
		this.session = session;

		this.sqm = SemanticQueryInterpreter.interpret(
				queryString,
				consumerContext
		);
		this.queryParameterBindings = QueryParameterBindingsBuilder.buildQueryParameterBindings( sqm );
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
	public Iterator iterate() {
		// todo : implement
		throw new NotYetImplementedException( "Query#iterate not yet implemented" );
	}

	@Override
	public ScrollableResults scroll() {
		return scroll( session.getFactory().getDialect().defaultScrollMode() );
	}

	@Override
	public ScrollableResults scroll(ScrollMode scrollMode) {
		// todo : implement
		throw new NotYetImplementedException( "Query#scroll not yet implemented" );
	}

	@Override
	public List list() {
		return new SemanticQueryExecutorImpl().executeSelect(
				(SelectStatement) sqm,
				interpretationOptions,
				executionOptions(),
				queryParameterBindings,
				new RowTransformerPassThruImpl(),
				new Callback() {},
				session
		);
	}

	@Override
	public Object uniqueResult() {
		final List results = list();
		if ( list().size() != 1 ) {
			throw new QueryException( "Query returned more than one result" );
		}

		return results.get( 0 );
	}

	@Override
	public int executeUpdate() {
		// todo : implement
		throw new NotYetImplementedException( "Query#executeUpdate not yet implemented" );
	}
}
