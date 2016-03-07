/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.ast.expression;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.sql.gen.ParameterBinder;
import org.hibernate.sql.gen.ParameterSpec;
import org.hibernate.sql.orm.QueryOptions;
import org.hibernate.sql.orm.QueryParameterBinding;
import org.hibernate.type.Type;

/**
 * Represents a named parameter coming from the query.
 *
 * @author Steve Ebersole
 */
public class NamedParameter implements Expression, ParameterSpec, ParameterBinder {
	private final String name;
	private final Type expectedType;

	public NamedParameter(String name, Type expectedType) {
		this.name = name;
		this.expectedType = expectedType;
	}

	@Override
	public Type getType() {
		return expectedType;
	}

	@Override
	public ParameterBinder getParameterBinder() {
		return this;
	}

	@Override
	public int bindParameterValue(
			PreparedStatement statement,
			int startPosition,
			QueryOptions queryOptions,
			SessionImplementor session) throws SQLException {
		final QueryParameterBinding binding = queryOptions.getParameterBindings().getNamedParameterBinding( name );
		return bindParameterValue(  statement, startPosition, binding, session );
	}

	protected int bindParameterValue(
			PreparedStatement statement,
			int startPosition,
			QueryParameterBinding valueBinding,
			SessionImplementor session) throws SQLException {
		final Type bindType;
		if ( valueBinding.getBindType() == null ) {
			bindType = expectedType;
		}
		else {
			bindType = valueBinding.getBindType();
		}
		assert bindType != null;

		bindType.nullSafeSet( statement, valueBinding.getBindValue(), startPosition, session );
		return bindType.getColumnSpan( session.getFactory() );
	}
}
