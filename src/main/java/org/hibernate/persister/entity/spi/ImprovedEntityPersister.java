/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.persister.entity.spi;

import java.util.List;

import org.hibernate.persister.common.internal.CompositeContainer;
import org.hibernate.persister.common.internal.DatabaseModel;
import org.hibernate.persister.common.internal.DomainMetamodelImpl;
import org.hibernate.persister.common.spi.AbstractTable;
import org.hibernate.persister.common.spi.AttributeContainer;
import org.hibernate.persister.common.spi.Column;
import org.hibernate.persister.common.spi.JoinableAttributeContainer;
import org.hibernate.persister.common.spi.OrmTypeExporter;
import org.hibernate.persister.common.spi.Table;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.sql.ast.from.AbstractTableGroup;
import org.hibernate.sql.ast.from.EntityTableGroup;
import org.hibernate.sql.ast.from.TableSpace;
import org.hibernate.sql.convert.internal.FromClauseIndex;
import org.hibernate.sql.convert.internal.SqlAliasBaseManager;
import org.hibernate.sql.convert.spi.TableGroupProducer;
import org.hibernate.sqm.domain.EntityReference;
import org.hibernate.sqm.query.JoinType;
import org.hibernate.sqm.query.from.SqmFrom;

/**
 * Isolate things we think are involved in an "improved design" for EntityPersister.
 *
 * @author Steve Ebersole
 */
public interface ImprovedEntityPersister
		extends EntityReference, JoinableAttributeContainer, TableGroupProducer, OrmTypeExporter, CompositeContainer {

	/**
	 * Called after all ImprovedEntityPersister instance have been created and
	 * (initially) initialized.
	 *
	 * @todo ultimately this needs to allow for MappedSuperclass supers
	 *
	 * @param superType The super type
	 * @param typeSource
	 * @param databaseModel
	 * @param domainMetamodel
	 */
	void finishInitialization(
			ImprovedEntityPersister superType,
			Object typeSource,
			DatabaseModel databaseModel,
			DomainMetamodelImpl domainMetamodel);

	/**
	 * In integrating this upstream, the methods here would all be part of EntityPersister
	 * but here we cannot do that and therefore still need access to EntityPersister
	 *
	 * @return The ORM EntityPersister
	 */
	EntityPersister getEntityPersister();

	IdentifierDescriptor getIdentifierDescriptor();
	DiscriminatorDescriptor getDiscriminatorDescriptor();
	RowIdDescriptor getRowIdDescriptor();

	@Override
	EntityTableGroup buildTableGroup(
			SqmFrom fromElement,
			TableSpace tableSpace,
			SqlAliasBaseManager sqlAliasBaseManager,
			FromClauseIndex fromClauseIndex);


	/**
	 * @todo prefer not exposing this
	 * 		- currently only used from ImprovedCollectionPersister as the
	 * 			"collection table" for one-to-many elements
	 * 			and for building the CollectionTableGroup for entity
	 * 			elements (one2many, many2many)
	 */
	Table getRootTable();

	/**
	 * @todo prefer not exposing this
	 * 		- again currently only used from ImprovedCollectionPersister to build
	 * 			the CollectionTableGroup for entity elements (one2many, many2many)
	 */
	void addTableJoins(AbstractTableGroup group, JoinType joinType, List<Column> fkColumns, List<Column> fkTargetColumns);

	@Override
	default TableGroupProducer resolveTableGroupProducer() {
		return this;
	}
}
