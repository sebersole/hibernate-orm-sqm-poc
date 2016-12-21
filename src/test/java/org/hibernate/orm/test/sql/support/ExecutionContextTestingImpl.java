/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.orm.test.sql.support;

import javax.persistence.TransactionRequiredException;

import org.hibernate.CacheMode;
import org.hibernate.FlushMode;
import org.hibernate.engine.spi.ExceptionConverter;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.internal.SessionImpl;
import org.hibernate.query.proposed.spi.ExecutionContext;
import org.hibernate.query.proposed.spi.QueryInterpretations;
import org.hibernate.type.Type;

/**
 * @author Steve Ebersole
 */
public class ExecutionContextTestingImpl implements ExecutionContext {
	private final SharedSessionContractImplementor session;

	public ExecutionContextTestingImpl(SharedSessionContractImplementor session) {
		this.session = session;
	}

	@Override
	public Type resolveParameterBindType(Object bindValue) {
		return session.getFactory().getMetamodel().getBasicTypeFactory().getRegisteredType( bindValue.getClass().getName() );
	}

	@Override
	public Type resolveParameterBindType(Class clazz) {
		return session.getFactory().getMetamodel().getBasicTypeFactory().getRegisteredType( clazz.getName() );
	}

	@Override
	public FlushMode getHibernateFlushMode() {
		return session.getHibernateFlushMode();
	}

	@Override
	public void setHibernateFlushMode(FlushMode effectiveFlushMode) {
		session.setHibernateFlushMode( effectiveFlushMode );
	}

	@Override
	public CacheMode getCacheMode() {
		return session.getCacheMode();
	}

	@Override
	public void setCacheMode(CacheMode effectiveCacheMode) {
		session.setCacheMode( effectiveCacheMode );
	}

	@Override
	public boolean isDefaultReadOnly() {
		return ( (SessionImpl) session ).isDefaultReadOnly();
	}

	@Override
	public ExceptionConverter getExceptionConverter() {
		return session.getExceptionConverter();
	}

	@Override
	public boolean isTransactionInProgress() {
		return session.isTransactionInProgress();
	}

	@Override
	public void checkOpen(boolean rollbackIfNot) {
		session.checkOpen( rollbackIfNot );
	}

	@Override
	public void prepareForQueryExecution(boolean requiresTxn) {
		session.checkOpen();
//			session.checkTransactionSynchStatus();

		if ( requiresTxn && !isTransactionInProgress() ) {
			throw new TransactionRequiredException(
					"Query requires transaction be in progress, but no transaction is known to be in progress"
			);
		}
	}

	@Override
	public QueryInterpretations getQueryInterpretations() {
		return QueryInterpretationsTestingImpl.INSTANCE;
	}
}
