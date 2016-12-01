/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.exec.results.process.internal.caching;

/**
 * @author Steve Ebersole
 */
public class QueryCacheDataAccessNoCachingImpl implements QueryCacheDataAccessImplementor {
	/**
	 * Singleton access
	 */
	public static final QueryCacheDataAccessNoCachingImpl INSTANCE = new QueryCacheDataAccessNoCachingImpl();

	// todo : this is really just a case of no cache-get AND no cache-put

	@Override
	public boolean next() {
		return true;
	}

	@Override
	public void finishUp() {
	}

	@Override
	public QueryCacheGetManagerImplementor getQueryCacheGetManager() {
		return QueryCacheGetManagerCacheMissImpl.INSTANCE;
	}

	@Override
	public QueryCachePutManagerImplementor getQueryCachePutManager() {
		return QueryCachePutManagerDisabledImpl.INSTANCE;
	}
}
