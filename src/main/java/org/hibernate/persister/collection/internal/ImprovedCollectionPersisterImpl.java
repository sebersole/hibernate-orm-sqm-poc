/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.persister.collection.internal;

import java.util.List;
import java.util.Optional;

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
import org.hibernate.persister.common.spi.AbstractAttribute;
import org.hibernate.persister.common.spi.AbstractTable;
import org.hibernate.persister.common.spi.Column;
import org.hibernate.persister.common.spi.JoinColumnMapping;
import org.hibernate.persister.common.spi.JoinableAttributeContainer;
import org.hibernate.persister.common.spi.SingularAttribute;
import org.hibernate.persister.entity.Joinable;
import org.hibernate.persister.entity.spi.ImprovedEntityPersister;
import org.hibernate.sql.ast.from.CollectionTableGroup;
import org.hibernate.sql.ast.from.TableBinding;
import org.hibernate.sql.ast.from.TableSpace;
import org.hibernate.sql.convert.internal.FromClauseIndex;
import org.hibernate.sql.convert.internal.SqlAliasBaseManager;
import org.hibernate.sqm.domain.EntityReference;
import org.hibernate.sqm.domain.PluralAttributeElementReference.ElementClassification;
import org.hibernate.sqm.query.JoinType;
import org.hibernate.sqm.query.from.SqmAttributeJoin;
import org.hibernate.sqm.query.from.SqmFrom;
import org.hibernate.type.AnyType;
import org.hibernate.type.BasicType;
import org.hibernate.type.CollectionType;
import org.hibernate.type.CompositeType;
import org.hibernate.type.EntityType;

/**
 * @author Steve Ebersole
 */
public class ImprovedCollectionPersisterImpl extends AbstractAttribute implements ImprovedCollectionPersister {
	private final AbstractCollectionPersister persister;
	private final CollectionClassification collectionClassification;

	private final PluralAttributeKey foreignKeyDescriptor;

	private PluralAttributeId idDescriptor;
	private PluralAttributeElement elementDescriptor;
	private PluralAttributeIndex indexDescriptor;

	private AbstractTable separateCollectionTable;

	public ImprovedCollectionPersisterImpl(
			JoinableAttributeContainer declaringType,
			String attributeName,
			CollectionPersister persister) {
		super( declaringType, attributeName );

		this.persister = (AbstractCollectionPersister) persister;
		this.collectionClassification = Helper.interpretCollectionClassification( persister.getCollectionType() );

		this.foreignKeyDescriptor = new PluralAttributeKey( this );
	}

	@Override
	public JoinableAttributeContainer getAttributeContainer() {
		return (JoinableAttributeContainer) super.getAttributeContainer();
	}

	public AbstractTable getSeparateCollectionTable() {
		return separateCollectionTable;
	}

	@Override
	public void finishInitialization(DatabaseModel databaseModel, DomainMetamodelImpl domainMetamodel) {
		final AbstractTable collectionTable;
		if ( persister.isOneToMany() ) {
			collectionTable = ( (ImprovedEntityPersister) domainMetamodel.resolveEntityReference( this.persister.getElementPersister().getEntityName() ) ).getRootTable();
			this.separateCollectionTable = null;
		}
		else {
			collectionTable = makeCollectionTable( databaseModel, this.persister.getTableName() );
			this.separateCollectionTable = collectionTable;

		}

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
			final List<Column> columns = Helper.makeValues(
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
								this,
								persister.getRole() + ".key",
								(CompositeType) persister.getIndexType(),
								columns
						)
				);
			}
			else if ( persister.getIndexType().isEntityType() ) {
				final EntityType indexTypeEntity = (EntityType) persister.getIndexType();
				this.indexDescriptor = new PluralAttributeIndexEntity(
						this,
						(ImprovedEntityPersister) domainMetamodel.resolveEntityReference( indexTypeEntity.getAssociatedEntityName( domainMetamodel.getSessionFactory() ) ),
						indexTypeEntity,
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

			final List<Column> columns = Helper.makeValues(
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

			final List<Column> columns = Helper.makeValues(
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
							this,
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
			final List<Column> columns = Helper.makeValues(
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

			final List<Column> columns = Helper.makeValues(
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
			SqmFrom sqmFrom,
			TableSpace tableSpace,
			SqlAliasBaseManager sqlAliasBaseManager,
			FromClauseIndex fromClauseIndex) {
		final JoinType joinType;
		if ( SqmAttributeJoin.class.isInstance( sqmFrom ) ) {
			joinType = ( (SqmAttributeJoin) sqmFrom ).getJoinType();
		}
		else {
			joinType = JoinType.INNER;
		}

		final CollectionTableGroup group = new CollectionTableGroup(
				tableSpace,
				sqmFrom.getUniqueIdentifier(),
				sqlAliasBaseManager.getSqlAliasBase( sqmFrom ),
				this,
				Helper.convert( sqmFrom.getPropertyPath() )
		);

		fromClauseIndex.crossReference( sqmFrom, group );

		if ( separateCollectionTable != null ) {
			group.setRootTableBinding( new TableBinding( separateCollectionTable, group.getAliasBase() ) );
		}

		if ( getElementReference() instanceof PluralAttributeElementEntity ) {
			List<Column> fkColumns = null;
			List<Column> fkTargetColumns = null;

			final PluralAttributeElementEntity elementEntity = (PluralAttributeElementEntity) getElementReference();
			final ImprovedEntityPersister elementPersister = elementEntity.getElementPersister();

			if ( separateCollectionTable != null ) {
				fkColumns = elementEntity.getColumns();
				if ( elementEntity.getOrmType().isReferenceToPrimaryKey() ) {
					fkTargetColumns = elementPersister.getIdentifierDescriptor().getColumns();
				}
				else {
					SingularAttribute referencedAttribute = (SingularAttribute) elementPersister.findAttribute( elementEntity.getOrmType().getRHSUniqueKeyPropertyName() );
					fkTargetColumns = referencedAttribute.getColumns();
				}
			}

			elementPersister.addTableJoins(
					group,
					joinType,
					fkColumns,
					fkTargetColumns
			);
		}

		return group;
	}

	private AbstractTable makeCollectionTable(DatabaseModel databaseModel, String tableExpression) {
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
	public CollectionType getOrmType() {
		return persister.getCollectionType();
	}

	@Override
	public String asLoggableText() {
		return toString();
	}

	@Override
	public List<JoinColumnMapping> getJoinColumnMappings() {
		return foreignKeyDescriptor.getJoinColumnMappings();
	}

	@Override
	public Optional<EntityReference> toEntityReference() {
		if ( elementDescriptor instanceof PluralAttributeElementEntity ) {
			return Optional.of( ( (PluralAttributeElementEntity) elementDescriptor ).getElementPersister() );
		}
		else {
			return Optional.empty();
		}
	}
}
