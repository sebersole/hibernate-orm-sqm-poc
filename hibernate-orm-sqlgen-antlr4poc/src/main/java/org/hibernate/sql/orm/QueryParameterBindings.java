/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.sql.orm;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.hibernate.sql.orm.internal.NamedQueryParameter;
import org.hibernate.sql.orm.internal.PositionalQueryParameter;
import org.hibernate.sql.orm.internal.QueryParameterBindingImpl;

/**
 * Kind of part of {@link org.hibernate.engine.spi.QueryParameters}; but partially implemented and done elsewhere too
 *
 * @author Steve Ebersole
 */
public class QueryParameterBindings {
	private Map<QueryParameter, QueryParameterBinding> parameterBindingMap;

	private Map<String,NamedQueryParameter> namedParameterMap;
	private Map<Integer,PositionalQueryParameter> positionalParameterMap;

	public QueryParameterBindings(Set<QueryParameter> queryParameters) {
		if ( queryParameters == null || queryParameters.isEmpty() ) {
			parameterBindingMap = Collections.emptyMap();
			namedParameterMap = Collections.emptyMap();
			positionalParameterMap = Collections.emptyMap();
		}
		else {
			parameterBindingMap = new HashMap<QueryParameter, QueryParameterBinding>();
			namedParameterMap = new HashMap<String, NamedQueryParameter>();
			positionalParameterMap = new HashMap<Integer, PositionalQueryParameter>();

			for ( QueryParameter queryParameter : queryParameters ) {
				parameterBindingMap.put(
						queryParameter,
						new QueryParameterBindingImpl( queryParameter )
				);

				if ( queryParameter instanceof NamedQueryParameter ) {
					final NamedQueryParameter namedQueryParameter = (NamedQueryParameter) queryParameter;
					namedParameterMap.put( namedQueryParameter.getName(), namedQueryParameter );
				}
				else if ( queryParameter instanceof PositionalQueryParameter ) {
					final PositionalQueryParameter positionalQueryParameter = (PositionalQueryParameter) queryParameter;
					positionalParameterMap.put( positionalQueryParameter.getPosition(), positionalQueryParameter );
				}
			}
		}
	}

	public QueryParameterBinding getBinding(QueryParameter parameter) {
		return parameterBindingMap.get( parameter );
	}

	public QueryParameterBinding getNamedParameterBinding(String name) {
		final NamedQueryParameter param = namedParameterMap.get( name );
		if ( param == null ) {
			throw new IllegalStateException( "Unknown named parameter : " + name );
		}

		return getBinding( param );
	}

	public QueryParameterBinding getPositionalParameterBinding(Integer position) {
		final PositionalQueryParameter param = positionalParameterMap.get( position );
		if ( param == null ) {
			throw new IllegalStateException( "Unknown positional parameter : " + position );
		}

		return getBinding( param );
	}
}
