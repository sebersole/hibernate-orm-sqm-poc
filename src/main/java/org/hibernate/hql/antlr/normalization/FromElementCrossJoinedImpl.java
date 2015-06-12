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
public class FromElementCrossJoinedImpl extends AbstractFromElementImpl implements FromElementJoined {
	private final String entityName;

	public FromElementCrossJoinedImpl(FromElementSpace fromElementSpace, String alias, String entityName) {
		super( fromElementSpace, alias );
		this.entityName = entityName;
	}

	public String getEntityName() {
		return entityName;
	}

	@Override
	public JoinType getJoinType() {
		return JoinType.CROSS;
	}
}
