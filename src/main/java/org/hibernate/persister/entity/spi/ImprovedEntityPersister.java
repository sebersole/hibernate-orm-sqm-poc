/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.persister.entity.spi;

import org.hibernate.persister.common.internal.DatabaseModel;
import org.hibernate.persister.common.internal.DomainMetamodelImpl;
import org.hibernate.persister.common.spi.AbstractTable;
import org.hibernate.persister.common.spi.Column;
import org.hibernate.persister.common.spi.DomainReferenceImplementor;
import org.hibernate.persister.common.spi.OrmTypeExporter;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.sql.ast.from.AbstractTableGroup;
import org.hibernate.sql.ast.from.ColumnBinding;
import org.hibernate.sql.ast.from.EntityTableGroup;
import org.hibernate.sql.ast.from.TableBinding;
import org.hibernate.sql.ast.from.TableSpace;
import org.hibernate.sql.convert.internal.FromClauseIndex;
import org.hibernate.sql.convert.internal.SqlAliasBaseManager;
import org.hibernate.sqm.domain.EntityReference;
import org.hibernate.sqm.query.JoinType;
import org.hibernate.sqm.query.from.SqmFrom;

/**
 * Isolate things we think are involved in an "improved design" for EntityPersister.
 *
 * @author Steve Ebersole
 */
public interface ImprovedEntityPersister extends EntityReference, DomainReferenceImplementor,
		OrmTypeExporter, AttributeReferenceSource {
	/**
	 * In integrating this upstream, the methods here would all be part of EntityPersister
	 * but here we cannot do that and therefore still need access to EntityPersister
	 *
	 * @return The ORM EntityPersister
	 */
	EntityPersister getEntityPersister();

	IdentifierDescriptor getIdentifierDescriptor();

	AbstractTable getRootTable();

	EntityTableGroup buildTableGroup(
			SqmFrom fromElement,
			TableSpace tableSpace,
			SqlAliasBaseManager sqlAliasBaseManager,
			FromClauseIndex fromClauseIndex);

	void addTableJoins(AbstractTableGroup group, JoinType joinType, Column[] fkColumns, Column[] fkTargetColumns);

	void finishInitialization(
			ImprovedEntityPersister superType,
			Object typeSource,
			DatabaseModel databaseModel,
			DomainMetamodelImpl domainMetamodel);

	ColumnBinding[] resolveColumnBindings(TableBinding tableBinding, boolean shallow);
}
