/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.ast.expression;

import org.hibernate.sql.ast.from.ColumnBinding;
import org.hibernate.sql.orm.internal.mapping.SingularAttributeImplementor;
import org.hibernate.type.Type;

/**
 * @author Steve Ebersole
 */
public class AttributeReference implements Expression {
	private final SingularAttributeImplementor referencedAttribute;
	private final ColumnBinding[] columnBindings;

	public AttributeReference(
			SingularAttributeImplementor referencedAttribute,
			ColumnBinding[] columnBindings) {
		this.referencedAttribute = referencedAttribute;
		this.columnBindings = columnBindings;
	}

	public SingularAttributeImplementor getReferencedAttribute() {
		return referencedAttribute;
	}

	@Override
	public Type getType() {
		return referencedAttribute.getOrmType();
	}

	public ColumnBinding[] getColumnBindings() {
		return columnBindings;
	}
}
