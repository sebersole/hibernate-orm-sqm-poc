/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.hql.parser.semantic.from;

import org.hibernate.hql.parser.JoinType;
import org.hibernate.hql.parser.model.TypeDescriptor;

/**
 * @author Steve Ebersole
 */
public class TreatedJoinedFromElement extends TreatedFromElement implements JoinedFromElement {
	public TreatedJoinedFromElement(JoinedFromElement wrapped, TypeDescriptor treatedAs) {
		super( wrapped, treatedAs );
	}

	@Override
	protected JoinedFromElement getWrapped() {
		return (JoinedFromElement) super.getWrapped();
	}

	@Override
	public JoinType getJoinType() {
		return getWrapped().getJoinType();
	}
}
