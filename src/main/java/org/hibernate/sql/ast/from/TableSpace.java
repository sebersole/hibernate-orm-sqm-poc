/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.sql.ast.from;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.hibernate.AssertionFailure;

import org.jboss.logging.Logger;

/**
 * Represents a groups of joined tables.  Roughly equivalent to what ANSI SQL
 * calls a {@code <table reference>}.
 * <p/>
 * We further group the individual TableSpecification references into groups to be able to
 * more easily refer to all the tables from a single entity/collection persister as a
 * single group.
 *
 * @author Steve Ebersole
 */
public class TableSpace {
	private static final Logger log = Logger.getLogger( TableSpace.class );

	private final FromClause fromClause;

	private TableSpecificationGroup rootTableSpecificationGroup;
	private List<TableSpecificationGroupJoin> joinedTableSpecificationGroups;

	public TableSpace(FromClause fromClause) {
		if ( fromClause == null ) {
			throw new AssertionFailure( "FromClause cannot be null" );
		}
		this.fromClause = fromClause;
	}

	public FromClause getFromClause() {
		return fromClause;
	}

	public TableSpecificationGroup getRootTableSpecificationGroup() {
		return rootTableSpecificationGroup;
	}

	public void setRootTableSpecificationGroup(TableSpecificationGroup rootTableSpecificationGroup) {
		log.tracef(
				"Setting root TableSpecificationGroup [%s] for space [%s] - was %s",
				rootTableSpecificationGroup,
				this,
				this.rootTableSpecificationGroup == null ? "<null>" : this.rootTableSpecificationGroup
		);
		this.rootTableSpecificationGroup = rootTableSpecificationGroup;
	}

	public List<TableSpecificationGroupJoin> getJoinedTableSpecificationGroups() {
		if ( joinedTableSpecificationGroups == null ) {
			return Collections.emptyList();
		}
		else {
			return Collections.unmodifiableList( joinedTableSpecificationGroups );
		}
	}

	public void addJoinedTableSpecificationGroup(TableSpecificationGroupJoin join) {
		log.tracef( "Adding TableSpecificationGroup join [%s] to space [%s]", join, this );
		if ( joinedTableSpecificationGroups == null ) {
			joinedTableSpecificationGroups = new ArrayList<TableSpecificationGroupJoin>();
		}
		joinedTableSpecificationGroups.add( join );
	}
}
