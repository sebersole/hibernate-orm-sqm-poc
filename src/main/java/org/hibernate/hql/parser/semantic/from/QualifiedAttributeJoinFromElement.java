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
public class QualifiedAttributeJoinFromElement extends AbstractFromElementJoinedImpl {
	private final String joinedAttribute;
	private final boolean fetched;

	public QualifiedAttributeJoinFromElement(
			FromElementSpace fromElementSpace,
			String alias,
			TypeDescriptor attributeTypeDescriptor,
			JoinType joinType,
			String joinedAttribute,
			boolean fetched) {
		super( fromElementSpace, alias, attributeTypeDescriptor, joinType );
		this.joinedAttribute = joinedAttribute;
		this.fetched = fetched;
	}

	public String getJoinedAttribute() {
		return joinedAttribute;
	}

	public boolean isFetched() {
		return fetched;
	}
}
