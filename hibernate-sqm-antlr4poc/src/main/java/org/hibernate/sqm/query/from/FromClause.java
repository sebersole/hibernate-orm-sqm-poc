/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.sqm.query.from;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jboss.logging.Logger;

/**
 * Contract representing a from clause.
 * <p/>
 * The parent/child bit represents sub-queries.  The child from clauses are only used for test assertions,
 * but are left here as it is most convenient to maintain them here versus another structure.
 *
 * @author Steve Ebersole
 */
public class FromClause {
	private static final Logger log = Logger.getLogger( FromClause.class );

	private final FromClause parentFromClause;
	private List<FromClause> childFromClauses;

	private List<FromElementSpace> fromElementSpaces = new ArrayList<FromElementSpace>();

	public FromClause() {
		this.parentFromClause = null;
	}

	public FromClause(FromClause parentFromClause) {
		this.parentFromClause = parentFromClause;
	}

	public FromClause getParentFromClause() {
		return parentFromClause;
	}

	public List<FromClause> getChildFromClauses() {
		if ( childFromClauses == null ) {
			return Collections.emptyList();
		}
		else {
			return Collections.unmodifiableList( childFromClauses );
		}
	}

	public List<FromElementSpace> getFromElementSpaces() {
		return fromElementSpaces;
	}

	public FromClause makeChildFromClause() {
		final FromClause child = new FromClause( this );
		if ( childFromClauses == null ) {
			childFromClauses = new ArrayList<FromClause>();
		}
		childFromClauses.add( child );

		return child;
	}

	public FromElementSpace makeFromElementSpace() {
		final FromElementSpace space = new FromElementSpace( this );
		fromElementSpaces.add( space );
		return space;
	}
}
