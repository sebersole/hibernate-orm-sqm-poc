/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.hql.antlr.normalization;

/**
 * @author Steve Ebersole
 */
public abstract class AbstractFromElementImpl implements FromElement {
	private final FromElementSpace fromElementSpace;
	private final String alias;

	protected AbstractFromElementImpl(FromElementSpace fromElementSpace, String alias) {
		this.fromElementSpace = fromElementSpace;
		this.alias = alias;
	}

	@Override
	public FromElementSpace getContainingSpace() {
		return fromElementSpace;
	}

	@Override
	public String getAlias() {
		return alias;
	}
}
