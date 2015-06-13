/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.hql.antlr.normalization;

import org.hibernate.hql.JoinType;

/**
 * @author Steve Ebersole
 */
public class FromElementQualifiedEntityJoinImpl extends AbstractFromElementJoinedImpl {
	private final String entityName;

	public FromElementQualifiedEntityJoinImpl(
			FromElementSpace fromElementSpace,
			String alias,
			JoinType joinType,
			String entityName) {
		super( fromElementSpace, alias, joinType );
		this.entityName = entityName;
	}

	public String getEntityName() {
		return entityName;
	}
}
