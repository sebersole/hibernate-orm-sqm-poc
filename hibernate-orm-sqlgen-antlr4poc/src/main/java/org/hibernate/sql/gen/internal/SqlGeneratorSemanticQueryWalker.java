package org.hibernate.sql.gen.internal;

import org.hibernate.loader.plan.spi.LoadPlan;
import org.hibernate.loader.plan.spi.QuerySpaces;
import org.hibernate.loader.plan.spi.Return;
import org.hibernate.persister.entity.SingleTableEntityPersister;
import org.hibernate.sqm.BaseSemanticQueryWalker;
import org.hibernate.sqm.query.DeleteStatement;
import org.hibernate.sqm.query.QuerySpec;
import org.hibernate.sqm.query.SelectStatement;
import org.hibernate.sqm.query.Statement;
import org.hibernate.sqm.query.UpdateStatement;
import org.hibernate.sqm.query.expression.AttributeReferenceExpression;
import org.hibernate.sqm.query.expression.AvgFunction;
import org.hibernate.sqm.query.expression.BinaryArithmeticExpression;
import org.hibernate.sqm.query.expression.ConcatExpression;
import org.hibernate.sqm.query.expression.ConstantEnumExpression;
import org.hibernate.sqm.query.expression.ConstantFieldExpression;
import org.hibernate.sqm.query.expression.CountFunction;
import org.hibernate.sqm.query.expression.CountStarFunction;
import org.hibernate.sqm.query.expression.EntityTypeExpression;
import org.hibernate.sqm.query.expression.FromElementReferenceExpression;
import org.hibernate.sqm.query.expression.FunctionExpression;
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
import org.hibernate.sqm.query.expression.PositionalParameterExpression;
import org.hibernate.sqm.query.expression.SubQueryExpression;
import org.hibernate.sqm.query.expression.SumFunction;
import org.hibernate.sqm.query.expression.UnaryOperationExpression;
import org.hibernate.sqm.query.from.CrossJoinedFromElement;
import org.hibernate.sqm.query.from.FromClause;
import org.hibernate.sqm.query.from.FromElementSpace;
import org.hibernate.sqm.query.from.QualifiedAttributeJoinFromElement;
import org.hibernate.sqm.query.from.QualifiedEntityJoinFromElement;
import org.hibernate.sqm.query.from.RootEntityFromElement;
import org.hibernate.sqm.query.from.TreatedJoinedFromElement;
import org.hibernate.sqm.query.order.OrderByClause;
import org.hibernate.sqm.query.order.SortSpecification;
import org.hibernate.sqm.query.predicate.AndPredicate;
import org.hibernate.sqm.query.predicate.BetweenPredicate;
import org.hibernate.sqm.query.predicate.GroupedPredicate;
import org.hibernate.sqm.query.predicate.InSubQueryPredicate;
import org.hibernate.sqm.query.predicate.InTupleListPredicate;
import org.hibernate.sqm.query.predicate.IsEmptyPredicate;
import org.hibernate.sqm.query.predicate.IsNullPredicate;
import org.hibernate.sqm.query.predicate.LikePredicate;
import org.hibernate.sqm.query.predicate.MemberOfPredicate;
import org.hibernate.sqm.query.predicate.NegatedPredicate;
import org.hibernate.sqm.query.predicate.OrPredicate;
import org.hibernate.sqm.query.predicate.RelationalPredicate;
import org.hibernate.sqm.query.predicate.WhereClause;
import org.hibernate.sqm.query.select.DynamicInstantiation;
import org.hibernate.sqm.query.select.SelectClause;
import org.hibernate.sqm.query.select.Selection;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author John O'Hara
 */
public class SqlGeneratorSemanticQueryWalker extends BaseSemanticQueryWalker {

	private Map<String, String> aliasMap;

	private final StringBuilder selectStringBuilder;
	private final StringBuilder fromStringBuilder;
	private final StringBuilder whereStringBuilder;

	//reference to current string builder
	private StringBuilder curStringBuilder;

	private List<? extends Return> returns;
	private QuerySpaces querySpaces;
	private LoadPlan.Disposition disposition;

	public SqlGeneratorSemanticQueryWalker() {
		super();

		this.aliasMap = new HashMap<String, String>();

		this.selectStringBuilder = new StringBuilder();
		this.fromStringBuilder = new StringBuilder();
		this.whereStringBuilder = new StringBuilder();

		this.curStringBuilder = this.selectStringBuilder;

		this.selectStringBuilder.append( "select " );
		this.fromStringBuilder.append( " from " );
		this.whereStringBuilder.append( " where " );

	}

	@Override
	public Statement visitStatement(Statement statement) {
		return super.visitStatement( statement );
	}

	@Override
	public SelectStatement visitSelectStatement(SelectStatement statement) {
		return super.visitSelectStatement( statement );
	}

	@Override
	public UpdateStatement visitUpdateStatement(UpdateStatement statement) {
		return super.visitUpdateStatement( statement );
	}

	@Override
	public DeleteStatement visitDeleteStatement(DeleteStatement statement) {
		return super.visitDeleteStatement( statement );
	}

	@Override
	public QuerySpec visitQuerySpec(QuerySpec querySpec) {
		this.curStringBuilder = this.selectStringBuilder;
		visitSelectClause( querySpec.getSelectClause() );

		this.curStringBuilder = this.fromStringBuilder;
		visitFromClause( querySpec.getFromClause() );

		this.curStringBuilder = this.whereStringBuilder;
		visitWhereClause( querySpec.getWhereClause() );

		return querySpec;
//		return super.visitQuerySpec( querySpec );
	}

	@Override
	public FromClause visitFromClause(FromClause fromClause) {
		FromClause _fromClause = super.visitFromClause( fromClause );

//		this.fromStringBuilder.append( " from" );

		for (FromElementSpace elementSpace : _fromClause.getFromElementSpaces()) {
			this.fromStringBuilder.append( ((SingleTableEntityPersister) ((EntityTypeDescriptorWithPersister) elementSpace.getRoot().getTypeDescriptor()).getPersister()).getTableName() );
			if ( elementSpace.getRoot().getAlias() != null ) {
				this.fromStringBuilder.append( " as ".concat( elementSpace.getRoot().getAlias() ) );
			}
//			this.fromStringBuilder.append(  elementSpace.getRoot().toString() );
		}


		return _fromClause;

	}

	@Override
	public FromElementSpace visitFromElementSpace(FromElementSpace fromElementSpace) {
		return super.visitFromElementSpace( fromElementSpace );
	}

	@Override
	public Object visitCrossJoinedFromElement(CrossJoinedFromElement joinedFromElement) {
		return super.visitCrossJoinedFromElement( joinedFromElement );
	}

	@Override
	public Object visitTreatedJoinFromElement(TreatedJoinedFromElement joinedFromElement) {
		return super.visitTreatedJoinFromElement( joinedFromElement );
	}

	@Override
	public Object visitQualifiedEntityJoinFromElement(QualifiedEntityJoinFromElement joinedFromElement) {
		return super.visitQualifiedEntityJoinFromElement( joinedFromElement );
	}

	@Override
	public Object visitQualifiedAttributeJoinFromElement(QualifiedAttributeJoinFromElement joinedFromElement) {
		return super.visitQualifiedAttributeJoinFromElement( joinedFromElement );
	}

	@Override
	public RootEntityFromElement visitRootEntityFromElement(RootEntityFromElement rootEntityFromElement) {

//		Return rootEntityReturn = new EntityReturnImpl(
//				new SimpleEntityIdentifierDescriptionImpl() ,
//				new EntityQuerySpaceImpl( rootEntityFromElement.getTypeDescriptor() )
//		);
//		this.returns.add( rootEntityReturn )

		return rootEntityFromElement;

	}

	@Override
	public SelectClause visitSelectClause(SelectClause selectClause) {
		return super.visitSelectClause( selectClause );
	}

	@Override
	public Selection visitSelection(Selection selection) {
		return super.visitSelection( selection );
	}

	@Override
	public DynamicInstantiation visitDynamicInstantiation(DynamicInstantiation dynamicInstantiation) {
		return super.visitDynamicInstantiation( dynamicInstantiation );
	}

	@Override
	public WhereClause visitWhereClause(WhereClause whereClause) {
		return super.visitWhereClause( whereClause );
	}

	@Override
	public GroupedPredicate visitGroupedPredicate(GroupedPredicate predicate) {
		return super.visitGroupedPredicate( predicate );
	}

	@Override
	public AndPredicate visitAndPredicate(AndPredicate predicate) {
		return super.visitAndPredicate( predicate );
	}

	@Override
	public OrPredicate visitOrPredicate(OrPredicate predicate) {
		return super.visitOrPredicate( predicate );
	}

	@Override
	public RelationalPredicate visitRelationalPredicate(RelationalPredicate predicate) {
		predicate.getLeftHandExpression().accept( this );
		this.whereStringBuilder.append( getWhereType( predicate.getType() ) );
		predicate.getRightHandExpression().accept( this );
		return predicate;
//		return super.visitRelationalPredicate( predicate );
	}

	private String getWhereType(RelationalPredicate.Type type) {
		switch (type) {
			case EQUAL:
				return " = ";
			case NOT_EQUAL:
				return " != ";
			case GT:
				return " > ";
			case GE:
				return " >= ";
			case LT:
				return " < ";
			case LE:
				return " <= ";
			default:
				return "";
		}
	}

	@Override
	public IsEmptyPredicate visitIsEmptyPredicate(IsEmptyPredicate predicate) {
		return super.visitIsEmptyPredicate( predicate );
	}

	@Override
	public IsNullPredicate visitIsNullPredicate(IsNullPredicate predicate) {
		return super.visitIsNullPredicate( predicate );
	}

	@Override
	public BetweenPredicate visitBetweenPredicate(BetweenPredicate predicate) {
		return super.visitBetweenPredicate( predicate );
	}

	@Override
	public LikePredicate visitLikePredicate(LikePredicate predicate) {
		return super.visitLikePredicate( predicate );
	}

	@Override
	public MemberOfPredicate visitMemberOfPredicate(MemberOfPredicate predicate) {
		return super.visitMemberOfPredicate( predicate );
	}

	@Override
	public NegatedPredicate visitNegatedPredicate(NegatedPredicate predicate) {
		return super.visitNegatedPredicate( predicate );
	}

	@Override
	public InTupleListPredicate visitInTupleListPredicate(InTupleListPredicate predicate) {
		return super.visitInTupleListPredicate( predicate );
	}

	@Override
	public InSubQueryPredicate visitInSubQueryPredicate(InSubQueryPredicate predicate) {
		return super.visitInSubQueryPredicate( predicate );
	}

	@Override
	public OrderByClause visitOrderByClause(OrderByClause orderByClause) {
		if ( orderByClause != null ) {
			return super.visitOrderByClause( orderByClause );
		} else {
			return null;
		}
	}

	@Override
	public SortSpecification visitSortSpecification(SortSpecification sortSpecification) {
		return super.visitSortSpecification( sortSpecification );
	}

	@Override
	public PositionalParameterExpression visitPositionalParameterExpression(PositionalParameterExpression expression) {
		return super.visitPositionalParameterExpression( expression );
	}

	@Override
	public NamedParameterExpression visitNamedParameterExpression(NamedParameterExpression expression) {
		return super.visitNamedParameterExpression( expression );
	}

	@Override
	public EntityTypeExpression visitEntityTypeExpression(EntityTypeExpression expression) {
		return super.visitEntityTypeExpression( expression );
	}

	@Override
	public UnaryOperationExpression visitUnaryOperationExpression(UnaryOperationExpression expression) {
		return super.visitUnaryOperationExpression( expression );
	}

	@Override
	public AttributeReferenceExpression visitAttributeReferenceExpression(AttributeReferenceExpression expression) {
		if ( expression.getAttributeDescriptor() != null ) {
			this.curStringBuilder.append( expression.getAttributeDescriptor().toString() );
		}
		return expression;
	}

	@Override
	public FromElementReferenceExpression visitFromElementReferenceExpression(FromElementReferenceExpression expression) {
		FromElementReferenceExpression fromElementReferenceExpression = super.visitFromElementReferenceExpression( expression );

		//build alias map
		if ( fromElementReferenceExpression.getFromElement().getAlias() != null ) {
			if ( !this.aliasMap.containsKey( fromElementReferenceExpression.getFromElement().getAlias() ) ) {
				this.aliasMap.put( fromElementReferenceExpression.getFromElement().getAlias(), fromElementReferenceExpression.getFromElement().getTypeDescriptor().getTypeName() );
			}
		}

//		this.fromStringBuilder.append( ((EntityTypeDescriptor) fromElementReferenceExpression.getFromElement().getTypeDescriptor()) )


		return fromElementReferenceExpression;
	}

	@Override
	public FunctionExpression visitFunctionExpression(FunctionExpression expression) {
		return super.visitFunctionExpression( expression );
	}

	@Override
	public AvgFunction visitAvgFunction(AvgFunction expression) {
		return super.visitAvgFunction( expression );
	}

	@Override
	public CountStarFunction visitCountStarFunction(CountStarFunction expression) {
		return super.visitCountStarFunction( expression );
	}

	@Override
	public CountFunction visitCountFunction(CountFunction expression) {
		return super.visitCountFunction( expression );
	}

	@Override
	public MaxFunction visitMaxFunction(MaxFunction expression) {
		return super.visitMaxFunction( expression );
	}

	@Override
	public MinFunction visitMinFunction(MinFunction expression) {
		return super.visitMinFunction( expression );
	}

	@Override
	public SumFunction visitSumFunction(SumFunction expression) {
		return super.visitSumFunction( expression );
	}

	@Override
	public LiteralStringExpression visitLiteralStringExpression(LiteralStringExpression expression) {
		this.curStringBuilder.append( expression.getLiteralValue() );
		return super.visitLiteralStringExpression( expression );
	}

	@Override
	public LiteralCharacterExpression visitLiteralCharacterExpression(LiteralCharacterExpression expression) {
		this.curStringBuilder.append( expression.getLiteralValue() );
		return super.visitLiteralCharacterExpression( expression );
	}

	@Override
	public LiteralDoubleExpression visitLiteralDoubleExpression(LiteralDoubleExpression expression) {
		this.curStringBuilder.append( expression.getLiteralValue() );
		return super.visitLiteralDoubleExpression( expression );
	}

	@Override
	public LiteralIntegerExpression visitLiteralIntegerExpression(LiteralIntegerExpression expression) {
		this.curStringBuilder.append( expression.getLiteralValue() );
		return super.visitLiteralIntegerExpression( expression );
	}

	@Override
	public LiteralBigIntegerExpression visitLiteralBigIntegerExpression(LiteralBigIntegerExpression expression) {
		this.curStringBuilder.append( expression.getLiteralValue() );
		return super.visitLiteralBigIntegerExpression( expression );
	}

	@Override
	public LiteralBigDecimalExpression visitLiteralBigDecimalExpression(LiteralBigDecimalExpression expression) {
		this.curStringBuilder.append( expression.getLiteralValue() );
		return super.visitLiteralBigDecimalExpression( expression );
	}

	@Override
	public LiteralFloatExpression visitLiteralFloatExpression(LiteralFloatExpression expression) {
		this.curStringBuilder.append( expression.getLiteralValue() );
		return super.visitLiteralFloatExpression( expression );
	}

	@Override
	public LiteralLongExpression visitLiteralLongExpression(LiteralLongExpression expression) {
		this.curStringBuilder.append( expression.getLiteralValue() );
		return super.visitLiteralLongExpression( expression );
	}

	@Override
	public LiteralTrueExpression visitLiteralTrueExpression(LiteralTrueExpression expression) {
		this.curStringBuilder.append( expression.getLiteralValue() );
		return super.visitLiteralTrueExpression( expression );
	}

	@Override
	public LiteralFalseExpression visitLiteralFalseExpression(LiteralFalseExpression expression) {
		this.curStringBuilder.append( expression.getLiteralValue() );
		return super.visitLiteralFalseExpression( expression );
	}

	@Override
	public LiteralNullExpression visitLiteralNullExpression(LiteralNullExpression expression) {
		this.curStringBuilder.append( expression.getLiteralValue() );
		return super.visitLiteralNullExpression( expression );
	}

	@Override
	public ConcatExpression visitConcatExpression(ConcatExpression expression) {
		return super.visitConcatExpression( expression );
	}

	@Override
	public ConstantEnumExpression visitConstantEnumExpression(ConstantEnumExpression expression) {
		return super.visitConstantEnumExpression( expression );
	}

	@Override
	public ConstantFieldExpression visitConstantFieldExpression(ConstantFieldExpression expression) {
		return super.visitConstantFieldExpression( expression );
	}

	@Override
	public BinaryArithmeticExpression visitBinaryArithmeticExpression(BinaryArithmeticExpression expression) {
		return super.visitBinaryArithmeticExpression( expression );
	}

	@Override
	public SubQueryExpression visitSubQueryExpression(SubQueryExpression expression) {
		return super.visitSubQueryExpression( expression );
	}

	public List<? extends Return> getReturns() {
		return returns;
	}

	public void setReturns(List<? extends Return> returns) {
		this.returns = returns;
	}

	public QuerySpaces getQuerySpaces() {
		return querySpaces;
	}

	public LoadPlan.Disposition getDisposition() {
		return disposition;
	}

	public boolean areLazyAttributesForceFetched() {
		return false;
	}

	public String getFromString() {
		return fromStringBuilder.toString();
	}

	public String getSelectString() {
		return selectStringBuilder.toString();
	}

	public String getWhereString() {
		return whereStringBuilder.toString();
	}
}
