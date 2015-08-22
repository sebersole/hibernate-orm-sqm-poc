/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.sqm.query.expression;

import org.hibernate.sqm.domain.TypeDescriptor;

/**
 * @author Steve Ebersole
 */
public class ParameterPositionalExpression implements ParameterExpression {
	private final int position;
	private TypeDescriptor typeDescriptor;

	public ParameterPositionalExpression(int position) {
		this.position = position;
	}

	@Override
	public TypeDescriptor getTypeDescriptor() {
		return typeDescriptor;
	}

	@Override
	public String getName() {
		return null;
	}

	@Override
	public Integer getPosition() {
		return position;
	}
}
