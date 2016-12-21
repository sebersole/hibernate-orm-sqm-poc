/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.persister.entity.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.hibernate.HibernateException;
import org.hibernate.cache.spi.access.EntityRegionAccessStrategy;
import org.hibernate.cache.spi.access.NaturalIdRegionAccessStrategy;
import org.hibernate.internal.util.collections.CollectionHelper;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.persister.common.internal.DatabaseModel;
import org.hibernate.persister.common.internal.DomainMetamodelImpl;
import org.hibernate.persister.common.internal.PersisterHelper;
import org.hibernate.persister.common.internal.PhysicalTable;
import org.hibernate.persister.common.internal.SingularAttributeEmbedded;
import org.hibernate.persister.common.internal.SingularAttributeEntity;
import org.hibernate.persister.common.internal.UnionSubclassTable;
import org.hibernate.persister.common.spi.AbstractAttribute;
import org.hibernate.persister.common.spi.AbstractTable;
import org.hibernate.persister.common.spi.Attribute;
import org.hibernate.persister.common.spi.AttributeContainer;
import org.hibernate.persister.common.spi.Column;
import org.hibernate.persister.common.spi.JoinColumnMapping;
import org.hibernate.persister.common.spi.JoinableAttribute;
import org.hibernate.persister.common.spi.PluralAttribute;
import org.hibernate.persister.common.spi.SingularAttribute;
import org.hibernate.persister.common.spi.Table;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.persister.entity.Loadable;
import org.hibernate.persister.entity.OuterJoinLoadable;
import org.hibernate.persister.entity.Queryable;
import org.hibernate.persister.entity.UnionSubclassEntityPersister;
import org.hibernate.persister.entity.spi.DiscriminatorDescriptor;
import org.hibernate.persister.entity.spi.IdentifierDescriptor;
import org.hibernate.persister.entity.spi.ImprovedEntityPersister;
import org.hibernate.persister.entity.spi.RowIdDescriptor;
import org.hibernate.persister.spi.PersisterCreationContext;
import org.hibernate.sql.NotYetImplementedException;
import org.hibernate.sql.ast.expression.ColumnBindingExpression;
import org.hibernate.sql.ast.from.AbstractTableGroup;
import org.hibernate.sql.ast.from.ColumnBinding;
import org.hibernate.sql.ast.from.EntityTableGroup;
import org.hibernate.sql.ast.from.TableBinding;
import org.hibernate.sql.ast.from.TableJoin;
import org.hibernate.sql.ast.from.TableSpace;
import org.hibernate.sql.ast.predicate.Junction;
import org.hibernate.sql.ast.predicate.RelationalPredicate;
import org.hibernate.sql.convert.internal.FromClauseIndex;
import org.hibernate.sql.convert.internal.SqlAliasBaseManager;
import org.hibernate.sqm.domain.EntityReference;
import org.hibernate.sqm.domain.SingularAttributeReference.SingularAttributeClassification;
import org.hibernate.sqm.query.JoinType;
import org.hibernate.sqm.query.from.SqmFrom;
import org.hibernate.type.BasicType;
import org.hibernate.type.CompositeType;
import org.hibernate.type.ForeignKeyDirection;
import org.hibernate.type.OneToOneType;

import org.jboss.logging.Logger;

/**
 * @author Steve Ebersole
 */
public class ImprovedEntityPersisterImpl implements ImprovedEntityPersister {
	private static final Logger log = Logger.getLogger( ImprovedEntityPersisterImpl.class );

	private final EntityPersister persister;

	private Table[] tables;

	private ImprovedEntityPersisterImpl superType;
	private IdentifierDescriptor identifierDescriptor;
	private RowIdDescriptor rowIdDescriptor;
	private DiscriminatorDescriptor discriminatorDescriptor;

	private final Map<String, AbstractAttribute> attributeMap = new HashMap<>();
	private final List<AbstractAttribute> attributeList = new ArrayList<>();

	public ImprovedEntityPersisterImpl(EntityPersister persister) {
		this.persister = persister;
	}

	private boolean initComplete = false;

	public ImprovedEntityPersisterImpl(
			EntityPersister legacyPersister,
			PersistentClass entityBinding,
			EntityRegionAccessStrategy entityCacheAccessStrategy,
			NaturalIdRegionAccessStrategy naturalIdCacheAccessStrategy,
			PersisterCreationContext creationContext) {
		this( legacyPersister );

		// todo : start moving more of the existing EntityPersister methods to this interface
		//		^^ this we allow us to better deduce SecondaryTables, etc to better build the understanding
		//			of that model here.
		// todo : So... how to model SecondaryTables in the org.hibernate.persister.common.spi.Table model?
		//		specialized Table type (like UnionSubclassTable)?  Does the distinction matter here at all?

		// *so far we only have tests against persistence-inheritance entities so far as SQM building
		//		todo : we need tests to make sure queries against entities with persistence defined inheritance work at the SQL level
		//
	}

	@Override
	public void finishInitialization(
			ImprovedEntityPersister superType,
			Object typeSource,
			DatabaseModel databaseModel,
			DomainMetamodelImpl domainMetamodel) {
		if ( initComplete ) {
			throw new IllegalStateException( "Persister already completed initialization" );
		}

		// do init

		this.superType = (ImprovedEntityPersisterImpl) superType;
		final Queryable queryable = (Queryable) persister;
		final OuterJoinLoadable ojlPersister = (OuterJoinLoadable) persister;

		if ( persister instanceof UnionSubclassEntityPersister ) {
			// todo : we will need a way to capture both the union query (used for selections) versus the physical tables (used for DML)
			//		maybe keep both sets and then define buildTableGroup to accept a selector (boolean) as
			//		to which set of tables to use.
			//
			// 		physicalTables versus selectionTables is the initial design to capturing the first part.
			//
			//		in buildTableGroup (below) we will need to add the selector to leverage that.
			//
			//		this approach has one drawback - it forces us to duplicate the Column definition
			//		in both sets of Tables for UnionSubclassEntityPersister to account for Column#getSourceTable
			tables = new Table[1];
			tables[0] = resolveUnionSubclassTables( this, databaseModel );
		}
		else {
			// for now we treat super, self and sub attributes here just as EntityPersister does
			// ultimately would be better to split that across the specific persister impls and link them imo
			final int subclassTableCount = PersisterHelper.INSTANCE.extractSubclassTableCount( persister );
			this.tables = new Table[subclassTableCount];

			tables[0] = makeTableReference( databaseModel, queryable.getSubclassTableName( 0 ) );
			for ( int i = 1; i < subclassTableCount; i++ ) {
				tables[i] = makeTableReference( databaseModel, queryable.getSubclassTableName( i ) );
			}
		}

		final ImprovedEntityPersister rootEntityPersister = findRootEntityPersister();
		if ( rootEntityPersister == null || rootEntityPersister == this ) {
			final List<Column> idColumns = PersisterHelper.makeValues(
					domainMetamodel.getSessionFactory(),
					persister.getIdentifierType(), ojlPersister.getIdentifierColumnNames(), null, tables[0]
			);

			if ( persister.getIdentifierType() instanceof BasicType ) {
				identifierDescriptor = new IdentifierSimple(
						this,
						persister.getIdentifierPropertyName(),
						(BasicType) persister.getIdentifierType(),
						idColumns
				);
			}
			else {
				final CompositeType cidType = (CompositeType) persister.getIdentifierType();
				// todo : need to pass along that any built sub attributes are part of the id
				if ( persister.hasIdentifierProperty() ) {
					identifierDescriptor = new IdentifierCompositeAggregated(
							this,
							persister.getIdentifierPropertyName(),
							PersisterHelper.INSTANCE.buildEmbeddablePersister(
									databaseModel,
									domainMetamodel,
									this,
									persister.getEntityName() + '.' + persister.getIdentifierPropertyName(),
									cidType,
									idColumns
							)
					);
				}
				else {
					identifierDescriptor = new IdentifierCompositeNonAggregated(
							this,
							PersisterHelper.INSTANCE.buildEmbeddablePersister(
									databaseModel,
									domainMetamodel,
									this,
									persister.getEntityName() + ".id",
									cidType,
									idColumns
							)
					);
				}
			}
		}
		else {
			this.identifierDescriptor = rootEntityPersister.getIdentifierDescriptor();
		}

		final Loadable loadable = (Loadable) persister;
		if ( loadable.hasRowId() ) {
			rowIdDescriptor = new RowIdDescriptorImpl( this );
		}

		if ( loadable.getDiscriminatorType() != null ) {
			this.discriminatorDescriptor = new DiscriminatorDescriptorImpl( this );
		}

//		final int fullAttributeCount = ( ojlPersister ).countSubclassProperties();
		final int fullAttributeCount = ( ojlPersister ).getPropertyTypes().length;
		for ( int attributeNumber = 0; attributeNumber < fullAttributeCount; attributeNumber++ ) {
//			final String attributeName = ojlPersister.getSubclassPropertyName( attributeNumber );
			final String attributeName = ojlPersister.getPropertyNames()[ attributeNumber ];
			log.tracef( "Starting building of Entity attribute : %s#%s", persister.getEntityName(), attributeName );

//			final org.hibernate.type.Type attributeType = ojlPersister.getSubclassPropertyType( attributeNumber );
			final org.hibernate.type.Type attributeType = ojlPersister.getPropertyTypes()[ attributeNumber ];

			final Table table;
			if ( persister instanceof UnionSubclassEntityPersister ) {
				table = tables[0];
			}
			else {
//				final int tableIndex = Helper.INSTANCE.getSubclassPropertyTableNumber(
//						persister,
//						attributeNumber
//				);
//				table = tables[tableIndex];
				table = PersisterHelper.INSTANCE.getPropertyTable(
						persister,
						attributeName,
						tables
				);
			}

			final String[] columns = PersisterHelper.INSTANCE.getSubclassPropertyColumnExpressions( persister, attributeNumber );
			final String[] formulas = PersisterHelper.INSTANCE.getSubclassPropertyFormulaExpressions(
					persister,
					attributeNumber
			);
			final List<Column> values = PersisterHelper.makeValues(
					domainMetamodel.getSessionFactory(),
					attributeType,
					columns,
					formulas,
					table
			);

			final AbstractAttribute attribute;
			if ( attributeType.isCollectionType() ) {
				attribute = PersisterHelper.INSTANCE.buildPluralAttribute(
						databaseModel,
						domainMetamodel,
						this,
						attributeName,
						attributeType
				);
			}
			else {
				attribute = PersisterHelper.INSTANCE.buildSingularAttribute(
						databaseModel,
						domainMetamodel,
						this,
						attributeName,
						attributeType,
						values
				);
			}

			attributeMap.put( attributeName, attribute );
			attributeList.add( attribute );
		}

		initComplete = true;
	}

	@Override
	public ImprovedEntityPersister getSuperEntityPersister() {
		return superType;
	}

	private ImprovedEntityPersister findRootEntityPersister() {
		ImprovedEntityPersisterImpl lastNonNullSuperPersister = this;
		ImprovedEntityPersisterImpl superPersister = superType;
		while ( superPersister != null ) {
			lastNonNullSuperPersister = superPersister;
			superPersister = superPersister.superType;
		}
		return lastNonNullSuperPersister;
	}

	private UnionSubclassTable resolveUnionSubclassTables(
			ImprovedEntityPersisterImpl persister,
			DatabaseModel databaseModel) {
		assert persister.getEntityPersister() instanceof UnionSubclassEntityPersister;

		// UnionSubclassEntityPersister#getTableName returns the union query
		final String unionQuery = ( (UnionSubclassEntityPersister) persister.getEntityPersister() ).getTableName();
		// UnionSubclassEntityPersister#getRootTableName returns the physical table name
		final PhysicalTable physicalTable = databaseModel.findOrCreatePhysicalTable(
				( (UnionSubclassEntityPersister) persister.getEntityPersister() ).getRootTableName()
		);

		if ( persister.superType == null ) {
			// we have reached the root
			return new UnionSubclassTable( unionQuery, physicalTable, null );
		}

		if ( persister.superType .getEntityPersister()instanceof UnionSubclassEntityPersister ) {
			return new UnionSubclassTable(
					unionQuery,
					physicalTable,
					resolveUnionSubclassTables( persister.superType, databaseModel )
			);
		}

		throw new HibernateException(
				"Could not determine how to resolve union-subclass UnionSubclassTable for super-type [" +
						persister.superType.getEntityName() + " <- " + persister.getEntityName() + "]"
		);
	}

	private AbstractTable makeTableReference(DatabaseModel databaseModel, String tableExpression) {
		if ( tableExpression.trim().startsWith( "select" ) || tableExpression.trim().contains( "( select" ) ) {
			// fugly, but when moved into persister we would know from mapping metamodel which type.
			return databaseModel.createDerivedTable( tableExpression );
		}
		else {
			return databaseModel.findOrCreatePhysicalTable( tableExpression );
		}
	}

	@Override
	public EntityPersister getEntityPersister() {
		return persister;
	}

	@Override
	public IdentifierDescriptor getIdentifierDescriptor() {
		return identifierDescriptor;
	}

	@Override
	public DiscriminatorDescriptor getDiscriminatorDescriptor() {
		return discriminatorDescriptor;
	}

	@Override
	public RowIdDescriptor getRowIdDescriptor() {
		return rowIdDescriptor;
	}

	@Override
	public Table getRootTable() {
		return tables[0];
	}

	@Override
	public EntityTableGroup buildTableGroup(
			SqmFrom fromElement,
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
				fromElement.getUniqueIdentifier(),
				sqlAliasBaseManager.getSqlAliasBase( fromElement ),
				this,
				PersisterHelper.convert( fromElement.getPropertyPath() )
		);

		fromClauseIndex.crossReference( fromElement, group );


		final TableBinding drivingTableBinding = new TableBinding( tables[0], group.getAliasBase() );
		group.setRootTableBinding( drivingTableBinding );

		// todo : determine proper join type
		JoinType joinType = JoinType.LEFT;
		addNonRootTables( group, joinType, 0, drivingTableBinding );

		return group;
	}

	private void addNonRootTables(
			AbstractTableGroup group,
			JoinType joinType,
			int baseAdjust,
			TableBinding entityRootTableBinding) {
		for ( int i = 1; i < tables.length; i++ ) {
			final TableBinding tableBinding = new TableBinding(
					tables[i],
					group.getAliasBase() + '_' + ( i + ( baseAdjust - 1 ) )
			);
			group.addTableSpecificationJoin( new TableJoin( joinType, tableBinding, null ) );
		}
	}

	@Override
	public AttributeContainer getSuperAttributeContainer() {
		return superType;
	}

	@Override
	public List<Attribute> getNonIdentifierAttributes() {
		final List<Attribute> rtn = new ArrayList<>();
		collectNonIdentifierAttributes( rtn );
		return rtn;
	}

	private void collectNonIdentifierAttributes(List<Attribute> list) {
		if ( superType != null ) {
			superType.collectNonIdentifierAttributes( list );
		}

		list.addAll( attributeList );
	}

	public void addTableJoins(
			AbstractTableGroup group,
			JoinType joinType,
			List<Column> fkColumns,
			List<Column> fkTargetColumns) {
		final int baseAdjust;
		final TableBinding drivingTableBinding;

		if ( group.getRootTableBinding() == null ) {
			assert fkColumns == null && fkTargetColumns == null;

			baseAdjust = 0;
			drivingTableBinding = new TableBinding( tables[0], group.getAliasBase() );
			group.setRootTableBinding( drivingTableBinding );
		}
		else {
			assert fkColumns.size() == fkTargetColumns.size();

			baseAdjust = 1;
			drivingTableBinding = new TableBinding( tables[0], group.getAliasBase() + '_' + 0 );

			final Junction joinPredicate = new Junction( Junction.Nature.CONJUNCTION );
			for ( int i = 0; i < fkColumns.size(); i++ ) {
				joinPredicate.add(
						new RelationalPredicate(
								RelationalPredicate.Operator.EQUAL,
								new ColumnBindingExpression(
										new ColumnBinding(
												fkColumns.get( i ),
												group.getRootTableBinding()
										)
								),
								new ColumnBindingExpression(
										new ColumnBinding(
												fkTargetColumns.get( i ),
												drivingTableBinding
										)
								)
						)
				);
			}
			group.addTableSpecificationJoin( new TableJoin( joinType, drivingTableBinding, joinPredicate ) );
		}

		addNonRootTables( group, joinType, baseAdjust, drivingTableBinding );
	}

	@Override
	public Attribute findAttribute(String name) {
		if ( attributeMap.containsKey( name ) ) {
			return attributeMap.get( name );
		}

		// we know its not a "normal" attribute, otherwise the attributeMap lookup would have hit
		// so see if it could refer to the identifier
		if ( "id".equals( name ) || identifierDescriptor.getIdAttribute().getAttributeName().equals( name ) ) {
			return identifierDescriptor.getIdAttribute();
		}

		return null;
	}

	@Override
	public List<JoinColumnMapping> resolveJoinColumnMappings(Attribute attribute) {
		// todo : associations defined on entity
		throw new NotYetImplementedException();
	}

	public Map<String, AbstractAttribute> getAttributeMap() {
		return attributeMap;
	}

	@Override
	public String toString() {
		return "ImprovedEntityPersister(" + getEntityName() + ")";
	}

	@Override
	public org.hibernate.type.Type getOrmType() {
		return getEntityPersister().getEntityMetamodel().getEntityType();
	}

	@Override
	public String getEntityName() {
		return persister.getEntityName();
	}

	@Override
	public String asLoggableText() {
		return toString();
	}

	@Override
	public Optional<EntityReference> toEntityReference() {
		return Optional.of( this );
	}

	@Override
	public List<JoinColumnMapping> resolveJoinColumnMappings(JoinableAttribute joinableAttribute) {
		// can be only one of 3 types of attributes:
		//		1) SingularAttributeEntity
		if ( joinableAttribute instanceof SingularAttributeEntity ) {
			return extractEntityAttributeJoinColumnMappings( (SingularAttributeEntity) joinableAttribute );
		}

		//		2) SingularAttributeEmbedded
		if ( joinableAttribute instanceof SingularAttributeEmbedded ) {
			// embeddable joins are conceptual; they are not really rendered into the SQL
			return Collections.emptyList();
		}

		//		3) PluralAttribute
		if ( joinableAttribute instanceof PluralAttribute ) {
			// for now we only support joining back to our (as the collection owner) identifier columns (aka, no property-ref support)
			//		this same limitation exists in current ORM as well
			return ( (PluralAttribute) joinableAttribute ).getForeignKeyDescriptor().buildJoinColumnMappings(
					getIdentifierDescriptor().getColumns()
			);
		}

		throw new HibernateException( "Unrecognized JoinableAttribute type [" + joinableAttribute + "]" );
	}

	private List<JoinColumnMapping> extractEntityAttributeJoinColumnMappings(SingularAttributeEntity singularAttribute) {
		final List<Column> lhsJoinColumns;
		final List<Column> rhsJoinColumns;

		final ImprovedEntityPersister rhsPersister = singularAttribute.getEntityPersister();
		// because we have SingularAttributeEntity we know the classification will be limited to either
		//		MANY_TO_ONE or ONE_TO_ONE
		if ( singularAttribute.getAttributeTypeClassification() == SingularAttributeClassification.ONE_TO_ONE
				&& !singularAttribute.getOrmType().isLogicalOneToOne() ) {
			final OneToOneType oneToOneType = (OneToOneType) singularAttribute.getOrmType();
			if ( oneToOneType.getForeignKeyDirection() == ForeignKeyDirection.FROM_PARENT ) {
				// the join predicates should point *from* the owner (parent)
				if ( oneToOneType.getPropertyName() == null ) {
					lhsJoinColumns = getIdentifierDescriptor().getColumns();
				}
				else {
					final SingularAttribute referencedAttribute = (SingularAttribute) findAttribute( oneToOneType.getPropertyName() );
					lhsJoinColumns = referencedAttribute.getColumns();
				}
				rhsJoinColumns = singularAttribute.getColumns();
			}
			else {
				// the join predicates should point *to* the owner (parent)
				lhsJoinColumns = singularAttribute.getColumns();
				if ( oneToOneType.getPropertyName() == null ) {
					rhsJoinColumns = getIdentifierDescriptor().getColumns();
				}
				else {
					final SingularAttribute referencedAttribute = (SingularAttribute) findAttribute( oneToOneType.getPropertyName() );
					rhsJoinColumns = referencedAttribute.getColumns();
				}
			}
		}
		else {
			lhsJoinColumns = singularAttribute.getColumns();
			if ( singularAttribute.getOrmType().getRHSUniqueKeyPropertyName() == null ) {
				rhsJoinColumns = rhsPersister.getIdentifierDescriptor().getColumns();
			}
			else {
				final SingularAttribute rhsPropertyRefAttribute = (SingularAttribute) rhsPersister.findAttribute(
						singularAttribute.getOrmType().getRHSUniqueKeyPropertyName()
				);
				rhsJoinColumns = rhsPropertyRefAttribute.getColumns();
			}
		}

		if ( lhsJoinColumns.size() != rhsJoinColumns.size() ) {
			throw new HibernateException( "Bad resolution of right-hand and left-hand columns for attribute join : " + singularAttribute );
		}

		final List<JoinColumnMapping> joinColumnMappings = CollectionHelper.arrayList( lhsJoinColumns.size() );
		for ( int i = 0; i < lhsJoinColumns.size(); i++ ) {
			joinColumnMappings.add(
					new JoinColumnMapping(
							lhsJoinColumns.get( i ),
							rhsJoinColumns.get( i )
					)
			);
		}

		return joinColumnMappings;
	}

	@Override
	public boolean canCompositeContainCollections() {
		return true;
	}
}
