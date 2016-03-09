/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.gen;

import java.util.Collections;

import org.hibernate.ScrollMode;
import org.hibernate.cache.spi.QueryCache;
import org.hibernate.sql.exec.spi.ExecutionOptions;
import org.hibernate.query.QueryParameter;
import org.hibernate.query.QueryParameterBindings;
import org.hibernate.type.Type;

/**
 * @author Steve Ebersole
 */
class ExecutionOptionsImpl implements ExecutionOptions {
	private final QueryParameterBindings queryParameterBindings;

	public ExecutionOptionsImpl() {
		this( new QueryParameterBindings() );
	}

	public ExecutionOptionsImpl(QueryParameter parameter, Object value) {
		this( new QueryParameterBindings( Collections.singleton( parameter ) ) );
		queryParameterBindings.getBinding( parameter ).setBindValue( value );
	}

	public ExecutionOptionsImpl(QueryParameter parameter, Object value, Type type) {
		this( new QueryParameterBindings( Collections.singleton( parameter ) ) );
		queryParameterBindings.getBinding( parameter ).setBindValue( value, type );
	}

	public ExecutionOptionsImpl(QueryParameterBindings queryParameterBindings) {
		this.queryParameterBindings = queryParameterBindings;
	}

	@Override
	public QueryParameterBindings getParameterBindings() {
		return queryParameterBindings;
	}

	@Override
	public Integer getTimeout() {
		return null;
	}

	@Override
	public Integer getFetchSize() {
		return null;
	}

	@Override
	public ScrollMode getScrollMode() {
		return null;
	}

	@Override
	public QueryCache getQueryResultCache() {
		return null;
	}
}
