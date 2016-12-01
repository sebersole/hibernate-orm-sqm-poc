/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.exec.results.process.internal.caching;

import java.util.List;

/**
 * @author Steve Ebersole
 */
class QueryCacheGetManagerCacheHitImpl implements QueryCacheDataAccessImplementor.QueryCacheGetManagerImplementor {
	private final List<Object[]> initialCachedData;

	private int currentIndex = 0;

	QueryCacheGetManagerCacheHitImpl(List<Object[]> initialCachedData) {
		this.initialCachedData = initialCachedData;
	}

	@Override
	public Object[] getCurrentRow() {
		return initialCachedData.get( currentIndex );
	}

	@Override
	public boolean next() {
		currentIndex++;
		return initialCachedData.size() < currentIndex;
	}
}
