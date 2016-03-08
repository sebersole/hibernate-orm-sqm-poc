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
import org.hibernate.sql.gen.SqlTreeWalker;
import org.hibernate.sql.orm.QueryOptions;
import org.hibernate.sql.orm.QueryParameterBinding;
import org.hibernate.type.Type;

/**
 * Represents a named parameter coming from the query.
 *
 * @author Steve Ebersole
 */
public class NamedParameter extends AbstractParameter {
	private final String name;

	public NamedParameter(String name, Type inferredType) {
		super( inferredType );
		this.name = name;
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

	@Override
	public void accept(SqlTreeWalker sqlTreeWalker) {
		sqlTreeWalker.visitNamedParameter( this );
	}
}
