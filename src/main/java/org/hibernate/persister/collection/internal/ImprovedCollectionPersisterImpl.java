/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.persister.collection.internal;

import org.hibernate.persister.collection.AbstractCollectionPersister;
import org.hibernate.persister.collection.CollectionPersister;
import org.hibernate.persister.collection.spi.ImprovedCollectionPersister;
import org.hibernate.persister.collection.spi.PluralAttributeElement;
import org.hibernate.persister.collection.spi.PluralAttributeId;
import org.hibernate.persister.collection.spi.PluralAttributeIndex;
import org.hibernate.persister.collection.spi.PluralAttributeKey;
import org.hibernate.persister.common.internal.DatabaseModel;
import org.hibernate.persister.common.internal.DomainMetamodelImpl;
import org.hibernate.persister.common.internal.Helper;
import org.hibernate.persister.common.spi.AbstractAttributeImpl;
import org.hibernate.persister.common.spi.AbstractTable;
import org.hibernate.persister.common.spi.Column;
import org.hibernate.persister.common.spi.DomainReferenceImplementor;
import org.hibernate.persister.common.spi.SingularAttributeImplementor;
import org.hibernate.persister.entity.spi.ImprovedEntityPersister;
import org.hibernate.sql.ast.from.CollectionTableGroup;
import org.hibernate.sql.ast.from.TableBinding;
import org.hibernate.sql.ast.from.TableSpace;
import org.hibernate.sql.convert.internal.FromClauseIndex;
import org.hibernate.sql.convert.internal.SqlAliasBaseManager;
import org.hibernate.sqm.domain.PluralAttributeReference.ElementReference.ElementClassification;
import org.hibernate.sqm.query.from.SqmAttributeJoin;
import org.hibernate.type.AnyType;
import org.hibernate.type.BasicType;
import org.hibernate.type.CompositeType;
import org.hibernate.type.EntityType;
import org.hibernate.type.Type;

/**
 * @author Steve Ebersole
 */
public class ImprovedCollectionPersisterImpl extends AbstractAttributeImpl implements ImprovedCollectionPersister {
	private final AbstractCollectionPersister persister;
	private final CollectionClassification collectionClassification;

	private PluralAttributeKey foreignKeyDescriptor;
	private PluralAttributeId idDescriptor;
	private PluralAttributeElement elementDescriptor;
	private PluralAttributeIndex indexDescriptor;

	private AbstractTable separateCollectionTable;

	public ImprovedCollectionPersisterImpl(
			DomainReferenceImplementor declaringType,
			String attributeName,
			CollectionPersister persister,
			Column[] foreignKeyColumns) {
		super( declaringType, attributeName );

		this.persister = (AbstractCollectionPersister) persister;
		this.collectionClassification = Helper.interpretCollectionClassification( persister.getCollectionType() );

	}

	@Override
	public void finishInitialization(DatabaseModel databaseModel, DomainMetamodelImpl domainMetamodel) {
		final AbstractTable collectionTable;
		if ( persister.isOneToMany() ) {
			collectionTable = ( (ImprovedEntityPersister) domainMetamodel.resolveEntityReference( this.persister.getElementPersister().getEntityName() ) ).getRootTable();
			this.separateCollectionTable = null;
		}
		else {
			collectionTable = makeTableReference( databaseModel, this.persister.getTableName() );
			this.separateCollectionTable = collectionTable;
		}

		this.foreignKeyDescriptor = new PluralAttributeKey(
				persister.getKeyType(),
				Helper.makeValues(
						domainMetamodel.getSessionFactory(),
						collectionTable,
						persister.getKeyType(),
						persister.getKeyColumnNames(),
						null
				)
		);

		if ( persister.getIdentifierType() == null ) {
			this.idDescriptor = null;
		}
		else {
			this.idDescriptor = new PluralAttributeId(
					(BasicType) persister.getIdentifierType(),
					persister.getIdentifierGenerator()
			);
		}

		if ( !persister.hasIndex() ) {
			this.indexDescriptor = null;
		}
		else {
			final Column[] columns = Helper.makeValues(
					domainMetamodel.getSessionFactory(),
					collectionTable,
					persister.getIndexType(),
					this.persister.getIndexColumnNames(),
					this.persister.getIndexFormulas()
			);
			if ( persister.getIndexType().isComponentType() ) {
				this.indexDescriptor = new PluralAttributeIndexEmbeddable(
						this,
						Helper.INSTANCE.buildEmbeddablePersister(
								databaseModel,
								domainMetamodel,
								persister.getRole() + ".key",
								(CompositeType) persister.getIndexType(),
								columns
						)
				);
			}
			else if ( persister.getIndexType().isEntityType() ) {
				this.indexDescriptor = new PluralAttributeIndexEntity(
						this,
						(EntityType) persister.getIndexType(),
						columns
				);
			}
			else {
				this.indexDescriptor = new PluralAttributeIndexBasic(
						this,
						(BasicType) persister.getIndexType(),
						columns
				);
			}
		}

		this.elementDescriptor = buildElementDescriptor( databaseModel, domainMetamodel );
	}

	private PluralAttributeElement buildElementDescriptor(
			DatabaseModel databaseModel,
			DomainMetamodelImpl domainMetamodel) {
		final org.hibernate.type.Type elementType = persister.getElementType();

		if ( elementType.isAnyType() ) {
			assert separateCollectionTable != null;

			final Column[] columns = Helper.makeValues(
					domainMetamodel.getSessionFactory(),
					separateCollectionTable,
					persister.getElementType(),
					this.persister.getElementColumnNames(),
					null
			);

			return new PluralAttributeElementAny(
					this,
					(AnyType) elementType,
					columns
			);
		}
		else if ( elementType.isComponentType() ) {
			assert separateCollectionTable != null;

			final Column[] columns = Helper.makeValues(
					domainMetamodel.getSessionFactory(),
					separateCollectionTable,
					persister.getElementType(),
					this.persister.getElementColumnNames(),
					null
			);

			return new PluralAttributeElementEmbeddable(
					this,
					Helper.INSTANCE.buildEmbeddablePersister(
							databaseModel,
							domainMetamodel,
							persister.getRole() + ".value",
							(CompositeType) elementType,
							columns
					)
			);
		}
		else if ( elementType.isEntityType() ) {
			// NOTE : this only handles the FK, not the all of the columns for the entity
			final AbstractTable table = separateCollectionTable != null
					? separateCollectionTable
					: ( (ImprovedEntityPersister) domainMetamodel.resolveEntityReference( this.persister.getElementPersister().getEntityName() ) ).getRootTable();
			final Column[] columns = Helper.makeValues(
					domainMetamodel.getSessionFactory(),
					table,
					persister.getElementType(),
					this.persister.getElementColumnNames(),
					null
			);

			return new PluralAttributeElementEntity(
					this,
					(ImprovedEntityPersister) domainMetamodel.resolveEntityReference( persister.getElementPersister().getEntityName() ),
					persister.isManyToMany() ? ElementClassification.MANY_TO_MANY : ElementClassification.ONE_TO_MANY,
					(EntityType) elementType,
					columns
			);
		}
		else {
			assert separateCollectionTable != null;

			final Column[] columns = Helper.makeValues(
					domainMetamodel.getSessionFactory(),
					separateCollectionTable,
					persister.getElementType(),
					this.persister.getElementColumnNames(),
					null
			);

			return new PluralAttributeElementBasic(
					this,
					(BasicType) elementType,
					columns
			);
		}
	}

	@Override
	public CollectionClassification getCollectionClassification() {
		return collectionClassification;
	}

	@Override
	public PluralAttributeKey getForeignKeyDescriptor() {
		return foreignKeyDescriptor;
	}

	@Override
	public PluralAttributeId getIdDescriptor() {
		return idDescriptor;
	}

	@Override
	public PluralAttributeElement getElementReference() {
		return elementDescriptor;
	}

	@Override
	public PluralAttributeIndex getIndexReference() {
		return indexDescriptor;
	}

	@Override
	public CollectionPersister getPersister() {
		return persister;
	}

	@Override
	public CollectionTableGroup buildTableGroup(
			SqmAttributeJoin joinedFromElement,
			TableSpace tableSpace,
			SqlAliasBaseManager sqlAliasBaseManager,
			FromClauseIndex fromClauseIndex) {
		final CollectionTableGroup group = new CollectionTableGroup(
				tableSpace, sqlAliasBaseManager.getSqlAliasBase( joinedFromElement ), this
		);

		fromClauseIndex.crossReference( joinedFromElement, group );

		if ( separateCollectionTable != null ) {
			group.setRootTableBinding( new TableBinding( separateCollectionTable, group.getAliasBase() ) );
		}

		if ( getElementReference() instanceof PluralAttributeElementEntity ) {
			Column[] fkColumns = null;
			Column[] fkTargetColumns = null;

			final PluralAttributeElementEntity elementEntity = (PluralAttributeElementEntity) getElementReference();
			final ImprovedEntityPersister elementPersister = elementEntity.getElementPersister();

			if ( separateCollectionTable != null ) {
				fkColumns = elementEntity.getColumns();
				if ( elementEntity.getOrmType().isReferenceToPrimaryKey() ) {
					fkTargetColumns = elementPersister.getIdentifierDescriptor().getColumns();
				}
				else {
					SingularAttributeImplementor referencedAttribute = (SingularAttributeImplementor) elementPersister.findAttribute( elementEntity.getOrmType().getRHSUniqueKeyPropertyName() );
					fkTargetColumns = referencedAttribute.getColumns();
				}
			}

			elementPersister.addTableJoins(
					group,
					joinedFromElement.getJoinType(),
					fkColumns,
					fkTargetColumns
			);
		}

		return group;
	}

	private AbstractTable makeTableReference(DatabaseModel databaseModel, String tableExpression) {
		final AbstractTable table;
		if ( tableExpression.startsWith( "(" ) && tableExpression.endsWith( ")" ) ) {
			table = databaseModel.createDerivedTable( tableExpression );
		}
		else if ( tableExpression.startsWith( "select" ) ) {
			table = databaseModel.createDerivedTable( tableExpression );
		}
		else {
			table = databaseModel.findOrCreatePhysicalTable( tableExpression );
		}
		return table;
	}

	@Override
	public String toString() {
		return "ImprovedCollectionPersister(" + persister.getRole() + ")";
	}

	@Override
	public Type getOrmType() {
		return persister.getType();
	}

	@Override
	public String asLoggableText() {
		return toString();
	}
}
