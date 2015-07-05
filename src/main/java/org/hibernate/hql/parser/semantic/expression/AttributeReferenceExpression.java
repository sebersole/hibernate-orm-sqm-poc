/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.hql.parser.semantic.expression;

import java.util.Locale;

import org.hibernate.hql.parser.antlr.path.AttributePathPart;
import org.hibernate.hql.parser.model.AttributeDescriptor;
import org.hibernate.hql.parser.model.TypeDescriptor;
import org.hibernate.hql.parser.semantic.from.FromElement;

/**
 * @author Steve Ebersole
 */
public class AttributeReferenceExpression implements AttributePathPart {
	private final FromElement source;
	private final AttributeDescriptor attributeDescriptor;

	public AttributeReferenceExpression(FromElement source, String attributeName) {
		this( source, source.getTypeDescriptor().getAttributeDescriptor( attributeName ) );
	}

	public AttributeReferenceExpression(
			FromElement source,
			AttributeDescriptor attributeDescriptor) {
		this.source = source;
		this.attributeDescriptor = attributeDescriptor;
	}

	public FromElement getSource() {
		return source;
	}

	public AttributeDescriptor getAttributeDescriptor() {
		return attributeDescriptor;
	}

	@Override
	public TypeDescriptor getTypeDescriptor() {
		return getAttributeDescriptor().getType();
	}

	@Override
	public String toString() {
		return String.format(
				Locale.ENGLISH,
				"AttributeReferenceExpression{" +
						"source=%s" +
						", attribute-name=%s" +
						", attribute-type=%s" +
						'}',
				getSource().getAlias(),
				getAttributeDescriptor().getName(),
				getAttributeDescriptor().getType().getTypeName()
		);
	}
}
