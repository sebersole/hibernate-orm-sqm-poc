/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.sql.orm.internal.mapping;

import org.hibernate.persister.collection.AbstractCollectionPersister;
import org.hibernate.persister.collection.CollectionPersister;
import org.hibernate.persister.entity.Queryable;
import org.hibernate.sql.ast.from.CollectionTableSpecificationGroup;
import org.hibernate.sql.ast.from.DerivedTableSpecification;
import org.hibernate.sql.ast.from.EntityTableSpecificationGroup;
import org.hibernate.sql.ast.from.PhysicalTableSpecification;
import org.hibernate.sql.ast.from.TableSpace;
import org.hibernate.sql.ast.from.TableSpecification;
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
	public CollectionTableSpecificationGroup getCollectionTableSpecificationGroup(
			JoinedFromElement joinedFromElement,
			TableSpace tableSpace,
			SqlAliasBaseManager sqlAliasBaseManager,
			FromClauseIndex fromClauseIndex) {
		final CollectionTableSpecificationGroup group = new CollectionTableSpecificationGroup(
				tableSpace, sqlAliasBaseManager.getSqlAliasBase( joinedFromElement ), persister
		);

		fromClauseIndex.crossReference( joinedFromElement, group );

		final TableSpecification drivingTable = makeTableSpecification(
				persister.getTableName(),
				group.getAliasBase() + '_' + 0
		);
		group.setRootTableSpecification( drivingTable );

		return group;
	}

	private TableSpecification makeTableSpecification(
			String tableExpression,
			String alias) {
		final TableSpecification tableSpecification;
		if ( tableExpression.startsWith( "(" ) && tableExpression.endsWith( ")" ) ) {
			tableSpecification = new DerivedTableSpecification( tableExpression, alias );
		}
		else {
			tableSpecification = new PhysicalTableSpecification( tableExpression, alias );
		}

		return tableSpecification;
	}
}
