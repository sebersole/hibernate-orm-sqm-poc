/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.hql.parser.semantic.expression;

import org.hibernate.hql.parser.model.TypeDescriptor;
import org.hibernate.hql.parser.semantic.from.FromElement;

/**
 * @author Steve Ebersole
 */
public class AttributeReferenceExpression implements Expression {
	private final FromElement source;
	private final String attributeName;

	private final TypeDescriptor typeDescriptor;

	public AttributeReferenceExpression(FromElement source, String attributeName) {
		this.source = source;
		this.attributeName = attributeName;

		this.typeDescriptor = source.getTypeDescriptor().getAttributeType( attributeName );
	}

	public FromElement getSource() {
		return source;
	}

	public String getAttributeName() {
		return attributeName;
	}

	@Override
	public TypeDescriptor getTypeDescriptor() {
		return typeDescriptor;
	}
}
