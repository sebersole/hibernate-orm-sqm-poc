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
class QueryCacheGetManagerCacheMissImpl implements QueryCacheDataAccessImplementor.QueryCacheGetManagerImplementor {
	/**
	 * Singleton access
	 */
	public static final QueryCacheGetManagerCacheMissImpl INSTANCE = new QueryCacheGetManagerCacheMissImpl();

	@Override
	public Object[] getCurrentRow() {
		return null;
	}

	@Override
	public boolean next() {
		return false;
	}
}
