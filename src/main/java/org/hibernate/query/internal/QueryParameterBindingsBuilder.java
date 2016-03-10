/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.internal;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.hibernate.query.QueryParameter;
import org.hibernate.query.QueryParameterBindings;
import org.hibernate.sqm.BaseSemanticQueryWalker;
import org.hibernate.sqm.query.Statement;
import org.hibernate.sqm.query.expression.NamedParameterExpression;
import org.hibernate.sqm.query.expression.PositionalParameterExpression;
import org.hibernate.sqm.query.order.OrderByClause;

/**
 * @author Steve Ebersole
 */
public class QueryParameterBindingsBuilder extends BaseSemanticQueryWalker {
	public static QueryParameterBindings buildQueryParameterBindings(Statement statement) {
		final QueryParameterBindingsBuilder walker = new QueryParameterBindingsBuilder();
		walker.visitStatement( statement );

		final Set<QueryParameter> queryParameters;
		if ( walker.namedParamMap == null && walker.positionParamMap == null ) {
			// no parameters were found
			queryParameters = Collections.emptySet();
		}
		else {
			queryParameters = new HashSet<QueryParameter>();
			if ( walker.namedParamMap != null ) {
				queryParameters.addAll( walker.namedParamMap.values() );
			}
			if ( walker.positionParamMap != null ) {
				queryParameters.addAll( walker.positionParamMap.values() );
			}
		}

		return new QueryParameterBindings( queryParameters );
	}

	private Map<String,NamedQueryParameter> namedParamMap;
	private Map<Integer,PositionalQueryParameter> positionParamMap;

	private QueryParameterBindingsBuilder() {
	}

	@Override
	public Object visitNamedParameterExpression(NamedParameterExpression expression) {
		super.visitNamedParameterExpression( expression );

		boolean create;
		if ( namedParamMap == null ) {
			namedParamMap = new HashMap<String, NamedQueryParameter>();
			create = true;
		}
		else {
			create = !namedParamMap.containsKey( expression.getName() );
		}

		if ( create ) {
			namedParamMap.put( expression.getName(), new NamedQueryParameter( expression.getName() ) );
		}

		return expression;
	}

	@Override
	public Object visitPositionalParameterExpression(PositionalParameterExpression expression) {
		super.visitPositionalParameterExpression( expression );

		boolean create;
		if ( positionParamMap == null ) {
			positionParamMap = new HashMap<Integer, PositionalQueryParameter>();
			create = true;
		}
		else {
			create = !positionParamMap.containsKey( expression.getPosition() );
		}

		if ( create ) {
			positionParamMap.put( expression.getPosition(), new PositionalQueryParameter( expression.getPosition() ) );
		}

		return expression;
	}

	@Override
	public Object visitOrderByClause(OrderByClause orderByClause) {
		if ( orderByClause == null ) {
			return null;
		}
		return super.visitOrderByClause( orderByClause );
	}
}
