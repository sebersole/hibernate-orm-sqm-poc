/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.sql.orm.internal.mapping;

import org.hibernate.persister.collection.AbstractCollectionPersister;
import org.hibernate.persister.collection.CollectionPersister;
import org.hibernate.sql.ast.from.CollectionTableGroup;
import org.hibernate.sql.ast.from.DerivedTable;
import org.hibernate.sql.ast.from.PhysicalTable;
import org.hibernate.sql.ast.from.TableSpace;
import org.hibernate.sql.ast.from.Table;
import org.hibernate.sql.gen.internal.FromClauseIndex;
import org.hibernate.sql.gen.internal.SqlAliasBaseManager;
import org.hibernate.sqm.query.from.JoinedFromElement;

/**
 * @author Steve Ebersole
 */
public class ImprovedCollectionPersisterImpl implements ImprovedCollectionPersister {
	private final AbstractCollectionPersister persister;

	public ImprovedCollectionPersisterImpl(CollectionPersister persister) {
		this.persister = (AbstractCollectionPersister) persister;
	}

	@Override
	public CollectionPersister getPersister() {
		return persister;
	}

	@Override
	public CollectionTableGroup getCollectionTableSpecificationGroup(
			JoinedFromElement joinedFromElement,
			TableSpace tableSpace,
			SqlAliasBaseManager sqlAliasBaseManager,
			FromClauseIndex fromClauseIndex) {
		final CollectionTableGroup group = new CollectionTableGroup(
				tableSpace, sqlAliasBaseManager.getSqlAliasBase( joinedFromElement ), persister
		);

		fromClauseIndex.crossReference( joinedFromElement, group );

		final Table drivingTable = makeTableSpecification(
				persister.getTableName(),
				group.getAliasBase() + '_' + 0
		);
		group.setRootTable( drivingTable );

		return group;
	}

	private Table makeTableSpecification(
			String tableExpression,
			String alias) {
		final Table table;
		if ( tableExpression.startsWith( "(" ) && tableExpression.endsWith( ")" ) ) {
			table = new DerivedTable( tableExpression, alias );
		}
		else {
			table = new PhysicalTable( tableExpression, alias );
		}

		return table;
	}
}
