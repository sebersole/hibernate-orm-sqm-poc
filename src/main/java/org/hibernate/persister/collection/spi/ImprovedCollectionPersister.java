/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.persister.collection.spi;

import java.util.List;

import org.hibernate.persister.collection.CollectionPersister;
import org.hibernate.persister.common.internal.CompositeContainer;
import org.hibernate.persister.common.internal.DatabaseModel;
import org.hibernate.persister.common.internal.DomainMetamodelImpl;
import org.hibernate.persister.common.spi.Column;
import org.hibernate.persister.common.spi.JoinableAttributeContainer;
import org.hibernate.persister.common.spi.OrmTypeExporter;
import org.hibernate.persister.common.spi.PluralAttribute;
import org.hibernate.sql.ast.from.CollectionTableGroup;
import org.hibernate.sql.ast.from.TableSpace;
import org.hibernate.sql.convert.internal.FromClauseIndex;
import org.hibernate.sql.convert.internal.SqlAliasBaseManager;
import org.hibernate.sql.convert.spi.TableGroupProducer;
import org.hibernate.sqm.query.from.SqmFrom;

/**
 * @author Steve Ebersole
 */
public interface ImprovedCollectionPersister
		extends PluralAttribute, TableGroupProducer, OrmTypeExporter, CompositeContainer {
	CollectionPersister getPersister();

	@Override
	JoinableAttributeContainer getAttributeContainer();

	void finishInitialization(DatabaseModel databaseModel, DomainMetamodelImpl domainMetamodel);

	@Override
	CollectionTableGroup buildTableGroup(
			SqmFrom joinedFromElement,
			TableSpace tableSpace,
			SqlAliasBaseManager sqlAliasBaseManager,
			FromClauseIndex fromClauseIndex);

	@Override
	default TableGroupProducer resolveTableGroupProducer() {
		return this;
	}
}
