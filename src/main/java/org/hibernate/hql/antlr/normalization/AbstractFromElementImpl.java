/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.hql.antlr.normalization;

import org.hibernate.hql.model.TypeDescriptor;

/**
 * @author Steve Ebersole
 */
public abstract class AbstractFromElementImpl implements FromElement {
	private final FromElementSpace fromElementSpace;
	private final String alias;
	private final TypeDescriptor typeDescriptor;

	protected AbstractFromElementImpl(
			FromElementSpace fromElementSpace,
			String alias,
			TypeDescriptor typeDescriptor) {
		this.fromElementSpace = fromElementSpace;
		this.alias = alias;
		this.typeDescriptor = typeDescriptor;
	}

	@Override
	public FromElementSpace getContainingSpace() {
		return fromElementSpace;
	}

	@Override
	public String getAlias() {
		return alias;
	}

	@Override
	public TypeDescriptor getTypeDescriptor() {
		return typeDescriptor;
	}
}
