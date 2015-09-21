/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.sql.ast.from;

import org.hibernate.sql.IllegalJoinSpecificationException;
import org.hibernate.sql.ast.predicate.Predicate;
import org.hibernate.sqm.query.JoinType;

/**
 * Represents a join between TableSpecifications; roughly equivalent to a SQL join.
 *
 * @author Steve Ebersole
 */
public class TableSpecificationJoin {
	private final JoinType joinType;
	private final TableSpecification joinedTable;
	private final Predicate predicate;

	public TableSpecificationJoin(JoinType joinType, TableSpecification joinedTable, Predicate predicate) {
		this.joinType = joinType;
		this.joinedTable = joinedTable;
		this.predicate = predicate;

		if ( joinType == JoinType.CROSS ) {
			if ( predicate != null ) {
				throw new IllegalJoinSpecificationException( "Cross join cannot include join predicate" );
			}
		}
	}

	public JoinType getJoinType() {
		return joinType;
	}

	public TableSpecification getJoinedTable() {
		return joinedTable;
	}

	public Predicate getJoinPredicate() {
		return predicate;
	}
}
