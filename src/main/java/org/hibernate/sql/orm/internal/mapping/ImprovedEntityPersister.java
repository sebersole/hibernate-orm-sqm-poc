/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.sql.orm.internal.mapping;

import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.sql.ast.from.AbstractTableGroup;
import org.hibernate.sql.ast.from.EntityTableGroup;
import org.hibernate.sql.ast.from.TableSpace;
import org.hibernate.sql.gen.internal.FromClauseIndex;
import org.hibernate.sql.gen.internal.SqlAliasBaseManager;
import org.hibernate.sql.orm.internal.sqm.model.SqmTypeImplementor;
import org.hibernate.sqm.domain.EntityType;
import org.hibernate.sqm.query.JoinType;
import org.hibernate.sqm.query.from.FromElement;

/**
 * Isolate things we think are involved in an "improved design" for EntityPersister.
 *
 * @todo : better design of inheritance support/handling
 *
 * @author Steve Ebersole
 */
public interface ImprovedEntityPersister extends EntityType, SqmTypeImplementor {
	/**
	 * In integrating this upstream, the methods here would all be part of EntityPersister
	 * but here we cannot do that and therefore still need access to EntityPersister
	 *
	 * @return The ORM EntityPersister
	 */
	EntityPersister getEntityPersister();

	@Override
	IdentifierDescriptorImplementor getIdentifierDescriptor();

	AbstractTable getRootTable();

	EntityTableGroup buildTableGroup(
			FromElement fromElement,
			TableSpace tableSpace,
			SqlAliasBaseManager sqlAliasBaseManager,
			FromClauseIndex fromClauseIndex);

	void addTableJoins(AbstractTableGroup group, JoinType joinType, Column[] fkColumns, Column[] fkTargetColumns);
}
