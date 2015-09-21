/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.sql.orm.internal.mapping;

import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.persister.entity.Queryable;
import org.hibernate.sql.ast.from.DerivedTableSpecification;
import org.hibernate.sql.ast.from.EntityTableSpecificationGroup;
import org.hibernate.sql.ast.from.PhysicalTableSpecification;
import org.hibernate.sql.ast.from.TableSpace;
import org.hibernate.sql.ast.from.TableSpecification;
import org.hibernate.sql.ast.from.TableSpecificationJoin;
import org.hibernate.sql.gen.internal.FromClauseIndex;
import org.hibernate.sql.gen.internal.SqlAliasBaseManager;
import org.hibernate.sqm.query.JoinType;
import org.hibernate.sqm.query.from.FromElement;

/**
 * @author Steve Ebersole
 */
public class ImprovedEntityPersisterImpl implements ImprovedEntityPersister {
	private final EntityPersister persister;
	private final Queryable queryable;
	private final int subclassTableCount;

	public ImprovedEntityPersisterImpl(EntityPersister persister) {
		this.persister = persister;
		this.queryable = (Queryable) persister;
		this.subclassTableCount = Helper.INSTANCE.extractSubclassTableCount( persister );
	}

	@Override
	public EntityPersister getEntityPersister() {
		return persister;
	}

	@Override
	public EntityTableSpecificationGroup getEntityTableSpecificationGroup(
			FromElement fromElement,
			TableSpace tableSpace,
			SqlAliasBaseManager sqlAliasBaseManager,
			FromClauseIndex fromClauseIndex) {
		final EntityTableSpecificationGroup group = new EntityTableSpecificationGroup(
				tableSpace,
				sqlAliasBaseManager.getSqlAliasBase( fromElement ),
				persister
		);

		fromClauseIndex.crossReference( fromElement, group );

		final TableSpecification drivingTable = makeTableSpecification(
				queryable.getSubclassTableName( 0 ),
				group.getAliasBase() + '_' + 0
		);
		group.setRootTableSpecification( drivingTable );

		// todo : determine proper join type
		JoinType joinType = JoinType.LEFT;

		for ( int i = 1; i < subclassTableCount; i++ ) {
			final TableSpecification tableSpecification = makeTableSpecification(
					queryable.getSubclassTableName( i ),
					group.getAliasBase() + '_' + i
			);

			group.addTableSpecificationJoin( new TableSpecificationJoin( joinType, tableSpecification, null ) );
		}

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
