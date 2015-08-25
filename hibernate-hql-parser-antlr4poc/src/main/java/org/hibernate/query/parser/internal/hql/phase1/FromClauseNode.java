/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.query.parser.internal.hql.phase1;

import org.hibernate.sqm.query.from.FromClause;

/**
 * @author Andrea Boriero
 */
public class FromClauseNode {
	FromClause fromClause;
	FromClauseNode parent;

	public FromClauseNode(FromClause fromClause) {
		this.fromClause = fromClause;
	}

	public FromClauseNode(FromClause fromClause, FromClauseNode parent) {
		this.fromClause = fromClause;
		this.parent = parent;
	}

	public FromClause getValue() {
		return fromClause;
	}

	public FromClauseNode getParent() {
		return parent;
	}

	public boolean hasParent() {
		return parent != null;
	}
}
