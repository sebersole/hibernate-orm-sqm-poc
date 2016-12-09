/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.convert.spi;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.AssertionFailure;
import org.hibernate.engine.FetchStrategy;
import org.hibernate.engine.FetchStyle;
import org.hibernate.engine.FetchTiming;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.graph.spi.AttributeNodeImplementor;
import org.hibernate.graph.spi.EntityGraphImplementor;
import org.hibernate.persister.collection.spi.ImprovedCollectionPersister;
import org.hibernate.persister.common.internal.SingularAttributeEmbedded;
import org.hibernate.persister.common.internal.SingularAttributeEntity;
import org.hibernate.persister.common.spi.OrmTypeExporter;
import org.hibernate.persister.common.spi.SingularAttributeDescriptor;
import org.hibernate.persister.entity.spi.ImprovedEntityPersister;
import org.hibernate.query.proposed.QueryOptions;
import org.hibernate.sql.NotYetImplementedException;
import org.hibernate.sql.ast.QuerySpec;
import org.hibernate.sql.ast.SelectQuery;
import org.hibernate.sql.ast.expression.AvgFunction;
import org.hibernate.sql.ast.expression.BinaryArithmeticExpression;
import org.hibernate.sql.ast.expression.CaseSearchedExpression;
import org.hibernate.sql.ast.expression.CaseSimpleExpression;
import org.hibernate.sql.ast.expression.CoalesceExpression;
import org.hibernate.sql.ast.expression.ColumnBindingExpression;
import org.hibernate.sql.ast.expression.ConcatExpression;
import org.hibernate.sql.ast.expression.CountFunction;
import org.hibernate.sql.ast.expression.CountStarFunction;
import org.hibernate.sql.ast.expression.Expression;
import org.hibernate.sql.ast.expression.MaxFunction;
import org.hibernate.sql.ast.expression.MinFunction;
import org.hibernate.sql.ast.expression.NamedParameter;
import org.hibernate.sql.ast.expression.NonStandardFunctionExpression;
import org.hibernate.sql.ast.expression.NullifExpression;
import org.hibernate.sql.ast.expression.PositionalParameter;
import org.hibernate.sql.ast.expression.QueryLiteral;
import org.hibernate.sql.ast.expression.SumFunction;
import org.hibernate.sql.ast.expression.UnaryOperationExpression;
import org.hibernate.sql.ast.expression.domain.CompositeColumnBindingSource;
import org.hibernate.sql.ast.expression.domain.PluralAttributeElementReferenceExpression;
import org.hibernate.sql.ast.expression.domain.SingularAttributeReferenceExpression;
import org.hibernate.sql.ast.expression.instantiation.DynamicInstantiation;
import org.hibernate.sql.ast.from.CollectionTableGroup;
import org.hibernate.sql.ast.from.ColumnBinding;
import org.hibernate.sql.ast.from.EntityTableGroup;
import org.hibernate.sql.ast.from.TableGroup;
import org.hibernate.sql.ast.from.TableGroupJoin;
import org.hibernate.sql.ast.from.TableSpace;
import org.hibernate.sql.ast.predicate.BetweenPredicate;
import org.hibernate.sql.ast.predicate.GroupedPredicate;
import org.hibernate.sql.ast.predicate.InListPredicate;
import org.hibernate.sql.ast.predicate.InSubQueryPredicate;
import org.hibernate.sql.ast.predicate.Junction;
import org.hibernate.sql.ast.predicate.LikePredicate;
import org.hibernate.sql.ast.predicate.NegatedPredicate;
import org.hibernate.sql.ast.predicate.NullnessPredicate;
import org.hibernate.sql.ast.predicate.Predicate;
import org.hibernate.sql.ast.predicate.RelationalPredicate;
import org.hibernate.sql.ast.select.SelectClause;
import org.hibernate.sql.ast.select.Selection;
import org.hibernate.sql.convert.SyntaxException;
import org.hibernate.sql.convert.internal.FromClauseIndex;
import org.hibernate.sql.convert.internal.SqlAliasBaseManager;
import org.hibernate.sql.convert.results.internal.FetchCompositeAttributeImpl;
import org.hibernate.sql.convert.results.internal.FetchEntityAttributeImpl;
import org.hibernate.sql.convert.results.spi.FetchParent;
import org.hibernate.sql.convert.results.spi.Return;
import org.hibernate.sql.convert.results.spi.ReturnDynamicInstantiation;
import org.hibernate.sqm.BaseSemanticQueryWalker;
import org.hibernate.sqm.domain.DomainMetamodel;
import org.hibernate.sqm.domain.DomainReference;
import org.hibernate.sqm.domain.PluralAttributeReference;
import org.hibernate.sqm.domain.SingularAttributeReference.SingularAttributeClassification;
import org.hibernate.sqm.parser.SemanticException;
import org.hibernate.sqm.query.SqmDeleteStatement;
import org.hibernate.sqm.query.SqmInsertSelectStatement;
import org.hibernate.sqm.query.SqmQuerySpec;
import org.hibernate.sqm.query.SqmSelectStatement;
import org.hibernate.sqm.query.SqmUpdateStatement;
import org.hibernate.sqm.query.expression.BinaryArithmeticSqmExpression;
import org.hibernate.sqm.query.expression.CaseSearchedSqmExpression;
import org.hibernate.sqm.query.expression.CaseSimpleSqmExpression;
import org.hibernate.sqm.query.expression.CoalesceSqmExpression;
import org.hibernate.sqm.query.expression.ConcatSqmExpression;
import org.hibernate.sqm.query.expression.ConstantEnumSqmExpression;
import org.hibernate.sqm.query.expression.ConstantFieldSqmExpression;
import org.hibernate.sqm.query.expression.LiteralBigDecimalSqmExpression;
import org.hibernate.sqm.query.expression.LiteralBigIntegerSqmExpression;
import org.hibernate.sqm.query.expression.LiteralCharacterSqmExpression;
import org.hibernate.sqm.query.expression.LiteralDoubleSqmExpression;
import org.hibernate.sqm.query.expression.LiteralFalseSqmExpression;
import org.hibernate.sqm.query.expression.LiteralFloatSqmExpression;
import org.hibernate.sqm.query.expression.LiteralIntegerSqmExpression;
import org.hibernate.sqm.query.expression.LiteralLongSqmExpression;
import org.hibernate.sqm.query.expression.LiteralNullSqmExpression;
import org.hibernate.sqm.query.expression.LiteralStringSqmExpression;
import org.hibernate.sqm.query.expression.LiteralTrueSqmExpression;
import org.hibernate.sqm.query.expression.NamedParameterSqmExpression;
import org.hibernate.sqm.query.expression.NullifSqmExpression;
import org.hibernate.sqm.query.expression.PositionalParameterSqmExpression;
import org.hibernate.sqm.query.expression.SqmExpression;
import org.hibernate.sqm.query.expression.UnaryOperationSqmExpression;
import org.hibernate.sqm.query.expression.domain.AttributeBinding;
import org.hibernate.sqm.query.expression.domain.EntityBinding;
import org.hibernate.sqm.query.expression.domain.PluralAttributeBinding;
import org.hibernate.sqm.query.expression.domain.PluralAttributeElementBinding;
import org.hibernate.sqm.query.expression.domain.SingularAttributeBinding;
import org.hibernate.sqm.query.expression.function.AvgFunctionSqmExpression;
import org.hibernate.sqm.query.expression.function.CountFunctionSqmExpression;
import org.hibernate.sqm.query.expression.function.CountStarFunctionSqmExpression;
import org.hibernate.sqm.query.expression.function.MaxFunctionSqmExpression;
import org.hibernate.sqm.query.expression.function.MinFunctionSqmExpression;
import org.hibernate.sqm.query.expression.function.SumFunctionSqmExpression;
import org.hibernate.sqm.query.from.FromElementSpace;
import org.hibernate.sqm.query.from.SqmAttributeJoin;
import org.hibernate.sqm.query.from.SqmCrossJoin;
import org.hibernate.sqm.query.from.SqmEntityJoin;
import org.hibernate.sqm.query.from.SqmFromClause;
import org.hibernate.sqm.query.from.SqmJoin;
import org.hibernate.sqm.query.from.SqmRoot;
import org.hibernate.sqm.query.order.OrderByClause;
import org.hibernate.sqm.query.order.SortSpecification;
import org.hibernate.sqm.query.predicate.AndSqmPredicate;
import org.hibernate.sqm.query.predicate.BetweenSqmPredicate;
import org.hibernate.sqm.query.predicate.GroupedSqmPredicate;
import org.hibernate.sqm.query.predicate.InListSqmPredicate;
import org.hibernate.sqm.query.predicate.InSubQuerySqmPredicate;
import org.hibernate.sqm.query.predicate.LikeSqmPredicate;
import org.hibernate.sqm.query.predicate.NegatedSqmPredicate;
import org.hibernate.sqm.query.predicate.NullnessSqmPredicate;
import org.hibernate.sqm.query.predicate.OrSqmPredicate;
import org.hibernate.sqm.query.predicate.RelationalSqmPredicate;
import org.hibernate.sqm.query.predicate.SqmWhereClause;
import org.hibernate.sqm.query.select.SqmDynamicInstantiation;
import org.hibernate.sqm.query.select.SqmDynamicInstantiationArgument;
import org.hibernate.sqm.query.select.SqmDynamicInstantiationTarget;
import org.hibernate.sqm.query.select.SqmSelectClause;
import org.hibernate.sqm.query.select.SqmSelection;
import org.hibernate.type.BasicType;
import org.hibernate.type.Type;

import org.jboss.logging.Logger;

/**
 * Interprets an SqmSelectStatement as a SQL-AST SelectQuery.
 *
 * @author Steve Ebersole
 * @author John O'Hara
 */
public class SqmSelectToSqlAstConverter extends BaseSemanticQueryWalker {
	private static final Logger log = Logger.getLogger( SqmSelectToSqlAstConverter.class );

	/**
	 * Main entry point into SQM SelectStatement interpretation
	 *
	 * @param statement The SQM SelectStatement to interpret
	 * @param queryOptions The options to be applied to the interpretation
	 * @param callback to be formally defined
	 *
	 * @return The SQL AST
	 */
	public static SelectQuery interpret(
			SqmSelectStatement statement,
			SessionFactoryImplementor factory,
			DomainMetamodel sqmDomainMetamodel,
			QueryOptions queryOptions,
			Callback callback) {
		final SqmSelectToSqlAstConverter walker = new SqmSelectToSqlAstConverter(
				factory,
				sqmDomainMetamodel,
				queryOptions,
				callback
		);
		return walker.interpret( statement );
	}

	private final SessionFactoryImplementor factory;
	private final DomainMetamodel sqmDomainMetamodel;
	private final QueryOptions queryOptions;
	private final Callback callback;

	private final FromClauseIndex fromClauseIndex = new FromClauseIndex();
	private final SqlAliasBaseManager sqlAliasBaseManager = new SqlAliasBaseManager();

	private SelectQuery sqlAst;

	private SqmSelectToSqlAstConverter(
			SessionFactoryImplementor factory,
			DomainMetamodel sqmDomainMetamodel,
			QueryOptions queryOptions,
			Callback callback) {
		this.factory = factory;
		this.sqmDomainMetamodel = sqmDomainMetamodel;
		this.queryOptions = queryOptions;
		this.callback = callback;
	}

	private SelectQuery interpret(SqmSelectStatement statement) {
		visitSelectStatement( statement );
		// todo : return the new SqmSelectInterpretation composition
		return getSelectQuery();
	}

	private SelectQuery getSelectQuery() {
		return sqlAst;
	}


	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// walker


	@Override
	public Object visitUpdateStatement(SqmUpdateStatement statement) {
		throw new AssertionFailure( "Not expecting UpdateStatement" );
	}

	@Override
	public Object visitDeleteStatement(SqmDeleteStatement statement) {
		throw new AssertionFailure( "Not expecting DeleteStatement" );
	}

	@Override
	public Object visitInsertSelectStatement(SqmInsertSelectStatement statement) {
		throw new AssertionFailure( "Not expecting DeleteStatement" );
	}

	@Override
	public SelectQuery visitSelectStatement(SqmSelectStatement statement) {
		if ( sqlAst != null ) {
			throw new AssertionFailure( "SelectQuery already visited" );
		}

		sqlAst = new SelectQuery( visitQuerySpec( statement.getQuerySpec() ) );

		if ( statement.getOrderByClause() != null ) {
			for ( SortSpecification sortSpecification : statement.getOrderByClause().getSortSpecifications() ) {
				sqlAst.addSortSpecification( visitSortSpecification( sortSpecification ) );
			}
		}

		return sqlAst;
	}

	@Override
	public OrderByClause visitOrderByClause(OrderByClause orderByClause) {
		throw new AssertionFailure( "Unexpected visitor call" );
	}

	@Override
	public org.hibernate.sql.ast.sort.SortSpecification visitSortSpecification(SortSpecification sortSpecification) {
		return new org.hibernate.sql.ast.sort.SortSpecification(
				(Expression) sortSpecification.getSortExpression().accept( this ),
				sortSpecification.getCollation(),
				sortSpecification.getSortOrder()
		);
	}

	private Stack<QuerySpec> querySpecStack = new Stack<>();

	@Override
	public QuerySpec visitQuerySpec(SqmQuerySpec querySpec) {
		final QuerySpec astQuerySpec = new QuerySpec();
		querySpecStack.push( astQuerySpec );

		fromClauseIndex.pushFromClause( astQuerySpec.getFromClause() );

		try {
			// we want to visit the from-clause first
			visitFromClause( querySpec.getFromClause() );

			final SqmSelectClause selectClause = querySpec.getSelectClause();
			if ( selectClause != null ) {
				visitSelectClause( selectClause );
			}

			final SqmWhereClause whereClause = querySpec.getWhereClause();
			if ( whereClause != null ) {
				querySpecStack.getCurrent().setWhereClauseRestrictions(
						(Predicate) whereClause.getPredicate().accept( this )
				);
			}

			return astQuerySpec;
		}
		finally {
			assert querySpecStack.pop() == astQuerySpec;
			assert fromClauseIndex.popFromClause() == astQuerySpec.getFromClause();
		}
	}

	@Override
	public Void visitFromClause(SqmFromClause fromClause) {
		fromClause.getFromElementSpaces().forEach( this::visitFromElementSpace );

		return null;
	}

	private TableSpace tableSpace;

	@Override
	public TableSpace visitFromElementSpace(FromElementSpace fromElementSpace) {
		tableSpace = fromClauseIndex.currentFromClause().makeTableSpace();
		try {
			visitRootEntityFromElement( fromElementSpace.getRoot() );
			for ( SqmJoin sqmJoin : fromElementSpace.getJoins() ) {
				tableSpace.addJoinedTableGroup( (TableGroupJoin) sqmJoin.accept( this ) );
			}
			return tableSpace;
		}
		finally {
			tableSpace = null;
		}
	}

	@Override
	public Object visitRootEntityFromElement(SqmRoot sqmRoot) {
		if ( fromClauseIndex.isResolved( sqmRoot ) ) {
			final TableGroup resolvedTableGroup = fromClauseIndex.findResolvedTableGroup( sqmRoot );
			return resolvedTableGroup.resolveEntityReference();
		}

		final EntityBinding binding = sqmRoot.getDomainReferenceBinding();
		final ImprovedEntityPersister entityPersister = (ImprovedEntityPersister) binding.getBoundDomainReference();
		final EntityTableGroup group = entityPersister.buildTableGroup(
				sqmRoot,
				tableSpace,
				sqlAliasBaseManager,
				fromClauseIndex
		);
		tableSpace.setRootTableGroup( group );

		return null;
	}

	@Override
	public Object visitQualifiedAttributeJoinFromElement(SqmAttributeJoin joinedFromElement) {
		if ( fromClauseIndex.isResolved( joinedFromElement ) ) {
			final TableGroup resolvedTableGroup = fromClauseIndex.findResolvedTableGroup( joinedFromElement );
			return resolvedTableGroup.resolveEntityReference();
		}

		final Junction predicate = new Junction( Junction.Nature.CONJUNCTION );
		final TableGroup group;

		if ( joinedFromElement.getAttributeBinding().getAttribute() instanceof PluralAttributeReference ) {
			final ImprovedCollectionPersister improvedCollectionPersister = (ImprovedCollectionPersister) joinedFromElement.getAttributeBinding().getAttribute();
			group = improvedCollectionPersister.buildTableGroup(
					joinedFromElement,
					tableSpace,
					sqlAliasBaseManager,
					fromClauseIndex
			);

			final TableGroup lhsTableGroup = fromClauseIndex.findResolvedTableGroup( joinedFromElement.getAttributeBinding().getLhs() );
			// I *think* it is a valid assumption here that the underlying TableGroup for an attribute is ultimately an EntityTableGroup
			// todo : verify this ^^
			final ColumnBinding[] joinLhsColumns = ( (EntityTableGroup) lhsTableGroup ).resolveIdentifierColumnBindings();
			final ColumnBinding[] joinRhsColumns = ( (CollectionTableGroup) group ).resolveKeyColumnBindings();
			assert joinLhsColumns.length == joinRhsColumns.length;

			for ( int i = 0; i < joinLhsColumns.length; i++ ) {
				predicate.add(
						new RelationalPredicate(
								RelationalPredicate.Operator.EQUAL,
								new ColumnBindingExpression( joinLhsColumns[i] ),
								new ColumnBindingExpression( joinRhsColumns[i] )
						)
				);
			}
		}
		else {
			final SingularAttributeDescriptor singularAttribute = (SingularAttributeDescriptor) joinedFromElement.getAttributeBinding().getAttribute();
			if ( singularAttribute.getAttributeTypeClassification() == SingularAttributeClassification.BASIC ) {
				throw new SemanticException( "Cannot join to attribute of BASIC type" );
			}
			else if ( singularAttribute.getAttributeTypeClassification() == SingularAttributeClassification.ANY ) {
				throw new SemanticException( "Cannot join to attribute of BASIC type" );
			}
			else if ( singularAttribute.getAttributeTypeClassification() == SingularAttributeClassification.EMBEDDED ) {
				group = fromClauseIndex.findResolvedTableGroup( joinedFromElement.getAttributeBinding().getLhs() );
			}
			else {
				final ImprovedEntityPersister entityPersister = Helper.extractEntityPersister(
						joinedFromElement,
						factory,
						sqmDomainMetamodel
				);
				group = entityPersister.buildTableGroup(
						joinedFromElement,
						tableSpace,
						sqlAliasBaseManager,
						fromClauseIndex
				);

				final TableGroup lhsTableGroup = fromClauseIndex.findResolvedTableGroup( joinedFromElement.getAttributeBinding().getLhs() );
				final ColumnBinding[] joinLhsColumns = lhsTableGroup.resolveBindings( singularAttribute );
				final ColumnBinding[] joinRhsColumns;

				final org.hibernate.type.EntityType ormType = (org.hibernate.type.EntityType) singularAttribute.getOrmType();
				if ( ormType.getRHSUniqueKeyPropertyName() == null ) {
					joinRhsColumns = ( (EntityTableGroup) group ).resolveIdentifierColumnBindings();
				}
				else {
					final ImprovedEntityPersister associatedPersister = ( (EntityTableGroup) lhsTableGroup ).getPersister();
					joinRhsColumns = group.resolveBindings(
							(SingularAttributeDescriptor) associatedPersister.findAttribute( ormType.getRHSUniqueKeyPropertyName() )
					);
				}
				assert joinLhsColumns.length == joinRhsColumns.length;

				for ( int i = 0; i < joinLhsColumns.length; i++ ) {
					predicate.add(
							new RelationalPredicate(
									RelationalPredicate.Operator.EQUAL,
									new ColumnBindingExpression( joinLhsColumns[i] ),
									new ColumnBindingExpression( joinRhsColumns[i] )
							)
					);
				}
			}
		}

		// add any additional join restrictions
		if ( joinedFromElement.getOnClausePredicate() != null ) {
			predicate.add( (Predicate) joinedFromElement.getOnClausePredicate().accept( this ) );
		}

		return new TableGroupJoin( joinedFromElement.getJoinType(), group, predicate );
	}

	@Override
	public TableGroupJoin visitCrossJoinedFromElement(SqmCrossJoin joinedFromElement) {
		// todo : this cast will not ultimately work.
		// 		Instead we will need to resolve the Bindable+intrinsicSubclassIndicator to its ImprovedEntityPersister/EntityPersister
		final ImprovedEntityPersister entityPersister = (ImprovedEntityPersister) joinedFromElement.getIntrinsicSubclassIndicator();
		TableGroup group = entityPersister.buildTableGroup(
				joinedFromElement,
				tableSpace,
				sqlAliasBaseManager,
				fromClauseIndex
		);
		return new TableGroupJoin( joinedFromElement.getJoinType(), group, null );
	}

	@Override
	public Object visitQualifiedEntityJoinFromElement(SqmEntityJoin joinedFromElement) {
		throw new NotYetImplementedException();
	}

	@Override
	public SelectClause visitSelectClause(SqmSelectClause selectClause) {
		super.visitSelectClause( selectClause );
		currentQuerySpec().getSelectClause().makeDistinct( selectClause.isDistinct() );
		return currentQuerySpec().getSelectClause();
	}

	private QuerySpec currentQuerySpec() {
		return querySpecStack.getCurrent();
	}

	@Override
	public Selection visitSelection(SqmSelection sqmSelection) {
		final Expression expression = (Expression) sqmSelection.getExpression().accept( this );

		final Return queryReturn = expression.toQueryReturn( sqmSelection.getAlias() );
		applyFetchesAndEntityGraph( queryReturn );

		final Selection selection = new Selection( queryReturn );

		currentQuerySpec().getSelectClause().selection( selection );

		return selection;
	}

	private void applyFetchesAndEntityGraph(Return queryReturn) {
		if ( queryReturn instanceof FetchParent ) {
			applyFetchesAndEntityGraph( (FetchParent) queryReturn, extractEntityGraph() );
		}

		// todo : dynamic-instantiations *if* the dynamic-instantiation takes the entity as an argument
		if ( queryReturn instanceof ReturnDynamicInstantiation ) {

		}

		// otherwise, nothing to do
	}

	private EntityGraphImplementor extractEntityGraph() {
		if ( queryOptions.getEntityGraphQueryHint() == null ) {
			return null;
		}
		else {
			return (EntityGraphImplementor) queryOptions.getEntityGraphQueryHint().getOriginEntityGraph();
		}
	}

	private Set<String> alreadyProcessedFetchParentTableGroupUids = new HashSet<>();

	private void applyFetchesAndEntityGraph(FetchParent fetchParent, EntityGraphImplementor entityGraph) {
		final String uniqueIdentifier = fetchParent.getTableGroupUniqueIdentifier();
		if ( !alreadyProcessedFetchParentTableGroupUids.add( uniqueIdentifier ) ) {
			log.errorf( "Found duplicate tableGroupUid as FetchParent [%s]", uniqueIdentifier );
			return;
		}

		// todo : to do this well we are going to need a way to get all of the attributes related to this FetchParent
		//		that way we can drive this process across all of the attributes defined for the
		//		FetchParent type at once (one iteration)

		// todo : look to define a vistor-based walker
		//		I think this (^^) helps too with recognizing graph circularities.  Peek at how load-plans
		//		recognize such circularities.
		//
		//		Possibly add a AttributeNodeImplementor#applyFetches method (returning Subgraphs?)

		// todo : fetches coming from an EntityGraph most likely need a "from element" (TableGroup)

		final List<SqmAttributeJoin> fetchedJoins = fromClauseIndex.findFetchesByUniqueIdentifier( uniqueIdentifier );


		for ( SqmAttributeJoin fetchedJoin : fetchedJoins ) {
			final AttributeBinding fetchedAttributeBinding = fetchedJoin.getAttributeBinding();
			// todo  : need this method added to EntityGraphImplementor
			//final String attributeName = fetchedAttributeBinding.getAttribute().getAttributeName();
			//final AttributeNodeImplementor attributeNode = entityGraphImplementor.findAttributeNode( attributeName );
			final AttributeNodeImplementor attributeNode = null;
			applyFetchesAndEntityGraph( fetchParent, fetchedJoin, attributeNode );
		}
	}

	private void applyFetchesAndEntityGraph(FetchParent fetchParent, SqmAttributeJoin attributeJoin, AttributeNodeImplementor attributeNode) {
		if ( attributeJoin.getAttributeBinding() instanceof PluralAttributeBinding ) {
			// apply the plural attribute fetch join
			final PluralAttributeBinding pluralAttributeBinding = (PluralAttributeBinding) attributeJoin.getAttributeBinding();

			// todo : work out how to model collection fetches...
			//		mainly... do we need a "grouping" fetch?  And if so, should
			// 		CollectionAttributeFetch expose its element versus key fetches individually?  Or
			// 		does it represent each by itself?

		}
		else if ( attributeJoin.getAttributeBinding() instanceof SingularAttributeBinding ) {
			// apply the singular attribute fetch join
			final SingularAttributeBinding attributeBinding = (SingularAttributeBinding) attributeJoin.getAttributeBinding();
			final SingularAttributeDescriptor boundAttribute = (SingularAttributeDescriptor) attributeBinding.getAttribute();

			switch ( boundAttribute.getAttributeTypeClassification() ) {
				case ANY:
				case BASIC: {
					throw new SyntaxException( "Attributes of BASIC or ANY type cannot be joined" );
				}
				case EMBEDDED: {
					final FetchCompositeAttributeImpl fetch = new FetchCompositeAttributeImpl(
							fetchParent,
							(SingularAttributeEmbedded) boundAttribute,
							new FetchStrategy( FetchTiming.IMMEDIATE, FetchStyle.JOIN )
					);
					fetchParent.addFetch( fetch );
					applyFetchesAndEntityGraph( fetch, null );
					break;
				}
				case ONE_TO_ONE:
				case MANY_TO_ONE: {
					final SingularAttributeEntity boundAttributeAsEntity = (SingularAttributeEntity) boundAttribute;
					final FetchEntityAttributeImpl fetch = new FetchEntityAttributeImpl(
							fetchParent,
							org.hibernate.persister.common.internal.Helper.convert( attributeJoin.getPropertyPath() ),
							attributeJoin.getUniqueIdentifier(),
							boundAttributeAsEntity,
							boundAttributeAsEntity.getEntityPersister(),
							new FetchStrategy( FetchTiming.IMMEDIATE, FetchStyle.JOIN )
					);
					fetchParent.addFetch( fetch );
					applyFetchesAndEntityGraph( fetch, null );
				}
			}
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public DynamicInstantiation visitDynamicInstantiation(SqmDynamicInstantiation dynamicInstantiation) {
		final Class target = interpret( dynamicInstantiation.getInstantiationTarget() );
		DynamicInstantiation sqlTree = new DynamicInstantiation( target );

		for ( SqmDynamicInstantiationArgument dynamicInstantiationArgument : dynamicInstantiation.getArguments() ) {
			sqlTree.addArgument(
					dynamicInstantiationArgument.getAlias(),
					(Expression) dynamicInstantiationArgument.getExpression().accept( this )
			);
		}

		return sqlTree;
	}

	private Class interpret(SqmDynamicInstantiationTarget instantiationTarget) {
		if ( instantiationTarget.getNature() == SqmDynamicInstantiationTarget.Nature.LIST ) {
			return List.class;
		}
		if ( instantiationTarget.getNature() == SqmDynamicInstantiationTarget.Nature.MAP ) {
			return Map.class;
		}
		return instantiationTarget.getJavaType();
	}

	@Override
	public Object visitAttributeReferenceExpression(AttributeBinding attributeBinding) {
		if ( attributeBinding instanceof PluralAttributeBinding ) {
			return resolvePluralAttributeBinding( (PluralAttributeBinding) attributeBinding );
		}
		else {
			return resolveSingularAttributeBinding( (SingularAttributeBinding) attributeBinding );
		}
	}

	private Object resolvePluralAttributeBinding(PluralAttributeBinding attributeBinding) {
		CollectionTableGroup tableGroup = null;
		if ( attributeBinding.getFromElement() != null ) {
			tableGroup = (CollectionTableGroup) fromClauseIndex.findResolvedTableGroup( attributeBinding.getFromElement() );
		}

		if ( tableGroup == null ) {
			throw new IllegalStateException( "From-element (join) for plural-attribute is not resolved" );
		}


		throw new NotYetImplementedException( "resolving AttributeBinding for plural-attributes" );
	}

	private SingularAttributeReferenceExpression resolveSingularAttributeBinding(SingularAttributeBinding attributeBinding) {
		final SingularAttributeDescriptor attribute = (SingularAttributeDescriptor) attributeBinding.getAttribute();
		final TableGroup tableGroup = fromClauseIndex.findResolvedTableGroup( attributeBinding.getLhs() );

		if ( attributeBinding.getFromElement() == null ) {
			return new SingularAttributeReferenceExpression(
					tableGroup,
					attribute,
					org.hibernate.persister.common.internal.Helper.convert( attributeBinding.getPropertyPath() )
			);
		}
		else {
			return new SingularAttributeReferenceExpression(
					new CompositeColumnBindingSource(
							tableGroup,
							fromClauseIndex.findResolvedTableGroup( attributeBinding.getFromElement() )
					),
					attribute,
					org.hibernate.persister.common.internal.Helper.convert( attributeBinding.getPropertyPath() )
			);
		}
	}

	@Override
	public QueryLiteral visitLiteralStringExpression(LiteralStringSqmExpression expression) {
		return new QueryLiteral(
				expression.getLiteralValue(),
				extractOrmType( expression.getExpressionType(), String.class )
		);
	}

	protected Type extractOrmType(DomainReference sqmType) {
		if ( sqmType == null ) {
			return null;
		}

		return ( (OrmTypeExporter) sqmType ).getOrmType();
	}

	protected Type extractOrmType(DomainReference expressionType, Class javaType) {
		Type type = extractOrmType( expressionType );
		if ( type == null ) {
			type = factory.getTypeHelper().basic( javaType );
		}
		return type;
	}

	@Override
	public QueryLiteral visitLiteralCharacterExpression(LiteralCharacterSqmExpression expression) {
		return new QueryLiteral(
				expression.getLiteralValue(),
				extractOrmType( expression.getExpressionType(), Character.class )
		);
	}

	@Override
	public QueryLiteral visitLiteralDoubleExpression(LiteralDoubleSqmExpression expression) {
		return new QueryLiteral(
				expression.getLiteralValue(),
				extractOrmType( expression.getExpressionType(), Double.class )
		);
	}

	@Override
	public QueryLiteral visitLiteralIntegerExpression(LiteralIntegerSqmExpression expression) {
		return new QueryLiteral(
				expression.getLiteralValue(),
				extractOrmType( expression.getExpressionType(), Integer.class )
		);
	}

	@Override
	public QueryLiteral visitLiteralBigIntegerExpression(LiteralBigIntegerSqmExpression expression) {
		return new QueryLiteral(
				expression.getLiteralValue(),
				extractOrmType( expression.getExpressionType(), BigInteger.class )
		);
	}

	@Override
	public QueryLiteral visitLiteralBigDecimalExpression(LiteralBigDecimalSqmExpression expression) {
		return new QueryLiteral(
				expression.getLiteralValue(),
				extractOrmType( expression.getExpressionType(), BigDecimal.class )
		);
	}

	@Override
	public QueryLiteral visitLiteralFloatExpression(LiteralFloatSqmExpression expression) {
		return new QueryLiteral(
				expression.getLiteralValue(),
				extractOrmType( expression.getExpressionType(), Float.class )
		);
	}

	@Override
	public QueryLiteral visitLiteralLongExpression(LiteralLongSqmExpression expression) {
		return new QueryLiteral(
				expression.getLiteralValue(),
				extractOrmType( expression.getExpressionType(), Long.class )
		);
	}

	@Override
	public QueryLiteral visitLiteralTrueExpression(LiteralTrueSqmExpression expression) {
		return new QueryLiteral(
				Boolean.TRUE,
				extractOrmType( expression.getExpressionType(), Boolean.class )
		);
	}

	@Override
	public QueryLiteral visitLiteralFalseExpression(LiteralFalseSqmExpression expression) {
		return new QueryLiteral(
				Boolean.FALSE,
				extractOrmType( expression.getExpressionType(), Boolean.class )
		);
	}

	@Override
	public QueryLiteral visitLiteralNullExpression(LiteralNullSqmExpression expression) {
		return new QueryLiteral(
				null,
				extractOrmType( expression.getExpressionType() )
		);
	}

	@Override
	public Object visitConstantEnumExpression(ConstantEnumSqmExpression expression) {
		return new QueryLiteral(
				expression.getValue(),
				extractOrmType( expression.getExpressionType(), expression.getValue().getClass() )
		);
	}

	@Override
	public Object visitConstantFieldExpression(ConstantFieldSqmExpression expression) {
		return new QueryLiteral(
				expression.getValue(),
				extractOrmType( expression.getExpressionType(), expression.getValue().getClass() )
		);
	}

	@Override
	public NamedParameter visitNamedParameterExpression(NamedParameterSqmExpression expression) {
		return new NamedParameter(
				expression.getName(),
				extractOrmType( expression.getExpressionType() )
		);
	}

	@Override
	public PositionalParameter visitPositionalParameterExpression(PositionalParameterSqmExpression expression) {
		return new PositionalParameter(
				expression.getPosition(),
				extractOrmType( expression.getExpressionType() )
		);
	}

	@Override
	public AvgFunction visitAvgFunction(AvgFunctionSqmExpression expression) {
		return new AvgFunction(
				(Expression) expression.getArgument().accept( this ),
				expression.isDistinct(),
				(BasicType) extractOrmType( expression.getExpressionType() )
		);
	}

	@Override
	public MaxFunction visitMaxFunction(MaxFunctionSqmExpression expression) {
		return new MaxFunction(
				(Expression) expression.getArgument().accept( this ),
				expression.isDistinct(),
				(BasicType) extractOrmType( expression.getExpressionType() )
		);
	}

	@Override
	public MinFunction visitMinFunction(MinFunctionSqmExpression expression) {
		return new MinFunction(
				(Expression) expression.getArgument().accept( this ),
				expression.isDistinct(),
				(BasicType) extractOrmType( expression.getExpressionType() )
		);
	}

	@Override
	public SumFunction visitSumFunction(SumFunctionSqmExpression expression) {
		return new SumFunction(
				(Expression) expression.getArgument().accept( this ),
				expression.isDistinct(),
				(BasicType) extractOrmType( expression.getExpressionType() )
		);
	}

	@Override
	public CountFunction visitCountFunction(CountFunctionSqmExpression expression) {
		return new CountFunction(
				(Expression) expression.getArgument().accept( this ),
				expression.isDistinct(),
				(BasicType) extractOrmType( expression.getExpressionType() )
		);
	}

	@Override
	public CountStarFunction visitCountStarFunction(CountStarFunctionSqmExpression expression) {
		return new CountStarFunction(
				expression.isDistinct(),
				(BasicType) extractOrmType( expression.getExpressionType() )
		);
	}

	@Override
	public Object visitUnaryOperationExpression(UnaryOperationSqmExpression expression) {
		return new UnaryOperationExpression(
				interpret( expression.getOperation() ),
				(Expression) expression.getOperand().accept( this ),
				(BasicType) extractOrmType( expression.getExpressionType() )
		);
	}

	private UnaryOperationExpression.Operation interpret(UnaryOperationSqmExpression.Operation operation) {
		switch ( operation ) {
			case PLUS: {
				return UnaryOperationExpression.Operation.PLUS;
			}
			case MINUS: {
				return UnaryOperationExpression.Operation.MINUS;
			}
		}

		throw new IllegalStateException( "Unexpected UnaryOperationExpression Operation : " + operation );
	}

	@Override
	public Expression visitBinaryArithmeticExpression(BinaryArithmeticSqmExpression expression) {
		if ( expression.getOperation() == BinaryArithmeticSqmExpression.Operation.MODULO ) {
			return new NonStandardFunctionExpression(
					"mod",
					null, //(BasicType) extractOrmType( expression.getExpressionType() ),
					(Expression) expression.getLeftHandOperand().accept( this ),
					(Expression) expression.getRightHandOperand().accept( this )
			);
		}
		return new BinaryArithmeticExpression(
				interpret( expression.getOperation() ),
				(Expression) expression.getLeftHandOperand().accept( this ),
				(Expression) expression.getRightHandOperand().accept( this ),
				null //(BasicType) extractOrmType( expression.getExpressionType() )
		);
	}

	private BinaryArithmeticExpression.Operation interpret(BinaryArithmeticSqmExpression.Operation operation) {
		switch ( operation ) {
			case ADD: {
				return BinaryArithmeticExpression.Operation.ADD;
			}
			case SUBTRACT: {
				return BinaryArithmeticExpression.Operation.SUBTRACT;
			}
			case MULTIPLY: {
				return BinaryArithmeticExpression.Operation.MULTIPLY;
			}
			case DIVIDE: {
				return BinaryArithmeticExpression.Operation.DIVIDE;
			}
			case QUOT: {
				return BinaryArithmeticExpression.Operation.QUOT;
			}
		}

		throw new IllegalStateException( "Unexpected BinaryArithmeticExpression Operation : " + operation );
	}

	@Override
	public CoalesceExpression visitCoalesceExpression(CoalesceSqmExpression expression) {
		final CoalesceExpression result = new CoalesceExpression();
		for ( SqmExpression value : expression.getValues() ) {
			result.value( (Expression) value.accept( this ) );
		}

		return result;
	}

	@Override
	public CaseSimpleExpression visitSimpleCaseExpression(CaseSimpleSqmExpression expression) {
		final CaseSimpleExpression result = new CaseSimpleExpression(
				extractOrmType( expression.getExpressionType() ),
				(Expression) expression.getFixture().accept( this )
		);

		for ( CaseSimpleSqmExpression.WhenFragment whenFragment : expression.getWhenFragments() ) {
			result.when(
					(Expression) whenFragment.getCheckValue().accept( this ),
					(Expression) whenFragment.getResult().accept( this )
			);
		}

		result.otherwise( (Expression) expression.getOtherwise().accept( this ) );

		return result;
	}

	@Override
	public CaseSearchedExpression visitSearchedCaseExpression(CaseSearchedSqmExpression expression) {
		final CaseSearchedExpression result = new CaseSearchedExpression( extractOrmType( expression.getExpressionType() ) );

		for ( CaseSearchedSqmExpression.WhenFragment whenFragment : expression.getWhenFragments() ) {
			result.when(
					(Predicate) whenFragment.getPredicate().accept( this ),
					(Expression) whenFragment.getResult().accept( this )
			);
		}

		result.otherwise( (Expression) expression.getOtherwise().accept( this ) );

		return result;
	}

	@Override
	public NullifExpression visitNullifExpression(NullifSqmExpression expression) {
		return new NullifExpression(
				(Expression) expression.getFirstArgument().accept( this ),
				(Expression) expression.getSecondArgument().accept( this )
		);
	}

	@Override
	public ConcatExpression visitConcatExpression(ConcatSqmExpression expression) {
		return new ConcatExpression(
				(Expression) expression.getLeftHandOperand().accept( this ),
				(Expression) expression.getLeftHandOperand().accept( this ),
				null //(BasicType) extractOrmType( expression.getExpressionType() )
		);
	}

	@Override
	public Object visitPluralAttributeElementBinding(PluralAttributeElementBinding binding) {
		final TableGroup resolvedTableGroup = fromClauseIndex.findResolvedTableGroup( binding.getFromElement() );

		final ImprovedCollectionPersister collectionPersister = (ImprovedCollectionPersister) binding.getPluralAttributeReference();
		return new PluralAttributeElementReferenceExpression(
				collectionPersister,
				resolvedTableGroup,
				org.hibernate.persister.common.internal.Helper.convert( binding.getPropertyPath() )
		);
	}



	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Predicates


	@Override
	public GroupedPredicate visitGroupedPredicate(GroupedSqmPredicate predicate) {
		return new GroupedPredicate ( (Predicate ) predicate.getSubPredicate().accept( this ) );
	}

	@Override
	public Junction visitAndPredicate(AndSqmPredicate predicate) {
		final Junction conjunction = new Junction( Junction.Nature.CONJUNCTION );
		conjunction.add( (Predicate) predicate.getLeftHandPredicate().accept( this ) );
		conjunction.add( (Predicate) predicate.getRightHandPredicate().accept( this ) );
		return conjunction;
	}

	@Override
	public Junction visitOrPredicate(OrSqmPredicate predicate) {
		final Junction disjunction = new Junction( Junction.Nature.DISJUNCTION );
		disjunction.add( (Predicate) predicate.getLeftHandPredicate().accept( this ) );
		disjunction.add( (Predicate) predicate.getRightHandPredicate().accept( this ) );
		return disjunction;
	}

	@Override
	public NegatedPredicate visitNegatedPredicate(NegatedSqmPredicate predicate) {
		return new NegatedPredicate(
				(Predicate) predicate.getWrappedPredicate().accept( this )
		);
	}

	@Override
	public RelationalPredicate visitRelationalPredicate(RelationalSqmPredicate predicate) {
		return new RelationalPredicate(
				interpret( predicate.getOperator() ),
				(Expression) predicate.getLeftHandExpression().accept( this ),
				(Expression) predicate.getRightHandExpression().accept( this )
		);
	}

	private RelationalPredicate.Operator interpret(RelationalSqmPredicate.Operator operator) {
		switch ( operator ) {
			case EQUAL: {
				return RelationalPredicate.Operator.EQUAL;
			}
			case NOT_EQUAL: {
				return RelationalPredicate.Operator.NOT_EQUAL;
			}
			case GREATER_THAN_OR_EQUAL: {
				return RelationalPredicate.Operator.GE;
			}
			case GREATER_THAN: {
				return RelationalPredicate.Operator.GT;
			}
			case LESS_THAN_OR_EQUAL: {
				return RelationalPredicate.Operator.LE;
			}
			case LESS_THAN: {
				return RelationalPredicate.Operator.LT;
			}
		}

		throw new IllegalStateException( "Unexpected RelationalPredicate Type : " + operator );
	}

	@Override
	public BetweenPredicate visitBetweenPredicate(BetweenSqmPredicate predicate) {
		return new BetweenPredicate(
				(Expression) predicate.getExpression().accept( this ),
				(Expression) predicate.getLowerBound().accept( this ),
				(Expression) predicate.getUpperBound().accept( this ),
				predicate.isNegated()
		);
	}

	@Override
	public LikePredicate visitLikePredicate(LikeSqmPredicate predicate) {
		final Expression escapeExpression = predicate.getEscapeCharacter() == null
				? null
				: (Expression) predicate.getEscapeCharacter().accept( this );

		return new LikePredicate(
				(Expression) predicate.getMatchExpression().accept( this ),
				(Expression) predicate.getPattern().accept( this ),
				escapeExpression,
				predicate.isNegated()
		);
	}

	@Override
	public NullnessPredicate visitIsNullPredicate(NullnessSqmPredicate predicate) {
		return new NullnessPredicate(
				(Expression) predicate.getExpression().accept( this ),
				predicate.isNegated()
		);
	}

	@Override
	public InListPredicate visitInListPredicate(InListSqmPredicate predicate) {
		final InListPredicate inPredicate = new InListPredicate(
				(Expression) predicate.getTestExpression().accept( this ),
				predicate.isNegated()
		);
		for ( SqmExpression expression : predicate.getListExpressions() ) {
			inPredicate.addExpression( (Expression) expression.accept( this ) );
		}
		return inPredicate;
	}

	@Override
	public InSubQueryPredicate visitInSubQueryPredicate(InSubQuerySqmPredicate predicate) {
		return new InSubQueryPredicate(
				(Expression) predicate.getTestExpression().accept( this ),
				(org.hibernate.sql.ast.QuerySpec) predicate.getSubQueryExpression().accept( this ),
				predicate.isNegated()
		);
	}
}
