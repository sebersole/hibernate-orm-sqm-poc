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

import org.hibernate.persister.common.internal.SingularAttributeBasic;
import org.hibernate.persister.common.internal.SingularAttributeEmbedded;
import org.hibernate.persister.common.internal.SingularAttributeEntity;
import org.hibernate.persister.common.spi.Column;
import org.hibernate.persister.common.spi.SingularAttributeDescriptor;
import org.hibernate.persister.common.spi.Table;
import org.hibernate.persister.entity.internal.IdentifierSimple;
import org.hibernate.persister.entity.spi.ImprovedEntityPersister;
import org.hibernate.sql.ast.expression.domain.ColumnBindingSource;
import org.hibernate.sql.ast.expression.domain.EntityReferenceExpression;
import org.hibernate.sql.convert.spi.NotYetImplementedException;

import org.jboss.logging.Logger;

/**
 * @author Steve Ebersole
 */
public abstract class AbstractTableGroup implements TableGroup, ColumnBindingSource {
	private static final Logger log = Logger.getLogger( AbstractTableGroup.class );

	private final TableSpace tableSpace;
	private final String uid;
	private final String aliasBase;

	private TableBinding rootTableBinding;
	private List<TableJoin> tableJoins;

	public AbstractTableGroup(TableSpace tableSpace, String uid, String aliasBase) {
		this.tableSpace = tableSpace;
		this.uid = uid;
		this.aliasBase = aliasBase;
	}

	@Override
	public TableSpace getTableSpace() {
		return tableSpace;
	}

	@Override
	public String getUid() {
		return uid;
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
	public ColumnBinding[] resolveBindings(SingularAttributeDescriptor attribute) {
		final Column[] columns;
		if ( attribute instanceof SingularAttributeBasic ) {
			columns = attribute.getColumns();
		}
		else if ( attribute instanceof SingularAttributeEntity ) {
			columns = attribute.getColumns();
		}
		else if ( attribute instanceof SingularAttributeEmbedded ) {
			columns = ( (SingularAttributeEmbedded) attribute ).getEmbeddablePersister().collectColumns();
		}
		else if ( attribute instanceof IdentifierSimple ) {
			columns = attribute.getColumns();
		}
		else {
			throw new NotYetImplementedException( "resolveBindings() : " + attribute );
		}

		final ColumnBinding[] bindings = new ColumnBinding[columns.length];
		for ( int i = 0; i < columns.length; i++ ) {
			final TableBinding tableBinding = locateTableBinding( columns[i].getSourceTable() );
			bindings[i] = new ColumnBinding( columns[i], columns[i].getJdbcType(), tableBinding );
		}

		return bindings;
	}

	@Override
	public EntityReferenceExpression resolveEntityReference() {
		final ImprovedEntityPersister improvedEntityPersister = resolveEntityReferenceBase();
		return new EntityReferenceExpression( this, improvedEntityPersister );
	}

	protected abstract ImprovedEntityPersister resolveEntityReferenceBase();

	@Override
	public TableBinding locateTableBinding(Table table) {
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
			tableJoins = new ArrayList<>();
		}
		tableJoins.add( join );
	}
}
