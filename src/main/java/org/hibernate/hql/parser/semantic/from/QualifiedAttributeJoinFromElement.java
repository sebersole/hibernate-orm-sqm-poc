/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.hql.parser.semantic.from;

import org.hibernate.hql.parser.JoinType;
import org.hibernate.hql.parser.model.AttributeDescriptor;
import org.hibernate.hql.parser.semantic.predicate.Predicate;

/**
 * @author Steve Ebersole
 */
public class QualifiedAttributeJoinFromElement
		extends AbstractFromElementJoinedImpl
		implements QualifiedJoinedFromElement {
	private final AttributeDescriptor joinedAttributeDescriptor;
	private final boolean fetched;

	private Predicate onClausePredicate;

	public QualifiedAttributeJoinFromElement(
			FromElementSpace fromElementSpace,
			String alias,
			AttributeDescriptor joinedAttributeDescriptor,
			JoinType joinType,
			boolean fetched) {
		super( fromElementSpace, alias, joinedAttributeDescriptor.getType(), joinType );
		this.joinedAttributeDescriptor = joinedAttributeDescriptor;
		this.fetched = fetched;
	}

	public AttributeDescriptor getJoinedAttributeDescriptor() {
		return joinedAttributeDescriptor;
	}

	public boolean isFetched() {
		return fetched;
	}

	@Override
	public Predicate getOnClausePredicate() {
		return onClausePredicate;
	}

	public Predicate setOnClausePredicate(Predicate predicate) {
		Predicate original = this.onClausePredicate;
		this.onClausePredicate = predicate;
		return original;
	}
}
