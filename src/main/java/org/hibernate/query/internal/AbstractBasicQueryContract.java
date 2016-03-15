/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.internal;

import org.hibernate.CacheMode;
import org.hibernate.FlushMode;
import org.hibernate.query.BasicQueryContract;

import org.jboss.logging.Logger;

/**
 * @author Steve Ebersole
 */
public abstract class AbstractBasicQueryContract<T extends BasicQueryContract> implements BasicQueryContract<T> {
	private static final Logger log = Logger.getLogger( AbstractBasicQueryContract.class );

	private final QueryOptionsImpl queryOptions = new QueryOptionsImpl();

	protected abstract boolean allowSelectOptions();

	protected QueryOptionsImpl queryOptions() {
		return queryOptions;
	}

	@Override
	public FlushMode getFlushMode() {
		return queryOptions.getFlushMode();
	}

	@Override
	@SuppressWarnings("unchecked")
	public T setFlushMode(FlushMode flushMode) {
		this.queryOptions.setFlushMode( flushMode );
		return (T) this;
	}

	@Override
	public Integer getTimeout() {
		return queryOptions().getTimeout();
	}

	@Override
	@SuppressWarnings("unchecked")
	public T setTimeout(int timeout) {
		this.queryOptions().setTimeout( timeout );
		return (T) this;
	}

	@Override
	public boolean isReadOnly() {
		return queryOptions().isReadOnly() == Boolean.TRUE;
	}

	@Override
	@SuppressWarnings("unchecked")
	public T setReadOnly(boolean readOnly) {
		if ( !allowSelectOptions() ) {
			log.debug( "Attempt to set read-only option on non-select (or native) query" );
		}
		this.queryOptions().setReadOnlyEnabled( readOnly );
		return (T) this;
	}

	@Override
	public CacheMode getCacheMode() {
		return queryOptions().getCacheMode();
	}

	@Override
	@SuppressWarnings("unchecked")
	public T setCacheMode(CacheMode cacheMode) {
		this.queryOptions().setCacheMode( cacheMode );
		return (T) this;
	}

	@Override
	public boolean isCacheable() {
		return this.queryOptions().isResultCachingEnabled() == Boolean.TRUE;
	}

	@Override
	@SuppressWarnings("unchecked")
	public T setCacheable(boolean cacheable) {
		this.queryOptions().setResultCachingEnabled( cacheable );
		return (T) this;
	}

	@Override
	public String getCacheRegion() {
		return queryOptions().getResultCacheRegionName();
	}

	@Override
	@SuppressWarnings("unchecked")
	public T setCacheRegion(String cacheRegion) {
		if ( !allowSelectOptions() ) {
			log.debug( "Attempt to set cache-region option on non-select (or native) query" );
		}
		this.queryOptions().setResultCacheRegionName( cacheRegion );
		return (T) this;
	}

	@Override
	public Integer getFetchSize() {
		return queryOptions().getFetchSize();
	}

	@Override
	@SuppressWarnings("unchecked")
	public T setFetchSize(int fetchSize) {
		if ( !allowSelectOptions() ) {
			log.debug( "Attempt to set cache-region option on non-select (or native) query" );
		}
		this.queryOptions().setFetchSize( fetchSize);
		return (T) this;
	}
}
