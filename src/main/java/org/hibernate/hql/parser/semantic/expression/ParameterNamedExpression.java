/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.hql.parser.semantic.expression;

import org.hibernate.hql.parser.model.TypeDescriptor;
import org.hibernate.hql.parser.util.BasicTypeDescriptorSingleton;

/**
 * @author Steve Ebersole
 */
public class ParameterNamedExpression implements ParameterExpression {
	private final String name;

	public ParameterNamedExpression(String name) {
		this.name = name;
	}

	@Override
	public TypeDescriptor getTypeDescriptor() {
		return BasicTypeDescriptorSingleton.INSTANCE;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Integer getPosition() {
		return null;
	}
}
