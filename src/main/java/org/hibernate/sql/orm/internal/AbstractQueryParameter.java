/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.sql.orm.internal;

import org.hibernate.sql.orm.QueryParameter;
import org.hibernate.type.Type;

/**
 * @author Steve Ebersole
 */
public abstract class AbstractQueryParameter implements QueryParameter {
	private final Type expectedType;

	public AbstractQueryParameter(Type expectedType) {
		this.expectedType = expectedType;
	}

	@Override
	public Type getExpectedType() {
		return expectedType;
	}
}
