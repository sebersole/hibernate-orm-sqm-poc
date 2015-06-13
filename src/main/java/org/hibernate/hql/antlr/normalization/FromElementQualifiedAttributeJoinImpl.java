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
public class FromElementQualifiedAttributeJoinImpl extends AbstractFromElementJoinedImpl {
	private final String joinedAttribute;
	private final boolean fetched;

	public FromElementQualifiedAttributeJoinImpl(
			FromElementSpace fromElementSpace,
			String alias,
			JoinType joinType,
			String joinedAttribute,
			boolean fetched) {
		super( fromElementSpace, alias, joinType );
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
