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
import org.hibernate.sql.orm.QueryParameterBinding;
import org.hibernate.type.Type;

/**
 * @author Steve Ebersole
 */
public abstract class AbstractParameter implements Expression, ParameterSpec, ParameterBinder {
	private final Type inferredType;

	public AbstractParameter(Type inferredType) {
		this.inferredType = inferredType;
	}

	public Type getInferredType() {
		return inferredType;
	}

	@Override
	public Type getType() {
		return getInferredType();
	}

	@Override
	public ParameterBinder getParameterBinder() {
		return this;
	}

	protected int bindParameterValue(
			PreparedStatement statement,
			int startPosition,
			QueryParameterBinding valueBinding,
			SessionImplementor session) throws SQLException {
		final Type bindType;
		if ( valueBinding.getBindType() == null ) {
			bindType = inferredType;
		}
		else {
			bindType = valueBinding.getBindType();
		}
		assert bindType != null;

		bindType.nullSafeSet( statement, valueBinding.getBindValue(), startPosition, session );
		return bindType.getColumnSpan( session.getFactory() );
	}
}
