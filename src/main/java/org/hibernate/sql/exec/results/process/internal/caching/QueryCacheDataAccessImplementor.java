/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.exec.results.process.internal.caching;

import org.hibernate.sql.exec.results.process.spi2.QueryCacheDataAccess;

import org.jboss.logging.Logger;

/**
 * @author Steve Ebersole
 */
public interface QueryCacheDataAccessImplementor extends QueryCacheDataAccess {
	Logger log = Logger.getLogger( QueryCacheDataAccessImplementor.class );

	interface QueryCacheGetManagerImplementor extends QueryCacheGetManager {
		boolean next();
	}

	@Override
	QueryCacheGetManagerImplementor getQueryCacheGetManager();

	default boolean next() {
		return getQueryCacheGetManager().next();
	}

	interface QueryCachePutManagerImplementor extends QueryCachePutManager {
		void finishUp();
	}

	@Override
	QueryCachePutManagerImplementor getQueryCachePutManager();

	default void finishUp() {
		getQueryCachePutManager().finishUp();
	}
}
