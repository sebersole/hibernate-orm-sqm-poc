/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.exec.results.process.internal.caching;

import java.util.List;

import org.hibernate.CacheMode;
import org.hibernate.cache.spi.QueryCache;
import org.hibernate.cache.spi.QueryKey;

import org.jboss.logging.Logger;

/**
 * @author Steve Ebersole
 */
public class QueryCacheDataAccessEnabled implements QueryCacheDataAccessImplementor {
	private static final Logger log = Logger.getLogger( QueryCacheDataAccessEnabled.class );

	private final QueryCacheGetManagerImplementor cacheGetManager;
	private final QueryCachePutManagerImplementor cachePutManager;

	// todo : might be better for now to not implement the cache get and put until we have proper info to make those calls.

	@SuppressWarnings("unchecked")
	public QueryCacheDataAccessEnabled(CacheMode cacheMode, QueryCache queryCache, QueryKey queryResultsCacheKey) {
		List<Object[]> initialCachedData = null;
		boolean arePutsAllowed = cacheMode.isPutEnabled();

		if ( cacheMode.isGetEnabled() ) {
			log.debugf( "Reading Query result cache data per CacheMode#isGetEnabled [%s]", cacheMode.name() );

			initialCachedData = queryCache.get(
					// todo : QueryCache#get takes the `queryResultsCacheKey` see tat discussion above
					queryResultsCacheKey,
					// todo : QueryCache#get also takes a `Type[] returnTypes` argument which ought to be replaced with the Return graph
					// 		(or ResolvedReturn graph)
					null,
					// todo : QueryCache#get also takes a `isNaturalKeyLookup` argument which should go away
					// 		that is no longer the supported way to perform a load-by-naturalId
					false,
					// todo : `querySpaces` and `session` make perfect sense as args, but its odd passing those here
					null,
					null
			);

			// todo : see also the discussion todo-comment on org.hibernate.sql.exec.results.process.internal.caching.QueryCachePutManagerEnabledImpl.finishUp()

			if ( initialCachedData != null && initialCachedData.isEmpty() ) {
				initialCachedData = null;
			}
			else {
				arePutsAllowed = false;
			}
		}
		else {
			log.debugf( "Bypassing reading of Query result cache data per CacheMode#isGetEnabled [%s]", cacheMode.name() );

			initialCachedData = null;
		}

		if ( initialCachedData == null || initialCachedData.isEmpty() ) {
			cacheGetManager = QueryCacheGetManagerCacheMissImpl.INSTANCE;
		}
		else {
			cacheGetManager = new QueryCacheGetManagerCacheHitImpl( initialCachedData );
			// if we have cached results we should not be putting back...
			arePutsAllowed = false;
		}

		if ( arePutsAllowed ) {
			cachePutManager = new QueryCachePutManagerEnabledImpl( queryCache, queryResultsCacheKey );
		}
		else {
			cachePutManager = QueryCachePutManagerDisabledImpl.INSTANCE;
		}
	}

	@Override
	public QueryCacheGetManagerImplementor getQueryCacheGetManager() {
		return cacheGetManager;
	}

	@Override
	public QueryCachePutManagerImplementor getQueryCachePutManager() {
		return cachePutManager;
	}
}
