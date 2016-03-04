/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.sql.orm.internal.mapping;

import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.persister.entity.Queryable;
import org.hibernate.sql.ast.from.DerivedTable;
import org.hibernate.sql.ast.from.EntityTableGroup;
import org.hibernate.sql.ast.from.PhysicalTable;
import org.hibernate.sql.ast.from.TableSpace;
import org.hibernate.sql.ast.from.Table;
import org.hibernate.sql.ast.from.TableJoin;
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
	public EntityTableGroup getEntityTableSpecificationGroup(
			FromElement fromElement,
			TableSpace tableSpace,
			SqlAliasBaseManager sqlAliasBaseManager,
			FromClauseIndex fromClauseIndex) {

		// todo : limit inclusion of subclass tables.
		// 		we should only include subclass tables in very specific circumstances (such
		// 		as handling persister reference in select clause, JPQL TYPE cast, subclass attribute
		// 		de-reference, etc).  In other cases it is an unnecessary overhead to include those
		// 		table joins
		//
		// however... the easiest way to accomplish this is during the SQM building to have each FromElement
		//		keep track of all needed subclass references.  The problem is that that gets tricky with the
		// 		design goal of having SQM be completely independent from ORM.  It basically means we will end
		// 		up needing to expose more model and mapping information in the org.hibernate.sqm.domain.ModelMetadata
		// 		contracts
		//
		// Another option would be to have exposed methods on TableSpecificationGroup to "register"
		//		path dereferences as we interpret SQM.  The idea being that we'd capture the need for
		//		certain subclasses as we interpret the SQM into SQL-AST via this registration.  However
		//		since

		final EntityTableGroup group = new EntityTableGroup(
				tableSpace,
				sqlAliasBaseManager.getSqlAliasBase( fromElement ),
				persister
		);

		fromClauseIndex.crossReference( fromElement, group );

		final Table drivingTable = makeTableSpecification(
				queryable.getSubclassTableName( 0 ),
				group.getAliasBase() + '_' + 0
		);
		group.setRootTable( drivingTable );

		// todo : determine proper join type
		JoinType joinType = JoinType.LEFT;

		for ( int i = 1; i < subclassTableCount; i++ ) {
			final Table table = makeTableSpecification(
					queryable.getSubclassTableName( i ),
					group.getAliasBase() + '_' + i
			);

			group.addTableSpecificationJoin( new TableJoin( joinType, table, null ) );
		}

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
