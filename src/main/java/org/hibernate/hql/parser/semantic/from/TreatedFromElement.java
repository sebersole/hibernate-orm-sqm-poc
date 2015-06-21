/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.hql.parser.semantic.from;

import org.hibernate.hql.parser.model.TypeDescriptor;

/**
 * @author Steve Ebersole
 */
public class TreatedFromElement implements FromElement {
	private final FromElement wrapped;
	private final TypeDescriptor treatedAs;

	public TreatedFromElement(FromElement wrapped, TypeDescriptor treatedAs) {
		this.wrapped = wrapped;
		this.treatedAs = treatedAs;
	}

	protected FromElement getWrapped() {
		return wrapped;
	}

	@Override
	public FromElementSpace getContainingSpace() {
		return wrapped.getContainingSpace();
	}

	@Override
	public String getAlias() {
		return wrapped.getAlias();
	}

	@Override
	public TypeDescriptor getTypeDescriptor() {
		return treatedAs;
	}

	@Override
	public void addTreatedAs(TypeDescriptor typeDescriptor) {
		wrapped.addTreatedAs( typeDescriptor );
	}

	public TypeDescriptor getBaseTypeDescriptor() {
		return wrapped.getTypeDescriptor();
	}
}
