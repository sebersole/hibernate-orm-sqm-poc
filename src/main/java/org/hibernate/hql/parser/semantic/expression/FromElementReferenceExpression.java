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
public class FromElementReferenceExpression implements Expression {
	private final FromElement fromElement;

	public FromElementReferenceExpression(FromElement fromElement) {
		this.fromElement = fromElement;
	}

	public FromElement getFromElement() {
		return fromElement;
	}

	@Override
	public TypeDescriptor getTypeDescriptor() {
		return fromElement.getTypeDescriptor();
	}
}
