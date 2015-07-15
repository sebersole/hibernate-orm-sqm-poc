/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.hql.parser.process;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.hql.parser.LiteralNumberFormatException;
import org.hibernate.hql.parser.ParsingException;
import org.hibernate.hql.parser.SemanticException;
import org.hibernate.hql.parser.antlr.HqlParser;
import org.hibernate.hql.parser.antlr.HqlParserBaseVisitor;
import org.hibernate.hql.parser.process.path.AttributePathPart;
import org.hibernate.hql.parser.process.path.AttributePathResolver;
import org.hibernate.hql.parser.process.path.AttributePathResolverStack;
import org.hibernate.hql.parser.process.path.IndexedAttributeRootPathResolver;
import org.hibernate.hql.parser.model.CollectionTypeDescriptor;
import org.hibernate.hql.parser.model.EntityTypeDescriptor;
import org.hibernate.hql.parser.model.TypeDescriptor;
import org.hibernate.hql.parser.semantic.QuerySpec;
import org.hibernate.hql.parser.semantic.SelectStatement;
import org.hibernate.hql.parser.semantic.expression.AttributeReferenceExpression;
import org.hibernate.hql.parser.semantic.expression.BinaryArithmeticExpression;
import org.hibernate.hql.parser.semantic.expression.ConcatExpression;
import org.hibernate.hql.parser.semantic.expression.ConstantEnumExpression;
import org.hibernate.hql.parser.semantic.expression.ConstantExpression;
import org.hibernate.hql.parser.semantic.expression.ConstantFieldExpression;
import org.hibernate.hql.parser.semantic.expression.EntityTypeExpression;
import org.hibernate.hql.parser.semantic.expression.Expression;
import org.hibernate.hql.parser.semantic.expression.FromElementReferenceExpression;
import org.hibernate.hql.parser.semantic.expression.FunctionExpression;
import org.hibernate.hql.parser.semantic.expression.LiteralBigDecimalExpression;
import org.hibernate.hql.parser.semantic.expression.LiteralBigIntegerExpression;
import org.hibernate.hql.parser.semantic.expression.LiteralCharacterExpression;
import org.hibernate.hql.parser.semantic.expression.LiteralDoubleExpression;
import org.hibernate.hql.parser.semantic.expression.LiteralExpression;
import org.hibernate.hql.parser.semantic.expression.LiteralFalseExpression;
import org.hibernate.hql.parser.semantic.expression.LiteralFloatExpression;
import org.hibernate.hql.parser.semantic.expression.LiteralIntegerExpression;
import org.hibernate.hql.parser.semantic.expression.LiteralLongExpression;
import org.hibernate.hql.parser.semantic.expression.LiteralNullExpression;
import org.hibernate.hql.parser.semantic.expression.LiteralStringExpression;
import org.hibernate.hql.parser.semantic.expression.LiteralTrueExpression;
import org.hibernate.hql.parser.semantic.expression.ParameterNamedExpression;
import org.hibernate.hql.parser.semantic.expression.ParameterPositionalExpression;
import org.hibernate.hql.parser.semantic.expression.UnaryOperationExpression;
import org.hibernate.hql.parser.semantic.from.FromClause;
import org.hibernate.hql.parser.semantic.from.FromElement;
import org.hibernate.hql.parser.semantic.from.JoinedFromElement;
import org.hibernate.hql.parser.semantic.from.TreatedFromElement;
import org.hibernate.hql.parser.semantic.from.TreatedJoinedFromElement;
import org.hibernate.hql.parser.semantic.order.OrderByClause;
import org.hibernate.hql.parser.semantic.order.SortOrder;
import org.hibernate.hql.parser.semantic.order.SortSpecification;
import org.hibernate.hql.parser.semantic.predicate.AndPredicate;
import org.hibernate.hql.parser.semantic.predicate.BetweenPredicate;
import org.hibernate.hql.parser.semantic.predicate.GroupedPredicate;
import org.hibernate.hql.parser.semantic.predicate.IndexedAttributePathPart;
import org.hibernate.hql.parser.semantic.predicate.IsEmptyPredicate;
import org.hibernate.hql.parser.semantic.predicate.IsNullPredicate;
import org.hibernate.hql.parser.semantic.predicate.LikePredicate;
import org.hibernate.hql.parser.semantic.predicate.MemberOfPredicate;
import org.hibernate.hql.parser.semantic.predicate.NegatedPredicate;
import org.hibernate.hql.parser.semantic.predicate.OrPredicate;
import org.hibernate.hql.parser.semantic.predicate.Predicate;
import org.hibernate.hql.parser.semantic.predicate.RelationalPredicate;
import org.hibernate.hql.parser.semantic.predicate.WhereClause;
import org.hibernate.hql.parser.semantic.select.AliasedDynamicInstantiationArgument;
import org.hibernate.hql.parser.semantic.select.DynamicInstantiation;
import org.hibernate.hql.parser.semantic.select.SelectClause;
import org.hibernate.hql.parser.semantic.select.SelectList;
import org.hibernate.hql.parser.semantic.select.SelectListItem;
import org.hibernate.hql.parser.semantic.select.Selection;

import org.jboss.logging.Logger;

/**
 * @author Steve Ebersole
 */
public abstract class AbstractHqlParseTreeVisitor extends HqlParserBaseVisitor {
	private static final Logger log = Logger.getLogger( AbstractHqlParseTreeVisitor.class );

	private final ParsingContext parsingContext;
	protected final AttributePathResolverStack attributePathResolverStack = new AttributePathResolverStack();

	public AbstractHqlParseTreeVisitor(ParsingContext parsingContext) {
		this.parsingContext = parsingContext;
	}

	public abstract FromClause getCurrentFromClause();

	public AttributePathResolver getCurrentAttributePathResolver() {
		return attributePathResolverStack.getCurrent();
	}

	@Override
	public SelectStatement visitSelectStatement(HqlParser.SelectStatementContext ctx) {
		final SelectStatement selectStatement = new SelectStatement( parsingContext );
		selectStatement.applyQuerySpec( visitQuerySpec( ctx.querySpec() ) );
		if ( ctx.orderByClause() != null ) {
			selectStatement.applyOrderByClause( visitOrderByClause( ctx.orderByClause() ) );
		}

		return selectStatement;
	}

	@Override
	public OrderByClause visitOrderByClause(HqlParser.OrderByClauseContext ctx) {
		final OrderByClause orderByClause = new OrderByClause( parsingContext );
		for ( HqlParser.SortSpecificationContext sortSpecificationContext : ctx.sortSpecification() ) {
			orderByClause.addSortSpecification( visitSortSpecification( sortSpecificationContext ) );
		}
		return orderByClause;
	}

	@Override
	public SortSpecification visitSortSpecification(HqlParser.SortSpecificationContext ctx) {
		final Expression sortExpression = (Expression) ctx.expression().accept( this );
		final String collation;
		if ( ctx.collationSpecification() != null && ctx.collationSpecification().collateName() != null ) {
			collation = ctx.collationSpecification().collateName().dotIdentifierSequence().getText();
		}
		else {
			collation = null;
		}
		final SortOrder sortOrder;
		if ( ctx.orderingSpecification() != null ) {
			sortOrder = SortOrder.interpret( ctx.orderingSpecification().getText() );
		}
		else {
			sortOrder = null;
		}
		return new SortSpecification( sortExpression, collation, sortOrder );
	}

	@Override
	public QuerySpec visitQuerySpec(HqlParser.QuerySpecContext ctx) {
		final SelectClause selectClause;
		if ( ctx.selectClause() != null ) {
			selectClause = visitSelectClause( ctx.selectClause() );
		}
		else {
			selectClause = buildInferredSelectClause( getCurrentFromClause() );
		}

		final WhereClause whereClause;
		if ( ctx.whereClause() != null ) {
			whereClause = visitWhereClause( ctx.whereClause() );
		}
		else {
			whereClause = null;
		}
		return new QuerySpec( parsingContext, getCurrentFromClause(), selectClause, whereClause );
	}

	protected SelectClause buildInferredSelectClause(FromClause fromClause) {
		// for now, this is slightly different than the legacy behavior where
		// the root and each non-fetched-join was selected.  For now, here, we simply
		// select the root
		return new SelectClause(
				new SelectList(
						new SelectListItem(
								new FromElementReferenceExpression(
										fromClause.getFromElementSpaces().get( 0 ).getRoot()
								)
						)
				)
		);
	}

	@Override
	public FromClause visitFromClause(HqlParser.FromClauseContext ctx) {
		return getCurrentFromClause();
	}

	@Override
	public SelectClause visitSelectClause(HqlParser.SelectClauseContext ctx) {
		return new SelectClause(
				visitSelection( ctx.selection() ),
				ctx.distinctKeyword() != null
		);
	}

	@Override
	public Selection visitSelection(HqlParser.SelectionContext ctx) {
		if ( ctx.dynamicInstantiation() != null ) {
			return visitDynamicInstantiation( ctx.dynamicInstantiation() );
		}
		else if ( ctx.jpaSelectObjectSyntax() != null ) {
			return visitJpaSelectObjectSyntax( ctx.jpaSelectObjectSyntax() );
		}
		else if ( ctx.selectItemList() != null ) {
			return visitSelectItemList( ctx.selectItemList() );
		}

		throw new ParsingException( "Unexpected selection rule type : " + ctx );
	}

	@Override
	public DynamicInstantiation visitDynamicInstantiation(HqlParser.DynamicInstantiationContext ctx) {
		final String className = ctx.dynamicInstantiationTarget().getText();
		final DynamicInstantiation dynamicInstantiation;
		try {
			dynamicInstantiation = new DynamicInstantiation(
					parsingContext.getConsumerContext().classByName( className )
			);
		}
		catch (ClassNotFoundException e) {
			throw new SemanticException( "Unable to resolve class named for dynamic instantiation : " + className );
		}

		for ( HqlParser.DynamicInstantiationArgContext arg : ctx.dynamicInstantiationArgs().dynamicInstantiationArg() ) {
			dynamicInstantiation.addArgument( visitDynamicInstantiationArg( arg ) );
		}

		return dynamicInstantiation;
	}

	@Override
	public AliasedDynamicInstantiationArgument visitDynamicInstantiationArg(HqlParser.DynamicInstantiationArgContext ctx) {
		return new AliasedDynamicInstantiationArgument(
				visitDynamicInstantiationArgExpression( ctx.dynamicInstantiationArgExpression() ),
				ctx.IDENTIFIER() == null ? null : ctx.IDENTIFIER().getText()
		);
	}

	@Override
	public Expression visitDynamicInstantiationArgExpression(HqlParser.DynamicInstantiationArgExpressionContext ctx) {
		if ( ctx.dynamicInstantiation() != null ) {
			return visitDynamicInstantiation( ctx.dynamicInstantiation() );
		}
		else if ( ctx.expression() != null ) {
			return (Expression) ctx.expression().accept( this );
		}

		throw new ParsingException( "Unexpected dynamic-instantiation-argument rule type : " + ctx );
	}

	@Override
	public Selection visitJpaSelectObjectSyntax(HqlParser.JpaSelectObjectSyntaxContext ctx) {
		final String alias = ctx.IDENTIFIER().getText();
		final FromElement fromElement = getCurrentFromClause().findFromElementByAlias( alias );
		if ( fromElement == null ) {
			throw new SemanticException( "Unable to resolve alias [" +  alias + "] in selection [" + ctx.getText() + "]" );
		}
		return new SelectList(
				new SelectListItem(
						new FromElementReferenceExpression( fromElement )
				)
		);
	}

	@Override
	public SelectList visitSelectItemList(HqlParser.SelectItemListContext ctx) {
		final SelectList selectList = new SelectList();
		for ( HqlParser.SelectItemContext selectItemContext : ctx.selectItem() ) {
			selectList.addSelectListItem( visitSelectItem( selectItemContext ) );
		}
		return selectList;
	}

	@Override
	public SelectListItem visitSelectItem(HqlParser.SelectItemContext ctx) {
		return new SelectListItem(
				(Expression) ctx.expression().accept( this ),
				ctx.IDENTIFIER() == null ? null : ctx.IDENTIFIER().getText()
		);
	}

	@Override
	public Predicate visitQualifiedJoinPredicate(HqlParser.QualifiedJoinPredicateContext ctx) {
		return (Predicate) ctx.predicate().accept( this );
	}

	@Override
	public WhereClause visitWhereClause(HqlParser.WhereClauseContext ctx) {
		return new WhereClause( parsingContext, (Predicate) ctx.predicate().accept( this ) );
	}

	@Override
	public GroupedPredicate visitGroupedPredicate(HqlParser.GroupedPredicateContext ctx) {
		return new GroupedPredicate( (Predicate) ctx.predicate().accept( this ) );
	}

	@Override
	public AndPredicate visitAndPredicate(HqlParser.AndPredicateContext ctx) {
		return new AndPredicate(
				(Predicate) ctx.predicate( 0 ).accept( this ),
				(Predicate) ctx.predicate( 1 ).accept( this )
		);
	}

	@Override
	public OrPredicate visitOrPredicate(HqlParser.OrPredicateContext ctx) {
		return new OrPredicate(
				(Predicate) ctx.predicate( 0 ).accept( this ),
				(Predicate) ctx.predicate( 1 ).accept( this )
		);
	}

	@Override
	public NegatedPredicate visitNegatedPredicate(HqlParser.NegatedPredicateContext ctx) {
		return new NegatedPredicate( (Predicate) ctx.predicate().accept( this ) );
	}

	@Override
	public IsNullPredicate visitIsNullPredicate(HqlParser.IsNullPredicateContext ctx) {
		return new IsNullPredicate( (Expression) ctx.expression().accept( this ) );
	}

	@Override
	public IsEmptyPredicate visitIsEmptyPredicate(HqlParser.IsEmptyPredicateContext ctx) {
		return new IsEmptyPredicate( (Expression) ctx.expression().accept( this ) );
	}

	@Override
	public Object visitEqualityPredicate(HqlParser.EqualityPredicateContext ctx) {
		return new RelationalPredicate(
				RelationalPredicate.Type.EQUAL,
				(Expression) ctx.expression().get( 0 ).accept( this ),
				(Expression) ctx.expression().get( 1 ).accept( this )
		);
	}

	@Override
	public Object visitInequalityPredicate(HqlParser.InequalityPredicateContext ctx) {
		return new RelationalPredicate(
				RelationalPredicate.Type.NOT_EQUAL,
				(Expression) ctx.expression().get( 0 ).accept( this ),
				(Expression) ctx.expression().get( 1 ).accept( this )
		);
	}

	@Override
	public Object visitGreaterThanPredicate(HqlParser.GreaterThanPredicateContext ctx) {
		return new RelationalPredicate(
				RelationalPredicate.Type.GT,
				(Expression) ctx.expression().get( 0 ).accept( this ),
				(Expression) ctx.expression().get( 1 ).accept( this )
		);
	}

	@Override
	public Object visitGreaterThanOrEqualPredicate(HqlParser.GreaterThanOrEqualPredicateContext ctx) {
		return new RelationalPredicate(
				RelationalPredicate.Type.GE,
				(Expression) ctx.expression().get( 0 ).accept( this ),
				(Expression) ctx.expression().get( 1 ).accept( this )
		);
	}

	@Override
	public Object visitLessThanPredicate(HqlParser.LessThanPredicateContext ctx) {
		return new RelationalPredicate(
				RelationalPredicate.Type.LT,
				(Expression) ctx.expression().get( 0 ).accept( this ),
				(Expression) ctx.expression().get( 1 ).accept( this )
		);
	}

	@Override
	public Object visitLessThanOrEqualPredicate(HqlParser.LessThanOrEqualPredicateContext ctx) {
		return new RelationalPredicate(
				RelationalPredicate.Type.LE,
				(Expression) ctx.expression().get( 0 ).accept( this ),
				(Expression) ctx.expression().get( 1 ).accept( this )
		);
	}

	@Override
	public Object visitBetweenPredicate(HqlParser.BetweenPredicateContext ctx) {
		return new BetweenPredicate(
				(Expression) ctx.expression().get( 0 ).accept( this ),
				(Expression) ctx.expression().get( 1 ).accept( this ),
				(Expression) ctx.expression().get( 2 ).accept( this )
		);
	}

	@Override
	public Object visitLikePredicate(HqlParser.LikePredicateContext ctx) {
		if ( ctx.likeEscape() != null ) {
			return new LikePredicate(
					(Expression) ctx.expression().get( 0 ).accept( this ),
					(Expression) ctx.expression().get( 1 ).accept( this ),
					(Expression) ctx.likeEscape().expression().accept( this )
			);
		}
		else {
			return new LikePredicate(
					(Expression) ctx.expression().get( 0 ).accept( this ),
					(Expression) ctx.expression().get( 1 ).accept( this )
			);
		}
	}

	@Override
	public Object visitMemberOfPredicate(HqlParser.MemberOfPredicateContext ctx) {
		final Object pathResolution = ctx.path().accept( this );
		if ( !AttributeReferenceExpression.class.isInstance( pathResolution ) ) {
			throw new SemanticException( "Could not resolve path [" + ctx.path().getText() + "] as an attribute reference" );
		}
		final AttributeReferenceExpression attributeReference = (AttributeReferenceExpression) pathResolution;
		if ( !CollectionTypeDescriptor.class.isInstance( attributeReference.getTypeDescriptor() ) ) {
			throw new SemanticException( "Path argument to MEMBER OF must be a collection" );
		}
		return new MemberOfPredicate( attributeReference );
	}

	@Override
	public Object visitSimplePath(HqlParser.SimplePathContext ctx) {
		final AttributePathPart attributePathPart = getCurrentAttributePathResolver().resolvePath( ctx.dotIdentifierSequence() );
		if ( attributePathPart != null ) {
			return attributePathPart;
		}

		final String pathText = ctx.getText();

		final EntityTypeDescriptor entityType = parsingContext.getConsumerContext().resolveEntityReference( pathText );
		if ( entityType != null ) {
			return new EntityTypeExpression( entityType );
		}

		// 5th level precedence : constant reference
		try {
			return resolveConstantExpression( pathText );
		}
		catch (SemanticException e) {
			log.debug( e.getMessage() );
		}

		// if we get here we had a problem interpreting the dot-ident sequence
		throw new SemanticException( "Could not interpret token : " + pathText );
	}

	@SuppressWarnings("unchecked")
	protected ConstantExpression resolveConstantExpression(String reference) {
		// todo : hook in "import" resolution using the ParsingContext
		final int dotPosition = reference.lastIndexOf( '.' );
		final String className = reference.substring( 0, dotPosition - 1 );
		final String fieldName = reference.substring( dotPosition+1, reference.length() );

		try {
			final Class clazz = parsingContext.getConsumerContext().classByName( className );
			if ( clazz.isEnum() ) {
				try {
					return new ConstantEnumExpression( Enum.valueOf( clazz, fieldName ) );
				}
				catch (IllegalArgumentException e) {
					throw new SemanticException( "Name [" + fieldName + "] does not represent an enum constant on enum class [" + className + "]" );
				}
			}
			else {
				try {
					final Field field = clazz.getField( fieldName );
					if ( !Modifier.isStatic( field.getModifiers() ) ) {
						throw new SemanticException( "Field [" + fieldName + "] is not static on class [" + className + "]" );
					}
					field.setAccessible( true );
					return new ConstantFieldExpression( field.get( null ) );
				}
				catch (NoSuchFieldException e) {
					throw new SemanticException( "Name [" + fieldName + "] does not represent a field on class [" + className + "]", e );
				}
				catch (SecurityException e) {
					throw new SemanticException( "Field [" + fieldName + "] is not accessible on class [" + className + "]", e );
				}
				catch (IllegalAccessException e) {
					throw new SemanticException( "Unable to access field [" + fieldName + "] on class [" + className + "]", e );
				}
			}
		}
		catch (ClassNotFoundException e) {
			throw new SemanticException( "Cannot resolve class for query constant [" + reference + "]" );
		}
	}

	@Override
	public AttributePathPart visitTreatedPath(HqlParser.TreatedPathContext ctx) {
		final FromElement fromElement = (FromElement) getCurrentAttributePathResolver().resolvePath( ctx.dotIdentifierSequence().get( 0 ) );
		if ( fromElement == null ) {
			throw new SemanticException( "Could not resolve path [" + ctx.dotIdentifierSequence().get( 0 ).getText() + "] as base for TREAT-AS expression" );
		}

		final String treatAsName = ctx.dotIdentifierSequence().get( 1 ).getText();

		final TypeDescriptor treatAsTypeDescriptor = parsingContext.getConsumerContext().resolveEntityReference( treatAsName );
		if ( treatAsTypeDescriptor == null ) {
			throw new SemanticException( "TREAT-AS target type [" + treatAsName + "] did not reference an entity" );
		}

		fromElement.addTreatedAs( treatAsTypeDescriptor );

		if ( fromElement instanceof JoinedFromElement ) {
			return new TreatedJoinedFromElement( (JoinedFromElement) fromElement, treatAsTypeDescriptor );
		}
		else {
			return new TreatedFromElement( fromElement, treatAsTypeDescriptor );
		}
	}

	@Override
	public AttributePathPart visitIndexedPath(HqlParser.IndexedPathContext ctx) {
		if ( ctx.path().size() > 2 ) {
			throw new ParsingException( "Encountered unexpected number of path expressions in indexed path reference : " + ctx.getText() );
		}

		final IndexedAttributePathPart indexedReference = new IndexedAttributePathPart(
				(AttributePathPart) ctx.path( 0 ).accept( this ),
				(Expression) ctx.expression().accept( this )
		);

		if ( ctx.path( 1 ) == null ) {
			return indexedReference;
		}


		// we have a de-reference of the indexed reference.  push a new path resolver
		// that handles the indexed reference as the root to the path
		attributePathResolverStack.push(
				new IndexedAttributeRootPathResolver( getCurrentFromClause(), indexedReference )
		);
		try {
			return (AttributePathPart) ctx.path( 1 ).accept( this );
		}
		finally {
			attributePathResolverStack.pop();
		}
	}

	@Override
	public ConcatExpression visitConcatenationExpression(HqlParser.ConcatenationExpressionContext ctx) {
		if ( ctx.expression().size() != 2 ) {
			throw new ParsingException( "Expecting 2 operands to the concat operator" );
		}
		return new ConcatExpression(
				(Expression) ctx.expression( 0 ).accept( this ),
				(Expression) ctx.expression( 0 ).accept( this )
		);
	}

	@Override
	public Object visitAdditionExpression(HqlParser.AdditionExpressionContext ctx) {
		if ( ctx.expression().size() != 2 ) {
			throw new ParsingException( "Expecting 2 operands to the + operator" );
		}
		return new BinaryArithmeticExpression(
				BinaryArithmeticExpression.Operation.ADD,
				(Expression) ctx.expression( 0 ).accept( this ),
				(Expression) ctx.expression( 0 ).accept( this )
		);
	}

	@Override
	public Object visitSubtractionExpression(HqlParser.SubtractionExpressionContext ctx) {
		if ( ctx.expression().size() != 2 ) {
			throw new ParsingException( "Expecting 2 operands to the - operator" );
		}
		return new BinaryArithmeticExpression(
				BinaryArithmeticExpression.Operation.SUBTRACT,
				(Expression) ctx.expression( 0 ).accept( this ),
				(Expression) ctx.expression( 0 ).accept( this )
		);
	}

	@Override
	public Object visitMultiplicationExpression(HqlParser.MultiplicationExpressionContext ctx) {
		if ( ctx.expression().size() != 2 ) {
			throw new ParsingException( "Expecting 2 operands to the * operator" );
		}
		return new BinaryArithmeticExpression(
				BinaryArithmeticExpression.Operation.MULTIPLY,
				(Expression) ctx.expression( 0 ).accept( this ),
				(Expression) ctx.expression( 0 ).accept( this )
		);
	}

	@Override
	public Object visitDivisionExpression(HqlParser.DivisionExpressionContext ctx) {
		if ( ctx.expression().size() != 2 ) {
			throw new ParsingException( "Expecting 2 operands to the / operator" );
		}
		return new BinaryArithmeticExpression(
				BinaryArithmeticExpression.Operation.DIVIDE,
				(Expression) ctx.expression( 0 ).accept( this ),
				(Expression) ctx.expression( 0 ).accept( this )
		);
	}

	@Override
	public Object visitModuloExpression(HqlParser.ModuloExpressionContext ctx) {
		if ( ctx.expression().size() != 2 ) {
			throw new ParsingException( "Expecting 2 operands to the % operator" );
		}
		return new BinaryArithmeticExpression(
				BinaryArithmeticExpression.Operation.MODULO,
				(Expression) ctx.expression( 0 ).accept( this ),
				(Expression) ctx.expression( 0 ).accept( this )
		);
	}

	@Override
	public Object visitUnaryPlusExpression(HqlParser.UnaryPlusExpressionContext ctx) {
		return new UnaryOperationExpression(
				UnaryOperationExpression.Operation.PLUS,
				(Expression) ctx.expression().accept( this )
		);
	}

	@Override
	public Object visitUnaryMinusExpression(HqlParser.UnaryMinusExpressionContext ctx) {
		return new UnaryOperationExpression(
				UnaryOperationExpression.Operation.MINUS,
				(Expression) ctx.expression().accept( this )
		);
	}

	@Override
	@SuppressWarnings("UnnecessaryBoxing")
	public LiteralExpression visitLiteralExpression(HqlParser.LiteralExpressionContext ctx) {
		if ( ctx.literal().CHARACTER_LITERAL() != null ) {
			final String text = ctx.literal().CHARACTER_LITERAL().getText();
			if ( text.length() > 1 ) {
				// todo : or just treat it as a String literal?
				throw new ParsingException( "Value for CHARACTER_LITERAL token was more than 1 character" );
			}
			return new LiteralCharacterExpression( Character.valueOf( text.charAt( 0 ) ) );
		}
		else if ( ctx.literal().STRING_LITERAL() != null ) {
			return new LiteralStringExpression( ctx.literal().STRING_LITERAL().getText() );
		}
		else if ( ctx.literal().INTEGER_LITERAL() != null ) {
			return integerLiteral( ctx.literal().INTEGER_LITERAL().getText() );
		}
		else if ( ctx.literal().LONG_LITERAL() != null ) {
			return longLiteral( ctx.literal().LONG_LITERAL().getText() );
		}
		else if ( ctx.literal().BIG_INTEGER_LITERAL() != null ) {
			return bigIntegerLiteral( ctx.literal().BIG_INTEGER_LITERAL().getText() );
		}
		else if ( ctx.literal().HEX_LITERAL() != null ) {
			final String text = ctx.literal().HEX_LITERAL().getText();
			if ( text.endsWith( "l" ) || text.endsWith( "L" ) ) {
				return longLiteral( text );
			}
			else {
				return integerLiteral( text );
			}
		}
		else if ( ctx.literal().OCTAL_LITERAL() != null ) {
			final String text = ctx.literal().OCTAL_LITERAL().getText();
			if ( text.endsWith( "l" ) || text.endsWith( "L" ) ) {
				return longLiteral( text );
			}
			else {
				return integerLiteral( text );
			}
		}
		else if ( ctx.literal().FLOAT_LITERAL() != null ) {
			return floatLiteral( ctx.literal().FLOAT_LITERAL().getText() );
		}
		else if ( ctx.literal().DOUBLE_LITERAL() != null ) {
			return doubleLiteral( ctx.literal().DOUBLE_LITERAL().getText() );
		}
		else if ( ctx.literal().BIG_DECIMAL_LITERAL() != null ) {
			return bigDecimalLiteral( ctx.literal().BIG_DECIMAL_LITERAL().getText() );
		}
		else if ( ctx.literal().FALSE() != null ) {
			return new LiteralFalseExpression();
		}
		else if ( ctx.literal().TRUE() != null ) {
			return new LiteralTrueExpression();
		}
		else if ( ctx.literal().NULL() != null ) {
			return new LiteralNullExpression();
		}

		// otherwise we have a problem
		throw new ParsingException( "Unexpected literal expression type [" + ctx.getText() + "]" );
	}

	protected LiteralIntegerExpression integerLiteral(String text) {
		try {
			final Integer value = Integer.valueOf( text );
			return new LiteralIntegerExpression( value );
		}
		catch (NumberFormatException e) {
			throw new LiteralNumberFormatException(
					"Unable to convert query literal [" + text + "] to Integer",
					e
			);
		}
	}

	protected LiteralLongExpression longLiteral(String text) {
		final String originalText = text;
		try {
			if ( text.endsWith( "l" ) || text.endsWith( "L" ) ) {
				text = text.substring( 0, text.length() - 1 );
			}
			final Long value = Long.valueOf( text );
			return new LiteralLongExpression( value );
		}
		catch (NumberFormatException e) {
			throw new LiteralNumberFormatException(
					"Unable to convert query literal [" + originalText + "] to Long",
					e
			);
		}
	}

	protected LiteralBigIntegerExpression bigIntegerLiteral(String text) {
		final String originalText = text;
		try {
			if ( text.endsWith( "bi" ) || text.endsWith( "BI" ) ) {
				text = text.substring( 0, text.length() - 2 );
			}
			return new LiteralBigIntegerExpression( new BigInteger( text ) );
		}
		catch (NumberFormatException e) {
			throw new LiteralNumberFormatException(
					"Unable to convert query literal [" + originalText + "] to BigInteger",
					e
			);
		}
	}

	protected LiteralFloatExpression floatLiteral(String text) {
		try {
			return new LiteralFloatExpression( Float.valueOf( text ) );
		}
		catch (NumberFormatException e) {
			throw new LiteralNumberFormatException(
					"Unable to convert query literal [" + text + "] to Float",
					e
			);
		}
	}

	protected LiteralDoubleExpression doubleLiteral(String text) {
		try {
			return new LiteralDoubleExpression( Double.valueOf( text ) );
		}
		catch (NumberFormatException e) {
			throw new LiteralNumberFormatException(
					"Unable to convert query literal [" + text + "] to Double",
					e
			);
		}
	}

	protected LiteralBigDecimalExpression bigDecimalLiteral(String text) {
		final String originalText = text;
		try {
			if ( text.endsWith( "bd" ) || text.endsWith( "BD" ) ) {
				text = text.substring( 0, text.length() - 2 );
			}
			return new LiteralBigDecimalExpression( new BigDecimal( text ) );
		}
		catch (NumberFormatException e) {
			throw new LiteralNumberFormatException(
					"Unable to convert query literal [" + originalText + "] to BigDecimal",
					e
			);
		}
	}

	@Override
	public Object visitParameterExpression(HqlParser.ParameterExpressionContext ctx) {
		return ctx.parameter().accept( this );
	}

	@Override
	public ParameterNamedExpression visitNamedParameter(HqlParser.NamedParameterContext ctx) {
		return new ParameterNamedExpression( ctx.IDENTIFIER().getText() );
	}

	@Override
	public ParameterPositionalExpression visitPositionalParameter(HqlParser.PositionalParameterContext ctx) {
		return new ParameterPositionalExpression( Integer.valueOf( ctx.INTEGER_LITERAL().getText() ) );
	}

	@Override
	public FunctionExpression visitNonStandardFunction(HqlParser.NonStandardFunctionContext ctx) {
		final String functionName = ctx.nonStandardFunctionName().getText();
		final List<Expression> functionArguments = visitNonStandardFunctionArguments( ctx.nonStandardFunctionArguments() );

		// todo : integrate some form of SqlFunction look-up using the ParsingContext so we can resolve the "type"
		return new FunctionExpression( functionName, functionArguments, null );
	}

	@Override
	public List<Expression> visitNonStandardFunctionArguments(HqlParser.NonStandardFunctionArgumentsContext ctx) {
		final List<Expression> arguments = new ArrayList<Expression>();

		for ( HqlParser.ExpressionContext expressionContext : ctx.expression() ) {
			arguments.add( (Expression) expressionContext.accept( this ) );
		}

		return arguments;
	}
}
