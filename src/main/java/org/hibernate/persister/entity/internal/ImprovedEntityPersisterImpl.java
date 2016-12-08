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

import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.persister.common.internal.DatabaseModel;
import org.hibernate.persister.common.internal.DomainMetamodelImpl;
import org.hibernate.persister.common.internal.Helper;
import org.hibernate.persister.common.spi.AbstractAttributeDescriptor;
import org.hibernate.persister.common.spi.AbstractTable;
import org.hibernate.persister.common.spi.AttributeDescriptor;
import org.hibernate.persister.common.spi.Column;
import org.hibernate.persister.common.spi.SingularAttributeDescriptor;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.persister.entity.OuterJoinLoadable;
import org.hibernate.persister.entity.Queryable;
import org.hibernate.persister.entity.UnionSubclassEntityPersister;
import org.hibernate.persister.entity.spi.IdentifierDescriptor;
import org.hibernate.persister.entity.spi.ImprovedEntityPersister;
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
import org.hibernate.sqm.domain.AttributeReference;
import org.hibernate.sqm.domain.EntityReference;
import org.hibernate.sqm.domain.PluralAttributeReference;
import org.hibernate.sqm.query.JoinType;
import org.hibernate.sqm.query.from.SqmFrom;
import org.hibernate.type.BasicType;
import org.hibernate.type.CompositeType;

import org.jboss.logging.Logger;

/**
 * @author Steve Ebersole
 */
public class ImprovedEntityPersisterImpl implements ImprovedEntityPersister {
	private static final Logger log = Logger.getLogger( ImprovedEntityPersisterImpl.class );

	private final EntityPersister persister;

	private AbstractTable[] tables;

	private ImprovedEntityPersisterImpl superType;
	private IdentifierDescriptor identifierDescriptor;

	private final Map<String, AbstractAttributeDescriptor> attributeMap = new HashMap<>();
	private final List<AbstractAttributeDescriptor> attributeList = new ArrayList<>();

	public ImprovedEntityPersisterImpl(EntityPersister persister) {
		this.persister = persister;
	}

	private boolean initComplete = false;

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
			tables = new AbstractTable[1];
			tables[0] =  makeTableReference( databaseModel, ((UnionSubclassEntityPersister) persister).getTableName() );
		}
		else {
			// for now we treat super, self and sub attributes here just as EntityPersister does
			// ultimately would be better to split that across the specific persister impls and link them imo
			final int subclassTableCount = Helper.INSTANCE.extractSubclassTableCount( persister );
			this.tables = new AbstractTable[subclassTableCount];

			tables[0] = makeTableReference( databaseModel, queryable.getSubclassTableName( 0 ) );
			for ( int i = 1; i < subclassTableCount; i++ ) {
				tables[i] = makeTableReference( databaseModel, queryable.getSubclassTableName( i ) );
			}
		}

		final Column[] idColumns = Helper.makeValues(
				domainMetamodel.getSessionFactory(),
				tables[0],
				persister.getIdentifierType(),
				ojlPersister.getIdentifierColumnNames(),
				null
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
						Helper.INSTANCE.buildEmbeddablePersister(
								databaseModel,
								domainMetamodel,
								persister.getEntityName() + '.' + persister.getIdentifierPropertyName(),
								cidType,
								idColumns
						)
				);
			}
			else {
				identifierDescriptor = new IdentifierCompositeNonAggregated(
						this,
						Helper.INSTANCE.buildEmbeddablePersister(
								databaseModel,
								domainMetamodel,
								persister.getEntityName() + ".id",
								cidType,
								idColumns
						)
				);
			}
		}

		final int fullAttributeCount = ( ojlPersister ).countSubclassProperties();
		for ( int attributeNumber = 0; attributeNumber < fullAttributeCount; attributeNumber++ ) {
			final String attributeName = ojlPersister.getSubclassPropertyName( attributeNumber );
			log.tracef( "Starting building of Entity attribute : %s#%s", persister.getEntityName(), attributeName );

			final org.hibernate.type.Type attributeType = ojlPersister.getSubclassPropertyType( attributeNumber );

			final AbstractTable containingTable = tables[ Helper.INSTANCE.getSubclassPropertyTableNumber( persister, attributeNumber ) ];
			final String [] columns = Helper.INSTANCE.getSubclassPropertyColumnExpressions( persister, attributeNumber );
			final String [] formulas = Helper.INSTANCE.getSubclassPropertyFormulaExpressions( persister, attributeNumber );
			final Column[] values = Helper.makeValues(
					domainMetamodel.getSessionFactory(),
					containingTable,
					attributeType,
					columns,
					formulas
			);

			final AbstractAttributeDescriptor attribute;
			if ( attributeType.isCollectionType() ) {
				attribute = Helper.INSTANCE.buildPluralAttribute(
						databaseModel,
						domainMetamodel,
						this,
						attributeName,
						attributeType,
						values
				);
			}
			else {
				attribute = Helper.INSTANCE.buildSingularAttribute(
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

	private AbstractTable makeTableReference(DatabaseModel databaseModel, String tableExpression) {
		// fugly, but when moved into persister we would know from mapping metamodel which type.
		if ( tableExpression.trim().startsWith( "select" ) || tableExpression.trim().contains( "( select" ) ) {
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
	public AbstractTable getRootTable() {
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
				Helper.convert( fromElement.getPropertyPath() )
		);

		fromClauseIndex.crossReference( fromElement, group );


		final TableBinding drivingTableBinding = new TableBinding( tables[0], group.getAliasBase() );
		group.setRootTableBinding( drivingTableBinding );

		// todo : determine proper join type
		JoinType joinType = JoinType.LEFT;
		addNonRootTables( group, joinType, 0, drivingTableBinding );

		return group;
	}

	private void addNonRootTables(AbstractTableGroup group, JoinType joinType, int baseAdjust, TableBinding entityRootTableBinding) {
		for ( int i = 1; i < tables.length; i++ ) {
			final TableBinding tableBinding = new TableBinding( tables[i], group.getAliasBase() + '_' + (i + (baseAdjust-1)) );
			group.addTableSpecificationJoin( new TableJoin( joinType, tableBinding, null ) );
		}
	}

	@Override
	public List<AttributeDescriptor> getNonIdentifierAttributes() {
		final List<AttributeDescriptor> rtn = new ArrayList<>();
		collectNonIdentifierAttributes( rtn );
		return rtn;
	}

	private void collectNonIdentifierAttributes(List<AttributeDescriptor> list) {
		if ( superType != null ) {
			superType.collectNonIdentifierAttributes( list );
		}

		list.addAll( attributeList );
	}

	public void addTableJoins(AbstractTableGroup group, JoinType joinType, Column[] fkColumns, Column[] fkTargetColumns) {
		final int baseAdjust;
		final TableBinding drivingTableBinding;

		if ( group.getRootTableBinding() == null ) {
			assert fkColumns == null && fkTargetColumns == null;

			baseAdjust = 0;
			drivingTableBinding = new TableBinding( tables[0], group.getAliasBase() );
			group.setRootTableBinding( drivingTableBinding );
		}
		else {
			assert fkColumns.length == fkTargetColumns.length;

			baseAdjust = 1;
			drivingTableBinding = new TableBinding( tables[0], group.getAliasBase() + '_' + 0 );

			final Junction joinPredicate = new Junction( Junction.Nature.CONJUNCTION );
			for ( int i=0; i < fkColumns.length; i++ ) {
				joinPredicate.add(
						new RelationalPredicate(
								RelationalPredicate.Operator.EQUAL,
								new ColumnBindingExpression(
										new ColumnBinding(
												fkColumns[i],
												fkColumns[i].getJdbcType(),
												group.getRootTableBinding()
										)
								),
								new ColumnBindingExpression(
										new ColumnBinding(
												fkTargetColumns[i],
												fkTargetColumns[i].getJdbcType(),
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
	public AttributeDescriptor findAttribute(String name) {
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

	public Map<String, AbstractAttributeDescriptor> getAttributeMap() {
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
	public int getColumnCount(boolean shallow, SessionFactoryImplementor factory) {
		int count = getIdentifierDescriptor().getColumns().length;
		if ( !shallow ) {
			for ( AttributeReference attributeReference : getNonIdentifierAttributes() ) {
				if ( attributeReference instanceof PluralAttributeReference ) {
					continue;
				}

				final SingularAttributeDescriptor attrRef = (SingularAttributeDescriptor) attributeReference;
				count += attrRef.getColumns().length;
			}
		}
		return count;
	}

	@Override
	public List<Column> getColumns(boolean shallow, SessionFactoryImplementor factory) {
		final List<Column> columns = new ArrayList<>();

		// add identifier columns regardless
		Collections.addAll( columns, getIdentifierDescriptor().getColumns() );

		if ( !shallow ) {
			for ( AttributeReference attributeReference : getNonIdentifierAttributes() ) {
				if ( attributeReference instanceof PluralAttributeReference ) {
					continue;
				}

				final SingularAttributeDescriptor attrRef = (SingularAttributeDescriptor) attributeReference;
				Collections.addAll( columns, attrRef.getColumns() );
			}
		}

		return columns;
	}
}
