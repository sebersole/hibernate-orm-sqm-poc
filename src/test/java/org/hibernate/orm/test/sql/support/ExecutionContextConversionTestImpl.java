/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.orm.test.sql.support;

import java.sql.SQLException;

import org.hibernate.CacheMode;
import org.hibernate.FlushMode;
import org.hibernate.HibernateException;
import org.hibernate.JDBCException;
import org.hibernate.LockOptions;
import org.hibernate.engine.spi.ExceptionConverter;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.internal.ExceptionConverterImpl;
import org.hibernate.query.proposed.spi.ExecutionContext;
import org.hibernate.query.proposed.spi.QueryInterpretations;
import org.hibernate.type.Type;

/**
 * A ExecutionContext for use in conversion tests (no Session)
 *
 * @author Steve Ebersole
 */
public class ExecutionContextConversionTestImpl implements ExecutionContext {
	private final SessionFactoryImplementor sessionFactory;

	public ExecutionContextConversionTestImpl(SessionFactoryImplementor sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	@Override
	public Type resolveParameterBindType(Object bindValue) {
		return sessionFactory.getMetamodel().getBasicTypeFactory().getRegisteredType( bindValue.getClass().getName() );
	}

	@Override
	public Type resolveParameterBindType(Class clazz) {
		return sessionFactory.getMetamodel().getBasicTypeFactory().getRegisteredType( clazz.getName() );
	}

	@Override
	public FlushMode getHibernateFlushMode() {
		return FlushMode.AUTO;
	}

	@Override
	public void setHibernateFlushMode(FlushMode effectiveFlushMode) {
	}

	@Override
	public CacheMode getCacheMode() {
		return CacheMode.NORMAL;
	}

	@Override
	public void setCacheMode(CacheMode effectiveCacheMode) {
	}

	@Override
	public boolean isDefaultReadOnly() {
		return false;
	}

	@Override
	public ExceptionConverter getExceptionConverter() {
		return new ExceptionConverter() {
			@Override
			public RuntimeException convertCommitException(RuntimeException e) {
				return null;
			}

			@Override
			public RuntimeException convert(HibernateException e, LockOptions lockOptions) {
				return null;
			}

			@Override
			public RuntimeException convert(HibernateException e) {
				return null;
			}

			@Override
			public RuntimeException convert(RuntimeException e) {
				return null;
			}

			@Override
			public RuntimeException convert(RuntimeException e, LockOptions lockOptions) {
				return null;
			}

			@Override
			public JDBCException convert(SQLException e, String message) {
				return null;
			}
		};
	}

	@Override
	public boolean isTransactionInProgress() {
		return false;
	}

	@Override
	public void checkOpen(boolean rollbackIfNot) {
	}

	@Override
	public void prepareForQueryExecution(boolean requiresTxn) {

	}

	@Override
	public QueryInterpretations getQueryInterpretations() {
		return null;
	}
}
