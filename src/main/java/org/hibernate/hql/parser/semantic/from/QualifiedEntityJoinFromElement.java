/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.hql.parser.semantic.from;

import org.hibernate.hql.parser.semantic.JoinType;
import org.hibernate.hql.parser.model.EntityTypeDescriptor;
import org.hibernate.hql.parser.semantic.predicate.Predicate;

/**
 * @author Steve Ebersole
 */
public class QualifiedEntityJoinFromElement
		extends AbstractFromElementJoinedImpl
		implements QualifiedJoinedFromElement {
	private final String entityName;

	private Predicate onClausePredicate;

	public QualifiedEntityJoinFromElement(
			FromElementSpace fromElementSpace,
			String alias,
			EntityTypeDescriptor entityTypeDescriptor,
			JoinType joinType) {
		super( fromElementSpace, alias, entityTypeDescriptor, joinType );
		this.entityName = entityTypeDescriptor.getTypeName();
	}

	public String getEntityName() {
		return entityName;
	}

	@Override
	public EntityTypeDescriptor getTypeDescriptor() {
		return (EntityTypeDescriptor) super.getTypeDescriptor();
	}

	@Override
	public Predicate getOnClausePredicate() {
		return onClausePredicate;
	}

	public void setOnClausePredicate(Predicate predicate) {
		this.onClausePredicate = predicate;
	}
}
