/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.sql.ast.from;


import java.util.ArrayList;
import java.util.List;

import org.hibernate.loader.PropertyPath;
import org.hibernate.persister.collection.internal.PluralAttributeElementEntity;
import org.hibernate.persister.collection.spi.ImprovedCollectionPersister;
import org.hibernate.persister.common.spi.Column;
import org.hibernate.persister.entity.spi.ImprovedEntityPersister;
import org.hibernate.sql.ast.expression.domain.DomainReferenceExpression;
import org.hibernate.sql.ast.expression.domain.PluralAttributeElementReferenceExpression;
import org.hibernate.sql.ast.expression.domain.PluralAttributeIndexReferenceExpression;

/**
 * A TableSpecificationGroup for a collection reference
 *
 * @author Steve Ebersole
 */
public class CollectionTableGroup extends AbstractTableGroup {
	private final ImprovedCollectionPersister persister;

	public CollectionTableGroup(
			TableSpace tableSpace,
			String uid,
			String aliasBase,
			ImprovedCollectionPersister persister,
			PropertyPath propertyPath) {
		super( tableSpace, uid, aliasBase, propertyPath );
		this.persister = persister;
	}

	public ImprovedCollectionPersister getPersister() {
		return persister;
	}

	public ColumnBinding[] resolveKeyColumnBindings() {
		final Column[] columns = persister.getForeignKeyDescriptor().getForeignKeyColumns();

		final TableBinding tableBinding = getRootTableBinding();
		final ColumnBinding[] bindings = new ColumnBinding[columns.length];
		for ( int i = 0; i < columns.length; i++ ) {
			bindings[i] = new ColumnBinding( columns[i], columns[i].getJdbcType(), tableBinding );
		}
		return bindings;
	}

	@Override
	protected ImprovedEntityPersister resolveEntityReferenceBase() {
		if ( persister.getElementReference() instanceof PluralAttributeElementEntity ) {
			return ( (PluralAttributeElementEntity) persister.getElementReference() ).getElementPersister();
		}

		return null;
	}

	@Override
	public List<ColumnBinding> resolveColumnBindings(DomainReferenceExpression expression, boolean shallow) {
		if ( expression instanceof PluralAttributeElementReferenceExpression ) {
			final PluralAttributeElementReferenceExpression elementExpression = (PluralAttributeElementReferenceExpression) expression;
			return createColumnBindings(
					elementExpression.getDomainReference().getColumns(
							shallow,
							persister.getPersister().getFactory()
					)
			);
		}
		else if ( expression instanceof PluralAttributeIndexReferenceExpression ) {
			final PluralAttributeIndexReferenceExpression indexExpression = (PluralAttributeIndexReferenceExpression) expression;
			return createColumnBindings(
					indexExpression.getDomainReference().getColumns( shallow, persister.getPersister().getFactory() )
			);
		}
		else {
			throw new IllegalArgumentException(
					"Illegal DomainReferenceExpression [" + expression + "] for ColumnBinding resolution via CollectionTableGroup"
			);
		}
	}

	private List<ColumnBinding> createColumnBindings(List<Column> columns) {
		final List<ColumnBinding> bindings = new ArrayList<>();
		for ( Column column : columns ) {
			bindings.add( createBinding( column ) );
		}
		return bindings;
	}

	private ColumnBinding createBinding(Column column) {
		return new ColumnBinding(
				column,
				column.getJdbcType(),
				locateTableBinding( column.getSourceTable() )
		);
	}
}
