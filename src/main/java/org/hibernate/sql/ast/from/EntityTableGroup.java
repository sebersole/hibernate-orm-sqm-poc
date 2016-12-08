/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.sql.ast.from;

import java.util.List;

import org.hibernate.internal.util.collections.CollectionHelper;
import org.hibernate.loader.PropertyPath;
import org.hibernate.persister.common.spi.Column;
import org.hibernate.persister.entity.spi.ImprovedEntityPersister;
import org.hibernate.sql.ast.expression.domain.DomainReferenceExpression;

/**
 * A TableSpecificationGroup for an entity reference
 *
 * @author Steve Ebersole
 */
public class EntityTableGroup extends AbstractTableGroup {
	private final ImprovedEntityPersister persister;

	public EntityTableGroup(
			TableSpace tableSpace,
			String uid,
			String aliasBase,
			ImprovedEntityPersister persister,
			PropertyPath propertyPath) {
		super( tableSpace, uid, aliasBase, propertyPath );
		this.persister = persister;
	}

	public ImprovedEntityPersister getPersister() {
		return persister;
	}

	public ColumnBinding[] resolveIdentifierColumnBindings() {
		final Column[] columns = persister.getIdentifierDescriptor().getColumns();

		final TableBinding tableBinding = getRootTableBinding();
		final ColumnBinding[] bindings = new ColumnBinding[columns.length];
		for ( int i = 0; i < columns.length; i++ ) {
			bindings[i] = new ColumnBinding( columns[i], columns[i].getJdbcType(), tableBinding );
		}
		return bindings;
	}

	@Override
	protected ImprovedEntityPersister resolveEntityReferenceBase() {
		return getPersister();
	}

	@Override
	public List<ColumnBinding> resolveColumnBindings(DomainReferenceExpression expression, boolean shallow) {
		final List<Column> columns = expression.getDomainReference().getColumns(
				shallow,
				getPersister().getEntityPersister().getFactory()
		);

		final List<ColumnBinding> bindings = CollectionHelper.arrayList( columns.size() );

		for ( Column column : columns ) {
			bindings.add(
					new ColumnBinding(
							column,
							column.getJdbcType(),
							locateTableBinding( column.getSourceTable() )
					)
			);
		}

		return bindings;
	}
}
