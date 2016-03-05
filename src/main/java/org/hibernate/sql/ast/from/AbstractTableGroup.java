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

import org.hibernate.sql.gen.NotYetImplementedException;
import org.hibernate.sql.orm.internal.mapping.Column;
import org.hibernate.sql.orm.internal.mapping.SingularAttributeBasic;
import org.hibernate.sql.orm.internal.mapping.SingularAttributeEntity;
import org.hibernate.sql.orm.internal.mapping.Table;
import org.hibernate.sqm.domain.SingularAttribute;

import org.jboss.logging.Logger;

/**
 * @author Steve Ebersole
 */
public abstract class AbstractTableGroup implements TableGroup {
	private static final Logger log = Logger.getLogger( AbstractTableGroup.class );

	private final TableSpace tableSpace;
	private final String aliasBase;

	private TableBinding rootTableBinding;
	private List<TableJoin> tableJoins;

	public AbstractTableGroup(TableSpace tableSpace, String aliasBase) {
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

	public TableBinding getRootTableBinding() {
		return rootTableBinding;
	}

	public void setRootTableBinding(TableBinding rootTableBinding) {
		log.tracef(
				"Setting root TableSpecification for group [%s] : %s (was %s)",
				this.toString(),
				rootTableBinding,
				this.rootTableBinding == null ? "<null>" : this.rootTableBinding
		);
		this.rootTableBinding = rootTableBinding;
	}

	public List<TableJoin> getTableJoins() {
		if ( tableJoins == null ) {
			return Collections.emptyList();
		}
		else {
			return Collections.unmodifiableList( tableJoins );
		}
	}

	@Override
	public ColumnBinding[] resolveAttribute(SingularAttribute attribute) {
		final Column[] columns;
		if ( attribute instanceof SingularAttributeBasic ) {
			columns = ( (SingularAttributeBasic) attribute ).getColumns();
		}
		else if ( attribute instanceof SingularAttributeEntity ) {
			columns = ( (SingularAttributeEntity) attribute ).getColumns();
		}
		else {
			throw new NotYetImplementedException();
		}

		final ColumnBinding[] bindings = new ColumnBinding[columns.length];
		for ( int i = 0; i < columns.length; i++ ) {
			final TableBinding tableBinding = locateTableBinding( columns[i].getSourceTable() );
			bindings[i] = new ColumnBinding( columns[i], tableBinding );
		}
		return bindings;
	}

	private TableBinding locateTableBinding(Table table) {
		if ( table == getRootTableBinding().getTable() ) {
			return getRootTableBinding();
		}

		for ( TableJoin tableJoin : getTableJoins() ) {
			if ( tableJoin.getJoinedTableBinding().getTable() == table ) {
				return tableJoin.getJoinedTableBinding();
			}
		}

		throw new IllegalStateException( "Could not resolve binding for table : " + table );
	}

	public void addTableSpecificationJoin(TableJoin join) {
		log.tracef( "Adding TableSpecification join [%s] to group [%s]", join, this );
		if ( tableJoins == null ) {
			tableJoins = new ArrayList<TableJoin>();
		}
		tableJoins.add( join );
	}
}
