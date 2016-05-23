/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.proposed.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.hibernate.CacheMode;
import org.hibernate.FlushMode;
import org.hibernate.LockOptions;
import org.hibernate.sql.exec.spi.Limit;
import org.hibernate.sql.exec.spi.QueryOptions;

/**
 * @author Steve Ebersole
 */
public class QueryOptionsImpl implements QueryOptions {
	private Integer timeout;
	private FlushMode flushMode;
	private String comment;
	private List<String> sqlHints;

	// only valid for (non-native) select queries
	private final Limit limit = new Limit();
	private final LockOptions lockOptions = new LockOptions();
	private Integer fetchSize;
	private CacheMode cacheMode;
	private Boolean resultCachingEnabled;
	private String resultCacheRegionName;
	private Boolean readOnlyEnabled;

	@Override
	public Integer getTimeout() {
		return timeout;
	}

	public void setTimeout(Integer timeout) {
		this.timeout = timeout;
	}

	@Override
	public FlushMode getFlushMode() {
		return flushMode;
	}

	public void setFlushMode(FlushMode flushMode) {
		this.flushMode = flushMode;
	}

	@Override
	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	@Override
	public List<String> getSqlHints() {
		return sqlHints == null ? Collections.<String>emptyList() : sqlHints;
	}

	public void addSqlHint(String hint) {
		if ( sqlHints == null ) {
			sqlHints = new ArrayList<String>();
		}
		sqlHints.add( hint );
	}

	@Override
	public Limit getLimit() {
		return limit;
	}

	@Override
	public LockOptions getLockOptions() {
		return lockOptions;
	}

	@Override
	public Integer getFetchSize() {
		return fetchSize;
	}

	public void setFetchSize(Integer fetchSize) {
		this.fetchSize = fetchSize;
	}

	@Override
	public CacheMode getCacheMode() {
		return cacheMode;
	}

	public void setCacheMode(CacheMode cacheMode) {
		this.cacheMode = cacheMode;
	}

	@Override
	public Boolean isResultCachingEnabled() {
		return resultCachingEnabled;
	}

	public void setResultCachingEnabled(boolean resultCachingEnabled) {
		this.resultCachingEnabled = resultCachingEnabled;
	}


	@Override
	public String getResultCacheRegionName() {
		return resultCacheRegionName;
	}

	public void setResultCacheRegionName(String resultCacheRegionName) {
		this.resultCacheRegionName = resultCacheRegionName;
	}

	@Override
	public Boolean isReadOnly() {
		return readOnlyEnabled;
	}

	public void setReadOnlyEnabled(boolean readOnlyEnabled) {
		this.readOnlyEnabled = readOnlyEnabled;
	}
}
