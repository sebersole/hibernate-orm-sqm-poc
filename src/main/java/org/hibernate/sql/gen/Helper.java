/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.gen;

import org.hibernate.QueryException;
import org.hibernate.sql.ast.expression.NamedParameter;
import org.hibernate.sql.ast.expression.PositionalParameter;
import org.hibernate.sql.orm.QueryParameterBinding;
import org.hibernate.sql.orm.QueryParameterBindings;
import org.hibernate.type.Type;

/**
 * @author Steve Ebersole
 */
public class Helper {
	public static Type resolveType(NamedParameter parameter, QueryParameterBindings bindings) {
		final QueryParameterBinding binding = bindings.getNamedParameterBinding( parameter.getName() );
		if ( binding != null ) {
			if ( binding.getBindType() != null ) {
				return binding.getBindType();
			}
		}

		if ( parameter.getType() != null ) {
			return parameter.getType();
		}

		throw new QueryException( "Unable to determine Type for named parameter [:" + parameter.getName() + "]" );
	}

	public static Type resolveType(PositionalParameter parameter, QueryParameterBindings bindings) {
		final QueryParameterBinding binding = bindings.getPositionalParameterBinding( parameter.getPosition() );
		if ( binding != null ) {
			if ( binding.getBindType() != null ) {
				return binding.getBindType();
			}
		}

		if ( parameter.getType() != null ) {
			return parameter.getType();
		}

		throw new QueryException( "Unable to determine Type for positional parameter [?" + parameter.getPosition() + "]" );
	}

	private Helper() {
	}
}
