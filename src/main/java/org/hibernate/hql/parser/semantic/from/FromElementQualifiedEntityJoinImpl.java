/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.hql.parser.semantic.from;

import org.hibernate.hql.parser.JoinType;
import org.hibernate.hql.parser.model.EntityTypeDescriptor;

/**
 * @author Steve Ebersole
 */
public class FromElementQualifiedEntityJoinImpl extends AbstractFromElementJoinedImpl {
	private final String entityName;

	public FromElementQualifiedEntityJoinImpl(
			FromElementSpace fromElementSpace,
			String alias,
			EntityTypeDescriptor entityTypeDescriptor,
			JoinType joinType) {
		super( fromElementSpace, alias, entityTypeDescriptor, joinType );
		this.entityName = entityTypeDescriptor.getEntityName();
	}

	public String getEntityName() {
		return entityName;
	}

	@Override
	public EntityTypeDescriptor getTypeDescriptor() {
		return (EntityTypeDescriptor) super.getTypeDescriptor();
	}
}
