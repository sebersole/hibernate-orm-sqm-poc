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

import org.jboss.logging.Logger;

/**
 * @author Steve Ebersole
 */
public abstract class AbstractTableSpecificationGroup implements TableSpecificationGroup {
	private static final Logger log = Logger.getLogger( AbstractTableSpecificationGroup.class );

	private final TableSpace tableSpace;
	private final String aliasBase;

	private TableSpecification rootTableSpecification;
	private List<TableSpecificationJoin> tableSpecificationJoins;

	public AbstractTableSpecificationGroup(TableSpace tableSpace, String aliasBase) {
		this.tableSpace = tableSpace;
		this.aliasBase = aliasBase;
	}

	@Override
	public TableSpace getTableSpace() {
		return tableSpace;
	}

	@Override
	public String getAliasBase() {
		return aliasBase;
	}

	@Override
	public TableSpecification getRootTableSpecification() {
		return rootTableSpecification;
	}

	public void setRootTableSpecification(TableSpecification rootTableSpecification) {
		log.tracef(
				"Setting root TableSpecification for group [%s] : %s (was %s)",
				this.toString(),
				rootTableSpecification,
				this.rootTableSpecification == null ? "<null>" : this.rootTableSpecification
		);
		this.rootTableSpecification = rootTableSpecification;
	}

	@Override
	public List<TableSpecificationJoin> getTableSpecificationJoins() {
		if ( tableSpecificationJoins == null ) {
			return Collections.emptyList();
		}
		else {
			return Collections.unmodifiableList( tableSpecificationJoins );
		}
	}

	public void addTableSpecificationJoin(TableSpecificationJoin join) {
		log.tracef( "Adding TableSpecification join [%s] to group [%s]", join, this );
		if ( tableSpecificationJoins == null ) {
			tableSpecificationJoins = new ArrayList<TableSpecificationJoin>();
		}
		tableSpecificationJoins.add( join );
	}
}
