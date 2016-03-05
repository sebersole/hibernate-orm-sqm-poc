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
import org.hibernate.sql.ast.from.TableSpace;
import org.hibernate.sql.ast.from.Table;
import org.hibernate.sql.gen.internal.FromClauseIndex;
import org.hibernate.sql.gen.internal.SqlAliasBaseManager;
import org.hibernate.sql.orm.internal.sqm.model.DomainMetamodelImpl;
import org.hibernate.sqm.domain.ManagedType;
import org.hibernate.sqm.domain.PluralAttribute;
import org.hibernate.sqm.domain.Type;
import org.hibernate.sqm.parser.NotYetImplementedException;
import org.hibernate.sqm.query.from.JoinedFromElement;
import org.hibernate.type.AnyType;
import org.hibernate.type.BasicType;
import org.hibernate.type.EntityType;

/**
 * @author Steve Ebersole
 */
public class ImprovedCollectionPersisterImpl extends AbstractAttributeImpl implements ImprovedCollectionPersister {
	private final AbstractCollectionPersister persister;

	private final CollectionClassification collectionClassification;
	private final PluralAttributeKey foreignKeyDescriptor;
	private final PluralAttributeId idDescriptor;
	private final PluralAttributeElement elementDescriptor;
	private final PluralAttributeIndex indexDescriptor;

	private final AbstractTable separateCollectionTable;

	public ImprovedCollectionPersisterImpl(
			DatabaseModel databaseModel,
			DomainMetamodelImpl domainMetamodel,
			ManagedType declaringType,
			String attributeName,
			CollectionPersister persister,
			Value[] foreignKeyValues) {
		super( declaringType, attributeName );

		this.persister = (AbstractCollectionPersister) persister;
		this.collectionClassification = Helper.interpretCollectionClassification( persister.getCollectionType() );

		this.foreignKeyDescriptor = new PluralAttributeKey(
				persister.getKeyType(),
				domainMetamodel.toSqmType( persister.getKeyType() ),
				foreignKeyValues
		);

		if ( persister.getIdentifierType() == null ) {
			this.idDescriptor = null;
		}
		else {
			this.idDescriptor = new PluralAttributeId(
					(BasicType) persister.getIdentifierType(),
					domainMetamodel.toSqmType( (BasicType) persister.getIdentifierType() ),
					persister.getIdentifierGenerator()
			);
		}

		final AbstractTable collectionTable;
		if ( persister.isOneToMany() ) {
			collectionTable = domainMetamodel.toSqmType( this.persister.getElementPersister() ).getRootTable();
			this.separateCollectionTable = null;
		}
		else {
			collectionTable = makeTableReference( databaseModel, this.persister.getTableName() );
			this.separateCollectionTable = collectionTable;
		}

		if ( !persister.hasIndex() ) {
			this.indexDescriptor = null;
		}
		else {
			final Value[] values = Helper.makeValues(
					collectionTable,
					persister.getIndexType(),
					this.persister.getIndexColumnNames(),
					this.persister.getIndexFormulas()
			);
			this.indexDescriptor = new PluralAttributeIndex(
					persister.getIndexType(),
					domainMetamodel.toSqmType( persister.getIndexType() ),
					values
			);
		}

		this.elementDescriptor = buildElementDescriptor( databaseModel, domainMetamodel );
	}

	private PluralAttributeElement buildElementDescriptor(
			DatabaseModel databaseModel,
			DomainMetamodelImpl domainMetamodel) {
		final org.hibernate.type.Type elementType = persister.getElementType();
		if ( elementType.isAnyType() ) {
			return new PluralAttributeElementAny(
					(AnyType) elementType,
					domainMetamodel.toSqmType( (AnyType) elementType )
			);
		}
		else if ( elementType.isComponentType() ) {
			throw new NotYetImplementedException();
		}
		else if ( elementType.isEntityType() ) {
			return new PluralAttributeElementEntity(
					persister.isManyToMany() ? ElementClassification.MANY_TO_MANY : ElementClassification.ONE_TO_MANY,
					(EntityType) elementType,
					domainMetamodel.toSqmType( (EntityType) elementType )
			);
		}
		else {
			return new PluralAttributeElementBasic(
					(BasicType) elementType,
					domainMetamodel.toSqmType( (BasicType) elementType )
			);
		}
	}

	@Override
	public CollectionClassification getCollectionClassification() {
		return collectionClassification;
	}

	public PluralAttributeKey getForeignKeyDescriptor() {
		return foreignKeyDescriptor;
	}

	public PluralAttributeId getIdDescriptor() {
		return idDescriptor;
	}

	public PluralAttributeElement getElementDescriptor() {
		return elementDescriptor;
	}

	public PluralAttributeIndex getIndexDescriptor() {
		return indexDescriptor;
	}

	@Override
	public ElementClassification getElementClassification() {
		return elementDescriptor.getElementClassification();
	}

	@Override
	public org.hibernate.sqm.domain.BasicType getCollectionIdType() {
		return idDescriptor == null ? null : idDescriptor.getSqmType();
	}

	@Override
	public Type getIndexType() {
		return indexDescriptor == null ? null : indexDescriptor.getSqmType();
	}

	@Override
	public Type getElementType() {
		return elementDescriptor.getSqmType();
	}

	@Override
	public Type getBoundType() {
		return getElementType();
	}

	@Override
	public ManagedType asManagedType() {
		// todo : for now, just let the ClassCastException happen
		return (ManagedType) getBoundType();
	}

	@Override
	public CollectionPersister getPersister() {
		return persister;
	}

	@Override
	public CollectionTableGroup buildTableGroup(
			JoinedFromElement joinedFromElement,
			TableSpace tableSpace,
			SqlAliasBaseManager sqlAliasBaseManager,
			FromClauseIndex fromClauseIndex) {
		final CollectionTableGroup group = new CollectionTableGroup(
				tableSpace, sqlAliasBaseManager.getSqlAliasBase( joinedFromElement ), persister
		);

		fromClauseIndex.crossReference( joinedFromElement, group );

		if ( separateCollectionTable != null ) {
			group.setRootTable(
					new Table( separateCollectionTable, group.getAliasBase() + '_' + 0 )
			);
		}

		if ( getElementDescriptor() instanceof PluralAttributeElementEntity ) {
			final ImprovedEntityPersister elementPersister = (ImprovedEntityPersister) getElementDescriptor().getSqmType();
			elementPersister.addTableJoins( group, joinedFromElement.getJoinType() );
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
}
