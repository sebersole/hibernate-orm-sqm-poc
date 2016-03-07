package org.hibernate.sql.gen.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

import org.hibernate.AssertionFailure;
import org.hibernate.loader.plan.spi.Return;
import org.hibernate.sql.ast.SelectQuery;
import org.hibernate.sql.ast.expression.AttributeReference;
import org.hibernate.sql.ast.expression.ColumnBindingExpression;
import org.hibernate.sql.ast.expression.QueryLiteral;
import org.hibernate.sql.ast.from.CollectionTableGroup;
import org.hibernate.sql.ast.from.ColumnBinding;
import org.hibernate.sql.ast.from.EntityTableGroup;
import org.hibernate.sql.ast.from.TableGroup;
import org.hibernate.sql.ast.from.TableGroupJoin;
import org.hibernate.sql.ast.from.TableSpace;
import org.hibernate.sql.ast.predicate.Junction;
import org.hibernate.sql.ast.predicate.Predicate;
import org.hibernate.sql.ast.predicate.RelationalPredicate;
import org.hibernate.sql.gen.Callback;
import org.hibernate.sql.gen.JdbcSelectPlan;
import org.hibernate.sql.gen.NotYetImplementedException;
import org.hibernate.sql.gen.ParameterBinder;
import org.hibernate.sql.gen.QueryOptionBinder;
import org.hibernate.sql.orm.QueryOptions;
import org.hibernate.sql.orm.internal.mapping.ImprovedCollectionPersister;
import org.hibernate.sql.orm.internal.mapping.ImprovedEntityPersister;
import org.hibernate.sql.orm.internal.mapping.SingularAttributeImplementor;
import org.hibernate.sql.orm.internal.sqm.model.SqmTypeImplementor;
import org.hibernate.sqm.BaseSemanticQueryWalker;
import org.hibernate.sqm.domain.PluralAttribute;
import org.hibernate.sqm.domain.SingularAttribute;
import org.hibernate.sqm.query.DeleteStatement;
import org.hibernate.sqm.query.InsertSelectStatement;
import org.hibernate.sqm.query.QuerySpec;
import org.hibernate.sqm.query.SelectStatement;
import org.hibernate.sqm.query.UpdateStatement;
import org.hibernate.sqm.query.expression.AttributeReferenceExpression;
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
import org.hibernate.sqm.query.from.CrossJoinedFromElement;
import org.hibernate.sqm.query.from.FromClause;
import org.hibernate.sqm.query.from.FromElementSpace;
import org.hibernate.sqm.query.from.JoinedFromElement;
import org.hibernate.sqm.query.from.QualifiedAttributeJoinFromElement;
import org.hibernate.sqm.query.from.QualifiedEntityJoinFromElement;
import org.hibernate.sqm.query.from.RootEntityFromElement;
import org.hibernate.sqm.query.order.OrderByClause;
import org.hibernate.sqm.query.order.SortSpecification;
import org.hibernate.sqm.query.predicate.WhereClause;
import org.hibernate.sqm.query.select.SelectClause;
import org.hibernate.sqm.query.select.Selection;

/**
 * @author Steve Ebersole
 * @author John O'Hara
 */
public class SelectStatementInterpreter extends BaseSemanticQueryWalker {

	public static JdbcSelectPlan interpret(SelectStatement statement, QueryOptions queryOptions, Callback callback) {
		final SelectStatementInterpreter walker = new SelectStatementInterpreter( queryOptions, callback );
		return walker.interpret( statement );
	}

	public JdbcSelectPlan interpret(SelectStatement statement) {
		visitSelectStatement( statement );
		return null;
	}

	private final QueryOptions queryOptions;
	private final Callback callback;

	private final FromClauseIndex fromClauseIndex = new FromClauseIndex();

	private SelectQuery sqlAst;

	private final List<Return> returnDescriptors = new ArrayList<Return>();
	private List<QueryOptionBinder> optionBinders;
	private List<ParameterBinder> parameterBinders;

	private final SqlAliasBaseManager sqlAliasBaseManager = new SqlAliasBaseManager();

	public SelectStatementInterpreter(QueryOptions queryOptions, Callback callback) {
		this.queryOptions = queryOptions;
		this.callback = callback;
	}

	public SelectQuery getSelectQuery() {
		return sqlAst;
	}

	private List<ParameterBinder> getParameterBinders() {
		if ( parameterBinders == null ) {
			return Collections.emptyList();
		}
		else {
			return Collections.unmodifiableList( parameterBinders );
		}
	}

	private List<QueryOptionBinder> getOptionBinders() {
		if ( optionBinders == null ) {
			return Collections.emptyList();
		}
		else {
			return Collections.unmodifiableList( optionBinders );
		}
	}

	private List<Return> getReturnDescriptors() {
		return Collections.unmodifiableList( returnDescriptors );
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
				visitWhereClause( whereClause );
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
	public Void visitRootEntityFromElement(RootEntityFromElement rootEntityFromElement) {
		if ( fromClauseIndex.isResolved( rootEntityFromElement ) ) {
			return null;
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
	public TableGroupJoin visitQualifiedAttributeJoinFromElement(QualifiedAttributeJoinFromElement joinedFromElement) {
		if ( fromClauseIndex.isResolved( joinedFromElement ) ) {
			return null;
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
								RelationalPredicate.Type.EQUAL,
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
									RelationalPredicate.Type.EQUAL,
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
	public Object visitCrossJoinedFromElement(CrossJoinedFromElement joinedFromElement) {

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
	public Object visitSelectClause(SelectClause selectClause) {
		super.visitSelectClause( selectClause );
		currentQuerySpec().getSelectClause().makeDistinct( selectClause.isDistinct() );
		return currentQuerySpec().getSelectClause();
	}

	private org.hibernate.sql.ast.QuerySpec currentQuerySpec() {
		return querySpecStack.peek();
	}

	@Override
	public Object visitSelection(Selection selection) {
		org.hibernate.sql.ast.select.Selection ormSelection = new org.hibernate.sql.ast.select.Selection(
				(org.hibernate.sql.ast.expression.Expression) selection.getExpression().accept( this ),
				selection.getAlias()
		);

		currentQuerySpec().getSelectClause().selection( ormSelection );

		return selection;
	}

	@Override
	public Object visitAttributeReferenceExpression(AttributeReferenceExpression expression) {
		// WARNING : works on the assumption that the referenced attribute is always singular.
		// I believe that is valid, but we will need to test
		// todo : verify if this is a valid assumption
		final SingularAttributeImplementor attribute = (SingularAttributeImplementor) expression.getBoundAttribute();

		final TableGroup tableGroup = fromClauseIndex.findResolvedTableGroup( expression.getAttributeBindingSource() );

		final ColumnBinding[] columnBindings = tableGroup.resolveBindings( attribute );

		return new AttributeReference( attribute, columnBindings );
	}

	@Override
	public Object visitLiteralStringExpression(LiteralStringExpression expression) {
		return new QueryLiteral(
				expression.getLiteralValue(),
				( (SqmTypeImplementor) expression.getExpressionType() ).getOrmType()
		);
	}

	@Override
	public Object visitLiteralCharacterExpression(LiteralCharacterExpression expression) {
		return new QueryLiteral(
				expression.getLiteralValue(),
				( (SqmTypeImplementor) expression.getExpressionType() ).getOrmType()
		);
	}

	@Override
	public Object visitLiteralDoubleExpression(LiteralDoubleExpression expression) {
		return new QueryLiteral(
				expression.getLiteralValue(),
				( (SqmTypeImplementor) expression.getExpressionType() ).getOrmType()
		);
	}

	@Override
	public Object visitLiteralIntegerExpression(LiteralIntegerExpression expression) {
		return new QueryLiteral(
				expression.getLiteralValue(),
				( (SqmTypeImplementor) expression.getExpressionType() ).getOrmType()
		);
	}

	@Override
	public Object visitLiteralBigIntegerExpression(LiteralBigIntegerExpression expression) {
		return new QueryLiteral(
				expression.getLiteralValue(),
				( (SqmTypeImplementor) expression.getExpressionType() ).getOrmType()
		);
	}

	@Override
	public Object visitLiteralBigDecimalExpression(LiteralBigDecimalExpression expression) {
		return new QueryLiteral(
				expression.getLiteralValue(),
				( (SqmTypeImplementor) expression.getExpressionType() ).getOrmType()
		);
	}

	@Override
	public Object visitLiteralFloatExpression(LiteralFloatExpression expression) {
		return new QueryLiteral(
				expression.getLiteralValue(),
				( (SqmTypeImplementor) expression.getExpressionType() ).getOrmType()
		);
	}

	@Override
	public Object visitLiteralLongExpression(LiteralLongExpression expression) {
		return new QueryLiteral(
				expression.getLiteralValue(),
				( (SqmTypeImplementor) expression.getExpressionType() ).getOrmType()
		);
	}

	@Override
	public Object visitLiteralTrueExpression(LiteralTrueExpression expression) {
		return new QueryLiteral(
				Boolean.TRUE,
				( (SqmTypeImplementor) expression.getExpressionType() ).getOrmType()
		);
	}

	@Override
	public Object visitLiteralFalseExpression(LiteralFalseExpression expression) {
		return new QueryLiteral(
				Boolean.FALSE,
				( (SqmTypeImplementor) expression.getExpressionType() ).getOrmType()
		);
	}

	@Override
	public Object visitLiteralNullExpression(LiteralNullExpression expression) {
		return new QueryLiteral(
				null,
				( (SqmTypeImplementor) expression.getExpressionType() ).getOrmType()
		);
	}
}
