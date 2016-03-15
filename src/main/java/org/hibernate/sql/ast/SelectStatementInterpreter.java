/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.ast;

import java.util.Stack;

import org.hibernate.AssertionFailure;
import org.hibernate.sql.ast.expression.AttributeReference;
import org.hibernate.sql.ast.expression.ColumnBindingExpression;
import org.hibernate.sql.ast.expression.NamedParameter;
import org.hibernate.sql.ast.expression.NonStandardFunctionExpression;
import org.hibernate.sql.ast.expression.PositionalParameter;
import org.hibernate.sql.ast.expression.QueryLiteral;
import org.hibernate.sql.ast.from.CollectionTableGroup;
import org.hibernate.sql.ast.from.ColumnBinding;
import org.hibernate.sql.ast.from.EntityTableGroup;
import org.hibernate.sql.ast.from.TableGroup;
import org.hibernate.sql.ast.from.TableGroupJoin;
import org.hibernate.sql.ast.from.TableSpace;
import org.hibernate.sql.ast.predicate.InListPredicate;
import org.hibernate.sql.ast.predicate.Junction;
import org.hibernate.sql.ast.predicate.Predicate;
import org.hibernate.sql.ast.predicate.RelationalPredicate;
import org.hibernate.sql.exec.spi.QueryOptions;
import org.hibernate.sql.gen.Callback;
import org.hibernate.sql.gen.NotYetImplementedException;
import org.hibernate.sql.gen.internal.FromClauseIndex;
import org.hibernate.sql.gen.internal.SqlAliasBaseManager;
import org.hibernate.persister.collection.spi.ImprovedCollectionPersister;
import org.hibernate.persister.entity.spi.ImprovedEntityPersister;
import org.hibernate.persister.common.spi.SingularAttributeImplementor;
import org.hibernate.persister.common.spi.SqmTypeImplementor;
import org.hibernate.sqm.BaseSemanticQueryWalker;
import org.hibernate.sqm.domain.PluralAttribute;
import org.hibernate.sqm.domain.SingularAttribute;
import org.hibernate.sqm.query.DeleteStatement;
import org.hibernate.sqm.query.InsertSelectStatement;
import org.hibernate.sqm.query.QuerySpec;
import org.hibernate.sqm.query.SelectStatement;
import org.hibernate.sqm.query.UpdateStatement;
import org.hibernate.sqm.query.expression.AttributeReferenceExpression;
import org.hibernate.sqm.query.expression.AvgFunction;
import org.hibernate.sqm.query.expression.BinaryArithmeticExpression;
import org.hibernate.sqm.query.expression.CaseSearchedExpression;
import org.hibernate.sqm.query.expression.CaseSimpleExpression;
import org.hibernate.sqm.query.expression.CoalesceExpression;
import org.hibernate.sqm.query.expression.ConcatExpression;
import org.hibernate.sqm.query.expression.ConstantEnumExpression;
import org.hibernate.sqm.query.expression.ConstantFieldExpression;
import org.hibernate.sqm.query.expression.CountFunction;
import org.hibernate.sqm.query.expression.CountStarFunction;
import org.hibernate.sqm.query.expression.Expression;
import org.hibernate.sqm.query.expression.LiteralBigDecimalExpression;
import org.hibernate.sqm.query.expression.LiteralBigIntegerExpression;
import org.hibernate.sqm.query.expression.LiteralCharacterExpression;
import org.hibernate.sqm.query.expression.LiteralDoubleExpression;
import org.hibernate.sqm.query.expression.LiteralFalseExpression;
import org.hibernate.sqm.query.expression.LiteralFloatExpression;
import org.hibernate.sqm.query.expression.LiteralIntegerExpression;
import org.hibernate.sqm.query.expression.LiteralLongExpression;
import org.hibernate.sqm.query.expression.LiteralNullExpression;
import org.hibernate.sqm.query.expression.LiteralStringExpression;
import org.hibernate.sqm.query.expression.LiteralTrueExpression;
import org.hibernate.sqm.query.expression.MaxFunction;
import org.hibernate.sqm.query.expression.MinFunction;
import org.hibernate.sqm.query.expression.NamedParameterExpression;
import org.hibernate.sqm.query.expression.NullifExpression;
import org.hibernate.sqm.query.expression.PositionalParameterExpression;
import org.hibernate.sqm.query.expression.SumFunction;
import org.hibernate.sqm.query.expression.UnaryOperationExpression;
import org.hibernate.sqm.query.from.CrossJoinedFromElement;
import org.hibernate.sqm.query.from.FromClause;
import org.hibernate.sqm.query.from.FromElementSpace;
import org.hibernate.sqm.query.from.JoinedFromElement;
import org.hibernate.sqm.query.from.QualifiedAttributeJoinFromElement;
import org.hibernate.sqm.query.from.QualifiedEntityJoinFromElement;
import org.hibernate.sqm.query.from.RootEntityFromElement;
import org.hibernate.sqm.query.order.OrderByClause;
import org.hibernate.sqm.query.order.SortSpecification;
import org.hibernate.sqm.query.predicate.AndPredicate;
import org.hibernate.sqm.query.predicate.BetweenPredicate;
import org.hibernate.sqm.query.predicate.GroupedPredicate;
import org.hibernate.sqm.query.predicate.InSubQueryPredicate;
import org.hibernate.sqm.query.predicate.LikePredicate;
import org.hibernate.sqm.query.predicate.NegatedPredicate;
import org.hibernate.sqm.query.predicate.NullnessPredicate;
import org.hibernate.sqm.query.predicate.OrPredicate;
import org.hibernate.sqm.query.predicate.WhereClause;
import org.hibernate.sqm.query.select.SelectClause;
import org.hibernate.sqm.query.select.Selection;
import org.hibernate.type.BasicType;
import org.hibernate.type.Type;

/**
 * @author Steve Ebersole
 * @author John O'Hara
 */
public class SelectStatementInterpreter extends BaseSemanticQueryWalker {

	/**
	 * Main entry point into SQM SelectStatement interpretation
	 *
	 * @param statement The SQM SelectStatement to interpret
	 * @param queryOptions The options to be applied to the interpretation
	 * @param callback to be formally defined
	 *
	 * @return The SQL AST
	 */
	public static SelectQuery interpret(SelectStatement statement, QueryOptions queryOptions, Callback callback) {
		final SelectStatementInterpreter walker = new SelectStatementInterpreter( queryOptions, callback );
		return walker.interpret( statement );
	}

	private final QueryOptions queryOptions;
	private final Callback callback;

	private final FromClauseIndex fromClauseIndex = new FromClauseIndex();
	private final SqlAliasBaseManager sqlAliasBaseManager = new SqlAliasBaseManager();

	private SelectQuery sqlAst;

	public SelectStatementInterpreter(QueryOptions queryOptions, Callback callback) {
		this.queryOptions = queryOptions;
		this.callback = callback;
	}

	public SelectQuery interpret(SelectStatement statement) {
		visitSelectStatement( statement );
		return getSelectQuery();
	}

	public SelectQuery getSelectQuery() {
		return sqlAst;
	}


	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// walker


	@Override
	public Object visitUpdateStatement(UpdateStatement statement) {
		throw new AssertionFailure( "Not expecting UpdateStatement" );
	}

	@Override
	public Object visitDeleteStatement(DeleteStatement statement) {
		throw new AssertionFailure( "Not expecting DeleteStatement" );
	}

	@Override
	public Object visitInsertSelectStatement(InsertSelectStatement statement) {
		throw new AssertionFailure( "Not expecting DeleteStatement" );
	}

	@Override
	public SelectQuery visitSelectStatement(SelectStatement statement) {
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
				(org.hibernate.sql.ast.expression.Expression) sortSpecification.getSortExpression().accept( this ),
				sortSpecification.getCollation(),
				sortSpecification.getSortOrder()
		);
	}

	private Stack<org.hibernate.sql.ast.QuerySpec> querySpecStack = new Stack<org.hibernate.sql.ast.QuerySpec>();

	@Override
	public org.hibernate.sql.ast.QuerySpec visitQuerySpec(QuerySpec querySpec) {
		final org.hibernate.sql.ast.QuerySpec astQuerySpec = new org.hibernate.sql.ast.QuerySpec();
		querySpecStack.push( astQuerySpec );

		fromClauseIndex.pushFromClause( astQuerySpec.getFromClause() );

		try {
			// we want to visit the from-clause first
			visitFromClause( querySpec.getFromClause() );

			final SelectClause selectClause = querySpec.getSelectClause();
			if ( selectClause != null ) {
				visitSelectClause( selectClause );
			}

			final WhereClause whereClause = querySpec.getWhereClause();
			if ( whereClause != null ) {
				querySpecStack.peek().setWhereClauseRestrictions(
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
	public Void visitFromClause(FromClause fromClause) {
		for ( FromElementSpace fromElementSpace : fromClause.getFromElementSpaces() ) {
			visitFromElementSpace( fromElementSpace );
		}

		return null;
	}

	private TableSpace tableSpace;

	@Override
	public TableSpace visitFromElementSpace(FromElementSpace fromElementSpace) {
		tableSpace = fromClauseIndex.currentFromClause().makeTableSpace();
		try {
			visitRootEntityFromElement( fromElementSpace.getRoot() );
			for ( JoinedFromElement joinedFromElement : fromElementSpace.getJoins() ) {
				tableSpace.addJoinedTableGroup( (TableGroupJoin) joinedFromElement.accept(
						this ) );
			}
			return tableSpace;
		}
		finally {
			tableSpace = null;
		}
	}

	@Override
	public Object visitRootEntityFromElement(RootEntityFromElement rootEntityFromElement) {
		if ( fromClauseIndex.isResolved( rootEntityFromElement ) ) {
			final TableGroup resolvedTableGroup = fromClauseIndex.findResolvedTableGroup( rootEntityFromElement );
			return resolvedTableGroup.resolveEntityReference();
		}

		final ImprovedEntityPersister entityPersister = (ImprovedEntityPersister) rootEntityFromElement.getBoundModelType();
		final EntityTableGroup group = entityPersister.buildTableGroup(
				rootEntityFromElement,
				tableSpace,
				sqlAliasBaseManager,
				fromClauseIndex
		);
		tableSpace.setRootTableGroup( group );

		return null;
	}

	@Override
	public Object visitQualifiedAttributeJoinFromElement(QualifiedAttributeJoinFromElement joinedFromElement) {
		if ( fromClauseIndex.isResolved( joinedFromElement ) ) {
			final TableGroup resolvedTableGroup = fromClauseIndex.findResolvedTableGroup( joinedFromElement );
			return resolvedTableGroup.resolveEntityReference();
		}

		final Junction predicate = new Junction( Junction.Nature.CONJUNCTION );
		final TableGroup group;

		if ( joinedFromElement.getBoundAttribute() instanceof PluralAttribute ) {
			final ImprovedCollectionPersister improvedCollectionPersister = (ImprovedCollectionPersister) joinedFromElement.getBoundAttribute();
			group = improvedCollectionPersister.buildTableGroup(
					joinedFromElement,
					tableSpace,
					sqlAliasBaseManager,
					fromClauseIndex
			);

			final TableGroup lhsTableGroup = fromClauseIndex.findResolvedTableGroup( joinedFromElement.getAttributeBindingSource() );
			// I *think* it is a valid assumption here that the underlying TableGroup for an attribute is ultimately an EntityTableGroup
			// todo : verify this
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
			final SingularAttributeImplementor singularAttribute = (SingularAttributeImplementor) joinedFromElement.getBoundAttribute();
			if ( singularAttribute.getAttributeTypeClassification() == SingularAttribute.Classification.EMBEDDED ) {
				group = fromClauseIndex.findResolvedTableGroup( joinedFromElement.getAttributeBindingSource() );
			}
			else {
				final ImprovedEntityPersister entityPersister = (ImprovedEntityPersister) joinedFromElement.getIntrinsicSubclassIndicator();
				group = entityPersister.buildTableGroup(
						joinedFromElement,
						tableSpace,
						sqlAliasBaseManager,
						fromClauseIndex
				);

				final TableGroup lhsTableGroup = fromClauseIndex.findResolvedTableGroup( joinedFromElement.getAttributeBindingSource() );
				final ColumnBinding[] joinLhsColumns = lhsTableGroup.resolveBindings( singularAttribute );
				final ColumnBinding[] joinRhsColumns;

				final org.hibernate.type.EntityType ormType = (org.hibernate.type.EntityType) singularAttribute.getOrmType();
				if ( ormType.getRHSUniqueKeyPropertyName() == null ) {
					joinRhsColumns = ( (EntityTableGroup) group ).resolveIdentifierColumnBindings();
				}
				else {
					final ImprovedEntityPersister associatedPersister = ( (EntityTableGroup) lhsTableGroup ).getPersister();
					joinRhsColumns = group.resolveBindings(
							(SingularAttribute) associatedPersister.findAttribute( ormType.getRHSUniqueKeyPropertyName() )
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

		fromClauseIndex.crossReference( joinedFromElement, group );

		// add any additional join restrictions
		if ( joinedFromElement.getOnClausePredicate() != null ) {
			predicate.add( (Predicate) joinedFromElement.getOnClausePredicate().accept( this ) );
		}

		return new TableGroupJoin( joinedFromElement.getJoinType(), group, predicate );
	}

	@Override
	public TableGroupJoin visitCrossJoinedFromElement(CrossJoinedFromElement joinedFromElement) {
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
	public Object visitQualifiedEntityJoinFromElement(QualifiedEntityJoinFromElement joinedFromElement) {
		throw new NotYetImplementedException();
	}

	@Override
	public org.hibernate.sql.ast.select.SelectClause visitSelectClause(SelectClause selectClause) {
		super.visitSelectClause( selectClause );
		currentQuerySpec().getSelectClause().makeDistinct( selectClause.isDistinct() );
		return currentQuerySpec().getSelectClause();
	}

	private org.hibernate.sql.ast.QuerySpec currentQuerySpec() {
		return querySpecStack.peek();
	}

	@Override
	public org.hibernate.sql.ast.select.Selection visitSelection(Selection selection) {
		org.hibernate.sql.ast.select.Selection ormSelection = new org.hibernate.sql.ast.select.Selection(
				(org.hibernate.sql.ast.expression.Expression) selection.getExpression().accept( this ),
				selection.getAlias()
		);

		currentQuerySpec().getSelectClause().selection( ormSelection );

		return ormSelection;
	}

	@Override
	public AttributeReference visitAttributeReferenceExpression(AttributeReferenceExpression expression) {
		// WARNING : works on the assumption that the referenced attribute is always singular.
		// I believe that is valid, but we will need to test
		// todo : verify if this is a valid assumption
		final SingularAttributeImplementor attribute = (SingularAttributeImplementor) expression.getBoundAttribute();
		final TableGroup tableGroup = fromClauseIndex.findResolvedTableGroup( expression.getAttributeBindingSource() );
		return tableGroup.resolve( attribute );
	}

	@Override
	public QueryLiteral visitLiteralStringExpression(LiteralStringExpression expression) {
		return new QueryLiteral(
				expression.getLiteralValue(),
				extractOrmType( expression.getExpressionType() )
		);
	}

	protected Type extractOrmType(org.hibernate.sqm.domain.Type sqmType) {
		if ( sqmType == null ) {
			return null;
		}

		return ( (SqmTypeImplementor) sqmType ).getOrmType();
	}

	@Override
	public QueryLiteral visitLiteralCharacterExpression(LiteralCharacterExpression expression) {
		return new QueryLiteral(
				expression.getLiteralValue(),
				extractOrmType( expression.getExpressionType() )
		);
	}

	@Override
	public QueryLiteral visitLiteralDoubleExpression(LiteralDoubleExpression expression) {
		return new QueryLiteral(
				expression.getLiteralValue(),
				extractOrmType( expression.getExpressionType() )
		);
	}

	@Override
	public QueryLiteral visitLiteralIntegerExpression(LiteralIntegerExpression expression) {
		return new QueryLiteral(
				expression.getLiteralValue(),
				extractOrmType( expression.getExpressionType() )
		);
	}

	@Override
	public QueryLiteral visitLiteralBigIntegerExpression(LiteralBigIntegerExpression expression) {
		return new QueryLiteral(
				expression.getLiteralValue(),
				extractOrmType( expression.getExpressionType() )
		);
	}

	@Override
	public QueryLiteral visitLiteralBigDecimalExpression(LiteralBigDecimalExpression expression) {
		return new QueryLiteral(
				expression.getLiteralValue(),
				extractOrmType( expression.getExpressionType() )
		);
	}

	@Override
	public QueryLiteral visitLiteralFloatExpression(LiteralFloatExpression expression) {
		return new QueryLiteral(
				expression.getLiteralValue(),
				extractOrmType( expression.getExpressionType() )
		);
	}

	@Override
	public QueryLiteral visitLiteralLongExpression(LiteralLongExpression expression) {
		return new QueryLiteral(
				expression.getLiteralValue(),
				extractOrmType( expression.getExpressionType() )
		);
	}

	@Override
	public QueryLiteral visitLiteralTrueExpression(LiteralTrueExpression expression) {
		return new QueryLiteral(
				Boolean.TRUE,
				extractOrmType( expression.getExpressionType() )
		);
	}

	@Override
	public QueryLiteral visitLiteralFalseExpression(LiteralFalseExpression expression) {
		return new QueryLiteral(
				Boolean.FALSE,
				extractOrmType( expression.getExpressionType() )
		);
	}

	@Override
	public QueryLiteral visitLiteralNullExpression(LiteralNullExpression expression) {
		return new QueryLiteral(
				null,
				extractOrmType( expression.getExpressionType() )
		);
	}

	@Override
	public Object visitConstantEnumExpression(ConstantEnumExpression expression) {
		return new QueryLiteral(
				expression.getValue(),
				extractOrmType( expression.getExpressionType() )
		);
	}

	@Override
	public Object visitConstantFieldExpression(ConstantFieldExpression expression) {
		return new QueryLiteral(
				expression.getValue(),
				extractOrmType( expression.getExpressionType() )
		);
	}

	@Override
	public NamedParameter visitNamedParameterExpression(NamedParameterExpression expression) {
		return new NamedParameter(
				expression.getName(),
				extractOrmType( expression.getExpressionType() )
		);
	}

	@Override
	public PositionalParameter visitPositionalParameterExpression(PositionalParameterExpression expression) {
		return new PositionalParameter(
				expression.getPosition(),
				extractOrmType( expression.getExpressionType() )
		);
	}

	@Override
	public org.hibernate.sql.ast.expression.AvgFunction visitAvgFunction(AvgFunction expression) {
		return new org.hibernate.sql.ast.expression.AvgFunction(
				(org.hibernate.sql.ast.expression.Expression) expression.getArgument().accept( this ),
				expression.isDistinct(),
				(BasicType) extractOrmType( expression.getExpressionType() )
		);
	}

	@Override
	public org.hibernate.sql.ast.expression.MaxFunction visitMaxFunction(MaxFunction expression) {
		return new org.hibernate.sql.ast.expression.MaxFunction(
				(org.hibernate.sql.ast.expression.Expression) expression.getArgument().accept( this ),
				expression.isDistinct(),
				(BasicType) extractOrmType( expression.getExpressionType() )
		);
	}

	@Override
	public org.hibernate.sql.ast.expression.MinFunction visitMinFunction(MinFunction expression) {
		return new org.hibernate.sql.ast.expression.MinFunction(
				(org.hibernate.sql.ast.expression.Expression) expression.getArgument().accept( this ),
				expression.isDistinct(),
				(BasicType) extractOrmType( expression.getExpressionType() )
		);
	}

	@Override
	public org.hibernate.sql.ast.expression.SumFunction visitSumFunction(SumFunction expression) {
		return new org.hibernate.sql.ast.expression.SumFunction(
				(org.hibernate.sql.ast.expression.Expression) expression.getArgument().accept( this ),
				expression.isDistinct(),
				(BasicType) extractOrmType( expression.getExpressionType() )
		);
	}

	@Override
	public org.hibernate.sql.ast.expression.CountFunction visitCountFunction(CountFunction expression) {
		return new org.hibernate.sql.ast.expression.CountFunction(
				(org.hibernate.sql.ast.expression.Expression) expression.getArgument().accept( this ),
				expression.isDistinct(),
				(BasicType) extractOrmType( expression.getExpressionType() )
		);
	}

	@Override
	public org.hibernate.sql.ast.expression.CountStarFunction visitCountStarFunction(CountStarFunction expression) {
		return new org.hibernate.sql.ast.expression.CountStarFunction(
				expression.isDistinct(),
				(BasicType) extractOrmType( expression.getExpressionType() )
		);
	}

	@Override
	public Object visitUnaryOperationExpression(UnaryOperationExpression expression) {
		return new org.hibernate.sql.ast.expression.UnaryOperationExpression(
				interpret( expression.getOperation() ),
				(org.hibernate.sql.ast.expression.Expression) expression.getOperand().accept( this ),
				(BasicType) extractOrmType( expression.getExpressionType() )
		);
	}

	private org.hibernate.sql.ast.expression.UnaryOperationExpression.Operation interpret(UnaryOperationExpression.Operation operation) {
		switch ( operation ) {
			case PLUS: {
				return org.hibernate.sql.ast.expression.UnaryOperationExpression.Operation.PLUS;
			}
			case MINUS: {
				return org.hibernate.sql.ast.expression.UnaryOperationExpression.Operation.MINUS;
			}
		}

		throw new IllegalStateException( "Unexpected UnaryOperationExpression Operation : " + operation );
	}

	@Override
	public org.hibernate.sql.ast.expression.Expression visitBinaryArithmeticExpression(BinaryArithmeticExpression expression) {
		if ( expression.getOperation() == BinaryArithmeticExpression.Operation.MODULO ) {
			return new NonStandardFunctionExpression(
					"mod",
					(BasicType) extractOrmType( expression.getExpressionType() ),
					(org.hibernate.sql.ast.expression.Expression) expression.getLeftHandOperand().accept( this ),
					(org.hibernate.sql.ast.expression.Expression) expression.getRightHandOperand().accept( this )
			);
		}
		return new org.hibernate.sql.ast.expression.BinaryArithmeticExpression(
				interpret( expression.getOperation() ),
				(org.hibernate.sql.ast.expression.Expression) expression.getLeftHandOperand().accept( this ),
				(org.hibernate.sql.ast.expression.Expression) expression.getRightHandOperand().accept( this ),
				(BasicType) extractOrmType( expression.getExpressionType() )
		);
	}

	private org.hibernate.sql.ast.expression.BinaryArithmeticExpression.Operation interpret(BinaryArithmeticExpression.Operation operation) {
		switch ( operation ) {
			case ADD: {
				return org.hibernate.sql.ast.expression.BinaryArithmeticExpression.Operation.ADD;
			}
			case SUBTRACT: {
				return org.hibernate.sql.ast.expression.BinaryArithmeticExpression.Operation.SUBTRACT;
			}
			case MULTIPLY: {
				return org.hibernate.sql.ast.expression.BinaryArithmeticExpression.Operation.MULTIPLY;
			}
			case DIVIDE: {
				return org.hibernate.sql.ast.expression.BinaryArithmeticExpression.Operation.DIVIDE;
			}
			case QUOT: {
				return org.hibernate.sql.ast.expression.BinaryArithmeticExpression.Operation.QUOT;
			}
		}

		throw new IllegalStateException( "Unexpected BinaryArithmeticExpression Operation : " + operation );
	}

	@Override
	public Object visitCoalesceExpression(CoalesceExpression expression) {
		final org.hibernate.sql.ast.expression.CoalesceExpression result = new org.hibernate.sql.ast.expression.CoalesceExpression();
		for ( Expression value : expression.getValues() ) {
			result.value(
					(org.hibernate.sql.ast.expression.Expression) value.accept( this )
			);
		}

		return result;
	}

	@Override
	public org.hibernate.sql.ast.expression.CaseSimpleExpression visitSimpleCaseExpression(CaseSimpleExpression expression) {
		final org.hibernate.sql.ast.expression.CaseSimpleExpression result = new org.hibernate.sql.ast.expression.CaseSimpleExpression(
				extractOrmType( expression.getExpressionType() ),
				(org.hibernate.sql.ast.expression.Expression) expression.getFixture().accept( this )
		);

		for ( CaseSimpleExpression.WhenFragment whenFragment : expression.getWhenFragments() ) {
			result.when(
					(org.hibernate.sql.ast.expression.Expression) whenFragment.getCheckValue().accept( this ),
					(org.hibernate.sql.ast.expression.Expression) whenFragment.getResult().accept( this )
			);
		}

		result.otherwise(
				(org.hibernate.sql.ast.expression.Expression) expression.getOtherwise().accept( this )
		);

		return result;
	}

	@Override
	public org.hibernate.sql.ast.expression.CaseSearchedExpression visitSearchedCaseExpression(CaseSearchedExpression expression) {
		final org.hibernate.sql.ast.expression.CaseSearchedExpression result = new org.hibernate.sql.ast.expression.CaseSearchedExpression(
				extractOrmType( expression.getExpressionType() )
		);

		for ( CaseSearchedExpression.WhenFragment whenFragment : expression.getWhenFragments() ) {
			result.when(
					(org.hibernate.sql.ast.predicate.Predicate) whenFragment.getPredicate().accept( this ),
					(org.hibernate.sql.ast.expression.Expression) whenFragment.getResult().accept( this )
			);
		}

		result.otherwise(
				(org.hibernate.sql.ast.expression.Expression) expression.getOtherwise().accept( this )
		);

		return result;
	}

	@Override
	public org.hibernate.sql.ast.expression.NullifExpression visitNullifExpression(NullifExpression expression) {
		return new org.hibernate.sql.ast.expression.NullifExpression(
				(org.hibernate.sql.ast.expression.Expression) expression.getFirstArgument().accept( this ),
				(org.hibernate.sql.ast.expression.Expression) expression.getSecondArgument().accept( this )
		);
	}

	@Override
	public org.hibernate.sql.ast.expression.ConcatExpression visitConcatExpression(ConcatExpression expression) {
		return new org.hibernate.sql.ast.expression.ConcatExpression(
				(org.hibernate.sql.ast.expression.Expression) expression.getLeftHandOperand().accept( this ),
				(org.hibernate.sql.ast.expression.Expression) expression.getLeftHandOperand().accept( this ),
				(BasicType) extractOrmType( expression.getExpressionType() )
		);
	}

	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Predicates


	@Override
	public org.hibernate.sql.ast.predicate.GroupedPredicate visitGroupedPredicate(GroupedPredicate predicate) {
		return new org.hibernate.sql.ast.predicate.GroupedPredicate(
				(org.hibernate.sql.ast.predicate.Predicate) predicate.getSubPredicate().accept( this )
		);
	}

	@Override
	public Junction visitAndPredicate(AndPredicate predicate) {
		final Junction conjunction = new Junction( Junction.Nature.CONJUNCTION );
		conjunction.add( (Predicate) predicate.getLeftHandPredicate().accept( this ) );
		conjunction.add( (Predicate) predicate.getRightHandPredicate().accept( this ) );
		return conjunction;
	}

	@Override
	public Junction visitOrPredicate(OrPredicate predicate) {
		final Junction disjunction = new Junction( Junction.Nature.DISJUNCTION );
		disjunction.add( (Predicate) predicate.getLeftHandPredicate().accept( this ) );
		disjunction.add( (Predicate) predicate.getRightHandPredicate().accept( this ) );
		return disjunction;
	}

	@Override
	public org.hibernate.sql.ast.predicate.NegatedPredicate visitNegatedPredicate(NegatedPredicate predicate) {
		return new org.hibernate.sql.ast.predicate.NegatedPredicate(
				(Predicate) predicate.getWrappedPredicate().accept( this )
		);
	}

	@Override
	public org.hibernate.sql.ast.predicate.RelationalPredicate visitRelationalPredicate(org.hibernate.sqm.query.predicate.RelationalPredicate predicate) {
		return new org.hibernate.sql.ast.predicate.RelationalPredicate(
				interpret( predicate.getOperator() ),
				(org.hibernate.sql.ast.expression.Expression) predicate.getLeftHandExpression().accept( this ),
				(org.hibernate.sql.ast.expression.Expression) predicate.getRightHandExpression().accept( this )
		);
	}

	private RelationalPredicate.Operator interpret(org.hibernate.sqm.query.predicate.RelationalPredicate.Operator operator) {
		switch ( operator ) {
			case EQUAL: {
				return RelationalPredicate.Operator.EQUAL;
			}
			case NOT_EQUAL: {
				return RelationalPredicate.Operator.NOT_EQUAL;
			}
			case GE: {
				return RelationalPredicate.Operator.GE;
			}
			case GT: {
				return RelationalPredicate.Operator.GT;
			}
			case LE: {
				return RelationalPredicate.Operator.LE;
			}
			case LT: {
				return RelationalPredicate.Operator.LT;
			}
		}

		throw new IllegalStateException( "Unexpected RelationalPredicate Type : " + operator );
	}

	@Override
	public org.hibernate.sql.ast.predicate.Predicate visitBetweenPredicate(BetweenPredicate predicate) {
		return new org.hibernate.sql.ast.predicate.BetweenPredicate(
				(org.hibernate.sql.ast.expression.Expression) predicate.getExpression().accept( this ),
				(org.hibernate.sql.ast.expression.Expression) predicate.getLowerBound().accept( this ),
				(org.hibernate.sql.ast.expression.Expression) predicate.getUpperBound().accept( this ),
				predicate.isNegated()
		);
	}

	@Override
	public org.hibernate.sql.ast.predicate.Predicate visitLikePredicate(LikePredicate predicate) {
		final org.hibernate.sql.ast.expression.Expression escapeExpression = predicate.getEscapeCharacter() == null
				? null
				: (org.hibernate.sql.ast.expression.Expression) predicate.getEscapeCharacter().accept( this );

		return new org.hibernate.sql.ast.predicate.LikePredicate(
				(org.hibernate.sql.ast.expression.Expression) predicate.getMatchExpression().accept( this ),
				(org.hibernate.sql.ast.expression.Expression) predicate.getPattern().accept( this ),
				escapeExpression,
				predicate.isNegated()
		);
	}

	@Override
	public org.hibernate.sql.ast.predicate.Predicate visitIsNullPredicate(NullnessPredicate predicate) {
		return new org.hibernate.sql.ast.predicate.NullnessPredicate(
				(org.hibernate.sql.ast.expression.Expression) predicate.getExpression().accept( this ),
				predicate.isNegated()
		);
	}

	@Override
	public org.hibernate.sql.ast.predicate.Predicate visitInListPredicate(org.hibernate.sqm.query.predicate.InListPredicate predicate) {
		final InListPredicate inPredicate = new InListPredicate(
				(org.hibernate.sql.ast.expression.Expression) predicate.getTestExpression().accept( this ),
				predicate.isNegated()
		);
		for ( org.hibernate.sqm.query.expression.Expression expression : predicate.getListExpressions() ) {
			inPredicate.addExpression( (org.hibernate.sql.ast.expression.Expression) expression.accept( this ) );
		}
		return inPredicate;
	}

	@Override
	public org.hibernate.sql.ast.predicate.Predicate visitInSubQueryPredicate(InSubQueryPredicate predicate) {
		return new org.hibernate.sql.ast.predicate.InSubQueryPredicate(
				(org.hibernate.sql.ast.expression.Expression) predicate.getTestExpression().accept( this ),
				(org.hibernate.sql.ast.QuerySpec) predicate.getSubQueryExpression().accept( this ),
				predicate.isNegated()
		);
	}
}
