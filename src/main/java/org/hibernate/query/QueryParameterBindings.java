/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.hibernate.query.internal.NamedQueryParameter;
import org.hibernate.query.internal.PositionalQueryParameter;
import org.hibernate.query.internal.QueryParameterBindingImpl;

/**
 * Collection of value bindings for query parameters.
 *
 * @author Steve Ebersole
 */
public class QueryParameterBindings {
	private Map<QueryParameter, QueryParameterBinding> parameterBindingMap;

	public QueryParameterBindings() {
		this( Collections.<QueryParameter>emptySet() );
	}

	public QueryParameterBindings(Set<QueryParameter> queryParameters) {
		if ( queryParameters == null || queryParameters.isEmpty() ) {
			parameterBindingMap = Collections.emptyMap();
		}
		else {
			parameterBindingMap = new HashMap<QueryParameter, QueryParameterBinding>();

			for ( QueryParameter queryParameter : queryParameters ) {
				parameterBindingMap.put( queryParameter, new QueryParameterBindingImpl() );
			}
		}
	}

	public QueryParameterBinding getBinding(QueryParameter parameter) {
		return parameterBindingMap.get( parameter );
	}

	public QueryParameterBinding getNamedParameterBinding(String name) {
		for ( Map.Entry<QueryParameter, QueryParameterBinding> entry : parameterBindingMap.entrySet() ) {
			if ( entry.getKey() instanceof NamedQueryParameter ) {
				if ( name.equals( ( (NamedQueryParameter) entry.getKey() ).getName() ) ) {
					return entry.getValue();
				}
			}
		}

		throw new IllegalStateException( "Unknown named parameter : " + name );
	}

	public QueryParameterBinding getPositionalParameterBinding(Integer position) {
		for ( Map.Entry<QueryParameter, QueryParameterBinding> entry : parameterBindingMap.entrySet() ) {
			if ( entry.getKey() instanceof PositionalQueryParameter ) {
				if ( position.equals( ( (PositionalQueryParameter) entry.getKey() ).getPosition() ) ) {
					return entry.getValue();
				}
			}
		}

		throw new IllegalStateException( "Unknown positional parameter : " + position );
	}
}
