/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.orm.test.sql.support;

import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.query.proposed.spi.NativeQueryImplementor;
import org.hibernate.query.proposed.spi.QueryImplementor;
import org.hibernate.query.proposed.spi.QueryProducerImplementor;

/**
 * @author Steve Ebersole
 */
public class QueryProducerTestingImpl implements QueryProducerImplementor {
	private final SharedSessionContractImplementor session;

	public QueryProducerTestingImpl(SharedSessionContractImplementor session) {
		this.session = session;
	}

	@Override
	public SessionFactoryImplementor getFactory() {
		return session.getFactory();
	}

	@Override
	public QueryImplementor createQuery(String queryString) {
		throw new UnsupportedOperationException();
	}

	@Override
	public <R> QueryImplementor<R> createQuery(String queryString, Class<R> resultClass) {
		throw new UnsupportedOperationException();
	}

	@Override
	public NativeQueryImplementor createNativeQuery(String sqlString) {
		throw new UnsupportedOperationException();
	}

	@Override
	public NativeQueryImplementor createNativeQuery(String sqlString, Class resultClass) {
		throw new UnsupportedOperationException();
	}

	@Override
	public NativeQueryImplementor createNativeQuery(String sqlString, String resultSetMapping) {
		throw new UnsupportedOperationException();
	}

	@Override
	public QueryImplementor getNamedQuery(String queryName) {
		throw new UnsupportedOperationException();
	}

	@Override
	public QueryImplementor createNamedQuery(String name) {
		throw new UnsupportedOperationException();
	}

	@Override
	public <R> QueryImplementor<R> createNamedQuery(String name, Class<R> resultClass) {
		throw new UnsupportedOperationException();
	}

	@Override
	public NativeQueryImplementor getNamedNativeQuery(String name) {
		throw new UnsupportedOperationException();
	}
}
