/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.hql.antlr.normalization;

import org.hibernate.hql.model.EntityTypeDescriptor;

/**
 * @author Steve Ebersole
 */
public class FromElementRootEntity extends AbstractFromElementImpl {
	private final String entityName;

	public FromElementRootEntity(
			FromElementSpace fromElementSpace,
			String alias,
			EntityTypeDescriptor entityTypeDescriptor) {
		super( fromElementSpace, alias, entityTypeDescriptor );
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
