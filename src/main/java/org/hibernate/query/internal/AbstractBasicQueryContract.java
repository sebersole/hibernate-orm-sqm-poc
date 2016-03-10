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
import org.hibernate.sql.exec.spi.ExecutionOptions;

/**
 * @author Steve Ebersole
 */
public abstract class AbstractBasicQueryContract<T extends BasicQueryContract> implements BasicQueryContract<T> {
	private FlushMode flushMode;
	private boolean readOnly;

	private final ExecutionOptionsImpl executionOptions = new ExecutionOptionsImpl();

	protected ExecutionOptions executionOptions() {
		return executionOptions;
	}

	@Override
	public FlushMode getFlushMode() {
		return flushMode;
	}

	@Override
	public T setFlushMode(FlushMode flushMode) {
		this.flushMode = flushMode;
		return (T) this;
	}

	@Override
	public boolean isReadOnly() {
		return readOnly;
	}

	@Override
	public T setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
		return (T) this;
	}

	@Override
	public CacheMode getCacheMode() {
		return executionOptions.getCacheMode();
	}

	@Override
	public T setCacheMode(CacheMode cacheMode) {
		this.executionOptions.setCacheMode( cacheMode );
		return (T) this;
	}

	@Override
	public String getCacheRegion() {
		return executionOptions.getCacheRegion();
	}

	@Override
	public T setCacheRegion(String cacheRegion) {
		this.executionOptions.setCacheRegion( cacheRegion );
		return (T) this;
	}

	@Override
	public Integer getTimeout() {
		return executionOptions.getTimeout();
	}

	@Override
	public T setTimeout(int timeout) {
		this.executionOptions.setTimeout( timeout );
		return (T) this;
	}

	@Override
	public Integer getFetchSize() {
		return executionOptions.getFetchSize();
	}

	@Override
	public T setFetchSize(int fetchSize) {
		this.executionOptions.setFetchSize( fetchSize);
		return (T) this;
	}
}
