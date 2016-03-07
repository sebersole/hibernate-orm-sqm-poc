/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.ast.expression;

import org.hibernate.type.Type;

/**
 * We classify literals different based on their source so that we can handle then differently
 * when rendering SQL.  This class offers convenience for those implementations
 *
 * @author Steve Ebersole
 */
public abstract class AbstractLiteral implements Expression {
	private final Object value;
	private final Type ormType;

	public AbstractLiteral(Object value, Type ormType) {
		this.value = value;
		this.ormType = ormType;
	}

	public Object getValue() {
		return value;
	}

	@Override
	public Type getType() {
		return ormType;
	}
}
