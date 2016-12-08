/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.exec.spi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.hibernate.QueryException;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.internal.util.collections.CollectionHelper;
import org.hibernate.query.proposed.spi.QueryParameterBindings;
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
import org.hibernate.sql.ast.expression.domain.ColumnBindingSource;
import org.hibernate.sql.ast.expression.domain.EntityReferenceExpression;
import org.hibernate.sql.ast.expression.domain.PluralAttributeElementReferenceExpression;
import org.hibernate.sql.ast.expression.domain.PluralAttributeIndexReferenceExpression;
import org.hibernate.sql.ast.expression.domain.SingularAttributeReferenceExpression;
import org.hibernate.sql.ast.expression.instantiation.DynamicInstantiation;
import org.hibernate.sql.ast.expression.instantiation.DynamicInstantiationArgument;
import org.hibernate.sql.ast.from.ColumnBinding;
import org.hibernate.sql.ast.from.FromClause;
import org.hibernate.sql.ast.from.TableBinding;
import org.hibernate.sql.ast.from.TableGroup;
import org.hibernate.sql.ast.from.TableGroupJoin;
import org.hibernate.sql.ast.from.TableJoin;
import org.hibernate.sql.ast.from.TableSpace;
import org.hibernate.sql.ast.predicate.BetweenPredicate;
import org.hibernate.sql.ast.predicate.FilterPredicate;
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
import org.hibernate.sql.ast.select.SqlSelectable;
import org.hibernate.sql.ast.select.SqlSelectionDescriptor;
import org.hibernate.sql.convert.expression.internal.DomainReferenceRendererSelectionImpl;
import org.hibernate.sql.convert.expression.internal.DomainReferenceRendererStandardImpl;
import org.hibernate.sql.convert.expression.spi.DomainReferenceRenderer;
import org.hibernate.sql.convert.internal.SqlSelectInterpretationImpl;
import org.hibernate.sql.convert.results.spi.Fetch;
import org.hibernate.sql.convert.results.spi.FetchParent;
import org.hibernate.sql.convert.results.spi.Return;
import org.hibernate.sql.convert.results.spi.ReturnDynamicInstantiation;
import org.hibernate.sql.convert.spi.Helper;
import org.hibernate.sql.NotYetImplementedException;
import org.hibernate.sql.convert.spi.Stack;
import org.hibernate.sql.exec.results.internal.instantiation.ResolvedReturnDynamicInstantiationImpl.ResolvedArgumentImpl;
import org.hibernate.sql.exec.results.process.internal.SqlSelectionDescriptorImpl;
import org.hibernate.sql.exec.results.spi.ResolvedFetch;
import org.hibernate.sql.exec.results.spi.ResolvedFetchParent;
import org.hibernate.sql.exec.results.spi.ResolvedReturn;
import org.hibernate.sql.exec.results.spi.ResolvedReturnDynamicInstantiation;
import org.hibernate.sql.exec.results.spi.ResolvedReturnDynamicInstantiation.ResolvedArgument;
import org.hibernate.sql.spi.ParameterBinder;
import org.hibernate.sqm.parser.ParsingException;
import org.hibernate.sqm.query.from.SqmFrom;
import org.hibernate.type.LiteralType;
import org.hibernate.type.Type;

import org.jboss.logging.Logger;

/**
 * The final phase of query translation.  Here we take the SQL-AST an
 * "interpretation".  For a select query, that means an instance of
 * {@link SqlSelectInterpretation}.
 *
 * @author Steve Ebersole
 */
public class SqlAstSelectInterpreter implements DomainReferenceRenderer.RenderingContext {
	private static final Logger log = Logger.getLogger( SqlAstSelectInterpreter.class );

	/**
	 * Perform interpretation of a select query, returning the SqlSelectInterpretation
	 *
	 * @param selectQuery
	 * @param shallow
	 * @param sessionFactory
	 * @param parameterBindings
	 *
	 * @return The interpretation result
	 */
	public static SqlSelectInterpretation interpret(
			SelectQuery selectQuery,
			boolean shallow,
			SessionFactoryImplementor sessionFactory,
			QueryParameterBindings parameterBindings) {
		final SqlAstSelectInterpreter walker = new SqlAstSelectInterpreter( sessionFactory, parameterBindings, shallow );
		walker.visitSelectQuery( selectQuery );
		return new SqlSelectInterpretationImpl(
				walker.sqlBuffer.toString(),
				walker.parameterBinders,
				walker.returns
		);
	}

	// pre-req state
	private final SessionFactoryImplementor sessionFactory;
	private final QueryParameterBindings parameterBindings;
	private final boolean shallow;

	// In-flight state
	private final StringBuilder sqlBuffer = new StringBuilder();
	private final List<ParameterBinder> parameterBinders = new ArrayList<>();
	private final List<ResolvedReturn> returns = new ArrayList<>();

	private Map<SqmFrom, ColumnBindingSource> columnBindingsSourceMap = new HashMap<>();

	// rendering expressions often has to be done differently if it occurs in certain contexts
	private final Stack<DomainReferenceRenderer> domainReferenceRendererStack = new Stack<>( new DomainReferenceRendererStandardImpl( this ) );
	private final Stack<SqlSelectableProcessor> sqlSelectableCollectorStack = new Stack<>( SqlSelectableProcessorNoOp.INSTANCE );
	private boolean currentlyInPredicate;
	private boolean currentlyInSelections;

	private SqlAstSelectInterpreter(SessionFactoryImplementor sessionFactory, QueryParameterBindings parameterBindings, boolean shallow) {
		this.sessionFactory = sessionFactory;
		this.parameterBindings = parameterBindings;
		this.shallow = shallow;
	}

	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// for now, for tests
	public String getSql() {
		return sqlBuffer.toString();
	}
	public List<ParameterBinder> getParameterBinders() {
		return parameterBinders;
	}
	public List<ResolvedReturn> getReturns() {
		return returns;
	}
	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	private void appendSql(String fragment) {
		sqlBuffer.append( fragment );
	}

	public void visitSelectQuery(SelectQuery selectQuery) {
		visitQuerySpec( selectQuery.getQuerySpec() );

	}

	public void visitQuerySpec(QuerySpec querySpec) {
		visitSelectClause( querySpec.getSelectClause() );
		visitFromClause( querySpec.getFromClause() );

		if ( querySpec.getWhereClauseRestrictions() != null && !querySpec.getWhereClauseRestrictions().isEmpty() ) {
			appendSql( " where " );

			boolean wasPreviouslyInPredicate = currentlyInPredicate;
			currentlyInPredicate = true;
			try {
				querySpec.getWhereClauseRestrictions().accept( this );
			}
			finally {
				currentlyInPredicate = wasPreviouslyInPredicate;
			}
		}
	}

	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// SELECT clause

	public void visitSelectClause(SelectClause selectClause) {
		domainReferenceRendererStack.push( new DomainReferenceRendererSelectionImpl( this, shallow ) );
		sqlSelectableCollectorStack.push( new SqlSelectableProcessorCollecting( this, shallow ) );
		try {
			boolean previouslyInSelections = currentlyInSelections;
			currentlyInSelections = true;

			try {
				appendSql( "select " );
				if ( selectClause.isDistinct() ) {
					appendSql( "distinct " );
				}

				String separator = "";
				for ( Selection selection : selectClause.getSelections() ) {
					appendSql( separator );
					visitSelection( selection );
					separator = ", ";
				}
			}
			finally {
				currentlyInSelections = previouslyInSelections;
			}
		}
		finally {
			sqlSelectableCollectorStack.pop();
			domainReferenceRendererStack.pop();
		}
	}

	public void visitSelection(Selection selection) {
		visitReturn( selection.getQueryReturn() );
	}

	private void visitReturn(Return queryReturn) {
		if ( queryReturn instanceof ReturnDynamicInstantiation ) {
			sqlSelectableCollectorStack.push(
					new SqlSelectableProcessorDynamicInstantiation(
							this,
							( (ReturnDynamicInstantiation) queryReturn ).getSelectExpression()
					)
			);
		}
		queryReturn.getSelectExpression().accept( this, shallow );

		sqlSelectableCollectorStack.getCurrent().processReturn( queryReturn );
	}

	public interface SqlSelectableProcessor {
		void addSelectable(SqlSelectable selectable);
		void addSelectables(SqlSelectable... selectables);

		void processReturn(Return queryReturn);
		void processDynamicInstantiationArgument(DynamicInstantiationArgument argument);
	}

	private static class SqlSelectableProcessorNoOp implements SqlSelectableProcessor {
		/**
		 * Singleton access
		 */
		public static final SqlSelectableProcessorNoOp INSTANCE = new SqlSelectableProcessorNoOp();

		@Override
		public void addSelectable(SqlSelectable selectable) {
			// nothing to do
		}

		@Override
		public void addSelectables(SqlSelectable... selectables) {
			// nothing to do
		}

		@Override
		public void processReturn(Return queryReturn) {
			// should this be an exception?
		}

		@Override
		public void processDynamicInstantiationArgument(DynamicInstantiationArgument argument) {
			// should this be an exception?
		}
	}

	private int numberOfSqlSelectionsConsumedSoFar = 0;

	private static abstract class AbstractSqlSelectableProcessorCollecting implements SqlSelectableProcessor {
		private final SqlAstSelectInterpreter interpreter;

		private List<SqlSelectionDescriptor> selectionDescriptorList;

		AbstractSqlSelectableProcessorCollecting(SqlAstSelectInterpreter interpreter) {
			this.interpreter = interpreter;
		}

		int numberOfSqlSelectionsConsumedSoFarIncrementing() {
			return interpreter.numberOfSqlSelectionsConsumedSoFar++;
		}

		void registerReturn(ResolvedReturn resolvedReturn) {
			interpreter.returns.add( resolvedReturn );
		}

		@Override
		public void addSelectable(SqlSelectable selectable) {
			if ( selectionDescriptorList == null ) {
				selectionDescriptorList = new ArrayList<>();
			}

			selectionDescriptorList.add(
					new SqlSelectionDescriptorImpl( selectable, numberOfSqlSelectionsConsumedSoFarIncrementing() )
			);
		}

		@Override
		public void addSelectables(SqlSelectable... selectables) {
			if ( selectables == null || selectables.length == 0 ) {
				return;
			}

			for ( SqlSelectable selectable : selectables ) {
				addSelectable( selectable );
			}
		}

		List<SqlSelectionDescriptor> makeSelectionDescriptorListCopy() {
			if ( selectionDescriptorList == null ) {
				return Collections.emptyList();
			}

			final List<SqlSelectionDescriptor> copy = CollectionHelper.arrayList( selectionDescriptorList.size() );
			for ( SqlSelectionDescriptor sqlSelectionDescriptor : selectionDescriptorList ) {
				copy.add( sqlSelectionDescriptor );
			}


			if ( selectionDescriptorList != null ) {
				selectionDescriptorList.clear();
			}

			return copy;
		}
	}


	private static class SqlSelectableProcessorCollecting extends AbstractSqlSelectableProcessorCollecting {
		private final boolean shallow;

		SqlSelectableProcessorCollecting(SqlAstSelectInterpreter interpreter, boolean shallow) {
			super( interpreter );
			this.shallow = shallow;
		}

		@Override
		public void processReturn(Return queryReturn) {
			final ResolvedReturn resolvedReturn = queryReturn.resolve(
					makeSelectionDescriptorListCopy(),
					shallow
			);
			applyFetches( resolvedReturn, queryReturn );
			registerReturn( resolvedReturn );
		}

		private void applyFetches(ResolvedReturn resolvedReturn, Return queryReturn) {
			if ( queryReturn instanceof FetchParent ) {
				if ( ! (resolvedReturn instanceof ResolvedFetchParent) ) {
					throw new ParsingException( "Non-matching Return and ResolvedReturn as fetch parent" );
				}
			}
		}

		private void applyFetches(ResolvedFetchParent resolvedReturn, FetchParent queryReturn) {
			for ( Fetch fetch : queryReturn.getFetches() ) {
			// todo : need to build SqlSelectionDescriptor List for the fetch...
				final List<SqlSelectionDescriptor> sqlSelectionDescriptors = null;
				final ResolvedFetch resolvedFetch = resolvedReturn.addFetch(
						sqlSelectionDescriptors,
						shallow,
						fetch
				);
				if ( fetch instanceof FetchParent ) {
					if ( ! (resolvedFetch instanceof ResolvedFetchParent) ) {
						throw new ParsingException( "Non-matching Return and ResolvedReturn as fetch parent" );
					}
					applyFetches( resolvedReturn, queryReturn );
				}
			}
		}

		@Override
		public void processDynamicInstantiationArgument(DynamicInstantiationArgument argument) {
			// should this be an exception?
		}
	}

	private static class SqlSelectableProcessorDynamicInstantiation extends AbstractSqlSelectableProcessorCollecting {
		private final DynamicInstantiation dynamicInstantiation;

		private List<ResolvedArgument> resolvedArguments = new ArrayList<>();

		public SqlSelectableProcessorDynamicInstantiation(SqlAstSelectInterpreter interpreter, DynamicInstantiation dynamicInstantiation) {
			super( interpreter );

			if ( dynamicInstantiation == null ) {
				throw new IllegalArgumentException( "Dynamic-instantiation expression cannot be null" );
			}
			if ( dynamicInstantiation.getTarget() == null ) {
				throw new IllegalArgumentException( "Dynamic-instantiation target cannot be null" );
			}

			this.dynamicInstantiation = dynamicInstantiation;
		}

		@Override
		public void processDynamicInstantiationArgument(DynamicInstantiationArgument argument) {
			ResolvedReturn resolvedArgumentReturn = argument.getExpression().toQueryReturn( argument.getAlias() ).resolve(
					makeSelectionDescriptorListCopy(),
					// shallow
					true
			);
			add( new ResolvedArgumentImpl( resolvedArgumentReturn, argument.getAlias() ) );
		}

		void add(ResolvedArgument resolvedArgument) {
			resolvedArguments.add( resolvedArgument );
		}

		@Override
		public void processReturn(Return queryReturn) {
			final ResolvedReturnDynamicInstantiation resolvedReturn = (ResolvedReturnDynamicInstantiation) queryReturn.resolve(
					Collections.emptyList(),
					true
			);

			resolvedReturn.setArguments( resolvedArguments );
			registerReturn( resolvedReturn );
		}
	}

	private static class SqlSelectableProcessorDynamicInstantiationArgument extends AbstractSqlSelectableProcessorCollecting {
		private final SqlSelectableProcessorDynamicInstantiation parent;

		public SqlSelectableProcessorDynamicInstantiationArgument(SqlAstSelectInterpreter interpreter, SqlSelectableProcessor parent) {
			super( interpreter );

			if ( !SqlSelectableProcessorDynamicInstantiation.class.isInstance( parent ) ) {
				throw new IllegalStateException( "Unexpected parent SqlSelectableProcessor type" );
			}
			this.parent = (SqlSelectableProcessorDynamicInstantiation) parent;
		}

		@Override
		public void processDynamicInstantiationArgument(DynamicInstantiationArgument argument) {
			ResolvedReturn resolvedArgumentReturn = argument.getExpression().toQueryReturn( argument.getAlias() ).resolve(
					makeSelectionDescriptorListCopy(),
					// shallow
					true
			);

			parent.add( new ResolvedArgumentImpl( resolvedArgumentReturn, argument.getAlias() ) );
		}

		@Override
		public void processReturn(Return queryReturn) {
			// should not be called
			throw new IllegalStateException( "should not be called" );
		}
	}


	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// FROM clause

	public void visitFromClause(FromClause fromClause) {
		appendSql( " from " );

		String separator = "";
		for ( TableSpace tableSpace : fromClause.getTableSpaces() ) {
			appendSql( separator );
			visitTableSpace( tableSpace );
			separator = ", ";
		}
	}

	public void visitTableSpace(TableSpace tableSpace) {
		visitTableGroup( tableSpace.getRootTableGroup() );

		for ( TableGroupJoin tableGroupJoin : tableSpace.getJoinedTableGroups() ) {
			appendSql( " " );
			appendSql( tableGroupJoin.getJoinType().getText() );
			appendSql( " join " );
			visitTableGroup( tableGroupJoin.getJoinedGroup() );

			boolean wasPreviouslyInPredicate = currentlyInPredicate;
			currentlyInPredicate = true;
			try {
				if ( tableGroupJoin.getPredicate() != null && !tableGroupJoin.getPredicate().isEmpty() ) {
					appendSql( " on " );
					tableGroupJoin.getPredicate().accept( this );
				}
			}
			finally {
				currentlyInPredicate = wasPreviouslyInPredicate;
			}
		}

	}

	public void visitTableGroup(TableGroup tableGroup) {
		visitTableBinding( tableGroup.getRootTableBinding() );

		for ( TableJoin tableJoin : tableGroup.getTableJoins() ) {
			appendSql( " " );
			appendSql( tableJoin.getJoinType().getText() );
			appendSql( " join " );
			visitTableBinding( tableJoin.getJoinedTableBinding() );
			if ( tableJoin.getJoinPredicate() != null && !tableJoin.getJoinPredicate().isEmpty() ) {
				appendSql( " on " );
				tableJoin.getJoinPredicate().accept( this );
			}
		}
	}

	public void visitTableBinding(TableBinding tableBinding) {
		appendSql( tableBinding.getTable().getTableExpression() + " as " + tableBinding.getIdentificationVariable() );
	}


	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Expressions

	public void visitSingularAttributeReference(SingularAttributeReferenceExpression attributeExpression,
																			 boolean shallow) {
		// todo : this needs to operate differently in different contexts (mainly for associations)
		//		e.g...
		//			1) In the select clause we should render the complete column bindings for associations
		//			2) In join predicates
		domainReferenceRendererStack.getCurrent().render( attributeExpression, shallow );
	}

	public void visitEntityExpression(
			EntityReferenceExpression entityExpression,
			boolean shallow) {
		domainReferenceRendererStack.getCurrent().render( entityExpression, shallow );
	}

	public void visitPluralAttributeElement(
			PluralAttributeElementReferenceExpression elementExpression,
			boolean shallow) {
		domainReferenceRendererStack.getCurrent().render( elementExpression, shallow );

	}

	public void visitPluralAttributeIndex(
			PluralAttributeIndexReferenceExpression indexExpression,
			boolean shallow) {
		domainReferenceRendererStack.getCurrent().render( indexExpression, shallow );
	}

	private void visitColumnBinding(ColumnBinding columnBinding) {
		appendSql( columnBinding.getColumn().render( columnBinding.getIdentificationVariable() ) );
	}

	public void visitAvgFunction(AvgFunction avgFunction) {
		sqlSelectableCollectorStack.push( SqlSelectableProcessorNoOp.INSTANCE );
		try {
			appendSql( "avg(" );
			avgFunction.getArgument().accept( this, true );
			appendSql( ")" );
		}
		finally {
			sqlSelectableCollectorStack.pop();
		}

		sqlSelectableCollectorStack.getCurrent().addSelectable( avgFunction );

	}

	public void visitBinaryArithmeticExpression(BinaryArithmeticExpression arithmeticExpression) {
		arithmeticExpression.getLeftHandOperand().accept( this, true );
		appendSql( arithmeticExpression.getOperation().getOperatorSqlText() );
		arithmeticExpression.getRightHandOperand().accept( this, true );
	}

	public void visitCaseSearchedExpression(CaseSearchedExpression caseSearchedExpression) {
		sqlSelectableCollectorStack.push( SqlSelectableProcessorNoOp.INSTANCE );
		try {
			appendSql( "case " );
			for ( CaseSearchedExpression.WhenFragment whenFragment : caseSearchedExpression.getWhenFragments() ) {
				appendSql( " when " );
				whenFragment.getPredicate().accept( this );
				appendSql( " then " );
				whenFragment.getResult().accept( this, true );
			}
			appendSql( " else " );

			caseSearchedExpression.getOtherwise().accept( this, true );
			appendSql( " end" );
		}
		finally {
			sqlSelectableCollectorStack.pop();
		}

		sqlSelectableCollectorStack.getCurrent().addSelectable( caseSearchedExpression );
	}

	public void visitCaseSimpleExpression(CaseSimpleExpression caseSimpleExpression) {
		sqlSelectableCollectorStack.push( SqlSelectableProcessorNoOp.INSTANCE );
		try {
			appendSql( "case " );
			caseSimpleExpression.getFixture().accept( this, true );
			for ( CaseSimpleExpression.WhenFragment whenFragment : caseSimpleExpression.getWhenFragments() ) {
				appendSql( " when " );
				whenFragment.getCheckValue().accept( this, true );
				appendSql( " then " );
				whenFragment.getResult().accept( this, true );
			}
			appendSql( " else " );

			caseSimpleExpression.getOtherwise().accept( this, true );
			appendSql( " end" );
		}
		finally {
			sqlSelectableCollectorStack.pop();
		}

		sqlSelectableCollectorStack.getCurrent().addSelectable( caseSimpleExpression );
	}

	public void visitColumnBindingExpression(ColumnBindingExpression columnBindingExpression) {
		renderColumnBinding( columnBindingExpression.getColumnBinding() );
	}

	public void visitCoalesceExpression(CoalesceExpression coalesceExpression) {
		sqlSelectableCollectorStack.push( SqlSelectableProcessorNoOp.INSTANCE );
		try {
			appendSql( "coalesce(" );
			String separator = "";
			for ( Expression expression : coalesceExpression.getValues() ) {
				appendSql( separator );
				expression.accept( this, true );
				separator = ", ";
			}

			appendSql( ")" );
		}
		finally {
			sqlSelectableCollectorStack.pop();
		}

		sqlSelectableCollectorStack.getCurrent().addSelectable( coalesceExpression );
	}

	public void visitConcatExpression(ConcatExpression concatExpression) {
		sqlSelectableCollectorStack.push( SqlSelectableProcessorNoOp.INSTANCE );
		try {
			appendSql( "concat(" );
			concatExpression.getLeftHandOperand().accept( this, true );
			appendSql( "," );
			concatExpression.getRightHandOperand().accept( this, true );
			appendSql( ")" );
		}
		finally {
			sqlSelectableCollectorStack.pop();
		}

		sqlSelectableCollectorStack.getCurrent().addSelectable( concatExpression );
	}

	public void visitCountFunction(CountFunction countFunction) {
		sqlSelectableCollectorStack.push( SqlSelectableProcessorNoOp.INSTANCE );
		try {
			appendSql( "count(" );
			if ( countFunction.isDistinct() ) {
				appendSql( "distinct " );
			}
			countFunction.getArgument().accept( this, true );
			appendSql( ")" );
		}
		finally {
			sqlSelectableCollectorStack.pop();
		}

		sqlSelectableCollectorStack.getCurrent().addSelectable( countFunction );
	}

	public void visitCountStarFunction(CountStarFunction function) {
		sqlSelectableCollectorStack.push( SqlSelectableProcessorNoOp.INSTANCE );
		try {
			appendSql( "count(" );
			if ( function.isDistinct() ) {
				appendSql( "distinct " );
			}
			appendSql( "*)" );
		}
		finally {
			sqlSelectableCollectorStack.pop();
		}

		sqlSelectableCollectorStack.getCurrent().addSelectable( function );
	}

	public void visitDynamicInstantiation(DynamicInstantiation<?> dynamicInstantiation) {
		sqlSelectableCollectorStack.push( new SqlSelectableProcessorDynamicInstantiationArgument( this, sqlSelectableCollectorStack.getCurrent() ) );

		try {
			// this is highly optimistic in thinking that each argument expression renders values to the select, but for now...
			String separator = "";
			for ( DynamicInstantiationArgument argument : dynamicInstantiation.getArguments() ) {
				appendSql( separator );

				argument.getExpression().accept( this, true );
				sqlSelectableCollectorStack.getCurrent().processDynamicInstantiationArgument( argument );
				separator = ", ";
			}
		}
		finally {
			sqlSelectableCollectorStack.pop();
		}
	}

	public void visitMaxFunction(MaxFunction maxFunction) {
		sqlSelectableCollectorStack.push( SqlSelectableProcessorNoOp.INSTANCE );
		try {
			appendSql( "max(" );
			if ( maxFunction.isDistinct() ) {
				appendSql( "distinct " );
			}
			maxFunction.getArgument().accept( this, true );
			appendSql( ")" );
		}
		finally {
			sqlSelectableCollectorStack.pop();
		}

		sqlSelectableCollectorStack.getCurrent().addSelectable( maxFunction );
	}

	public void visitMinFunction(MinFunction minFunction) {
		sqlSelectableCollectorStack.push( SqlSelectableProcessorNoOp.INSTANCE );
		try {
			appendSql( "min(" );
			if ( minFunction.isDistinct() ) {
				appendSql( "distinct " );
			}
			minFunction.getArgument().accept( this, true );
			appendSql( ")" );
		}
		finally {
			sqlSelectableCollectorStack.pop();
		}

		sqlSelectableCollectorStack.getCurrent().addSelectable( minFunction );
	}

	public void visitNamedParameter(NamedParameter namedParameter) {
		parameterBinders.add( namedParameter.getParameterBinder() );

		final Type type = Helper.resolveType( namedParameter, parameterBindings );

		final int columnCount = type.getColumnSpan( sessionFactory );
		final boolean needsParens = currentlyInPredicate && columnCount > 1;

		if ( needsParens ) {
			appendSql( "(" );
		}

		String separator = "";
		for ( int i = 0; i < columnCount; i++ ) {
			appendSql( separator );
			appendSql( "?" );
			separator = ", ";
		}

		if ( needsParens ) {
			appendSql( ")" );
		}

		sqlSelectableCollectorStack.getCurrent().addSelectable( namedParameter );
	}

	public void visitNonStandardFunctionExpression(NonStandardFunctionExpression functionExpression) {
		sqlSelectableCollectorStack.push( SqlSelectableProcessorNoOp.INSTANCE );
		try {
			// todo : look up function registry entry (maybe even when building the SQL tree)
			appendSql( functionExpression.getFunctionName() );
			if ( !functionExpression.getArguments().isEmpty() ) {
				appendSql( "(" );
				String separator = "";
				for ( Expression argumentExpression : functionExpression.getArguments() ) {
					appendSql( separator );
					argumentExpression.accept( this, true );
					separator = ", ";
				}
				appendSql( ")" );
			}
		}
		finally {
			sqlSelectableCollectorStack.pop();
		}

		sqlSelectableCollectorStack.getCurrent().addSelectable( functionExpression );
	}

	public void visitNullifExpression(NullifExpression nullifExpression) {
		sqlSelectableCollectorStack.push( SqlSelectableProcessorNoOp.INSTANCE );
		try {
			appendSql( "nullif(" );
			nullifExpression.getFirstArgument().accept( this, true );
			appendSql( ", " );
			nullifExpression.getSecondArgument().accept( this, true );
			appendSql( ")" );
		}
		finally {
			sqlSelectableCollectorStack.pop();
		}

		sqlSelectableCollectorStack.getCurrent().addSelectable( nullifExpression );
	}

	public void visitPositionalParameter(PositionalParameter positionalParameter) {
		parameterBinders.add( positionalParameter.getParameterBinder() );

		final Type type = Helper.resolveType( positionalParameter, parameterBindings );

		final int columnCount = type.getColumnSpan( sessionFactory );
		final boolean needsParens = currentlyInPredicate && columnCount > 1;

		if ( needsParens ) {
			appendSql( "(" );
		}

		String separator = "";
		for ( int i = 0; i < columnCount; i++ ) {
			appendSql( separator );
			appendSql( "?" );
			separator = ", ";
		}

		if ( needsParens ) {
			appendSql( ")" );
		}

		sqlSelectableCollectorStack.getCurrent().addSelectable( positionalParameter );
	}

	public void visitQueryLiteral(QueryLiteral queryLiteral) {
		if ( !currentlyInSelections ) {
			// handle literals via parameter binding if they occur outside the select
			parameterBinders.add( queryLiteral );

			final int columnCount = queryLiteral.getType().getColumnSpan( sessionFactory );
			final boolean needsParens = currentlyInPredicate && columnCount > 1;

			if ( needsParens ) {
				appendSql( "(" );
			}

			String separator = "";
			for ( int i = 0; i < columnCount; i++ ) {
				appendSql( separator );
				appendSql( "?" );
				separator = ", ";
			}

			if ( needsParens ) {
				appendSql( ")" );
			}
		}
		else {
			// otherwise, render them as literals
			// todo : better scheme for rendering these as literals
			try {
				appendSql(
						( (LiteralType) queryLiteral.getType() ).objectToSQLString( queryLiteral.getValue(), sessionFactory.getDialect() )
				);
			}
			catch (Exception e) {
				throw new QueryException(
						String.format(
								Locale.ROOT,
								"Could not render literal value [%s (%s)] into SQL",
								queryLiteral.getValue(),
								queryLiteral.getType().getName()
						),
						e
				);
			}
		}

		sqlSelectableCollectorStack.getCurrent().addSelectable( queryLiteral );
	}

	public void visitSumFunction(SumFunction sumFunction) {
		sqlSelectableCollectorStack.push( SqlSelectableProcessorNoOp.INSTANCE );
		try {
			appendSql( "sum(" );
			if ( sumFunction.isDistinct() ) {
				appendSql( "distinct " );
			}
			sumFunction.getArgument().accept( this, true );
			appendSql( ")" );
		}
		finally {
			sqlSelectableCollectorStack.pop();
		}

		sqlSelectableCollectorStack.getCurrent().addSelectable( sumFunction );
	}

	public void visitUnaryOperationExpression(UnaryOperationExpression unaryOperationExpression) {
		sqlSelectableCollectorStack.push( SqlSelectableProcessorNoOp.INSTANCE );
		try {
			if ( unaryOperationExpression.getOperation() == UnaryOperationExpression.Operation.PLUS ) {
				appendSql( "+" );
			}
			else {
				appendSql( "-" );
			}
			unaryOperationExpression.getOperand().accept( this, true );
		}
		finally {
			sqlSelectableCollectorStack.pop();
		}

		sqlSelectableCollectorStack.getCurrent().addSelectable( unaryOperationExpression );
	}


	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Predicates

	public void visitBetweenPredicate(BetweenPredicate betweenPredicate) {
		betweenPredicate.getExpression().accept( this, true );
		if ( betweenPredicate.isNegated() ) {
			appendSql( " not" );
		}
		appendSql( " between " );
		betweenPredicate.getLowerBound().accept( this, true );
		appendSql( " and " );
		betweenPredicate.getUpperBound().accept( this, true );
	}

	public void visitFilterPredicate(FilterPredicate filterPredicate) {
		throw new NotYetImplementedException();
	}

	public void visitGroupedPredicate(GroupedPredicate groupedPredicate) {
		if ( groupedPredicate.isEmpty() ) {
			return;
		}

		appendSql( "(" );
		groupedPredicate.getSubPredicate().accept( this );
		appendSql( ")" );
	}

	public void visitInListPredicate(InListPredicate inListPredicate) {
		inListPredicate.getTestExpression().accept( this, true );
		if ( inListPredicate.isNegated() ) {
			appendSql( " not" );
		}
		appendSql( " in(" );
		if ( inListPredicate.getListExpressions().isEmpty() ) {
			appendSql( "null" );
		}
		else {
			String separator = "";
			for ( Expression expression : inListPredicate.getListExpressions() ) {
				appendSql( separator );
				expression.accept( this, true );
				separator = ", ";
			}
		}
		appendSql( ")" );
	}

	public void visitInSubQueryPredicate(InSubQueryPredicate inSubQueryPredicate) {
		inSubQueryPredicate.getTestExpression().accept( this, true );
		if ( inSubQueryPredicate.isNegated() ) {
			appendSql( " not" );
		}
		appendSql( " in(" );
		visitQuerySpec( inSubQueryPredicate.getSubQuery() );
		appendSql( ")" );
	}

	public void visitJunction(Junction junction) {
		if ( junction.isEmpty() ) {
			return;
		}

		String separator = "";
		for ( Predicate predicate : junction.getPredicates() ) {
			appendSql( separator );
			predicate.accept( this );
			separator = junction.getNature() == Junction.Nature.CONJUNCTION ? " and " : " or ";
		}
	}

	public void visitLikePredicate(LikePredicate likePredicate) {
		likePredicate.getMatchExpression().accept( this, true );
		if ( likePredicate.isNegated() ) {
			appendSql( " not" );
		}
		appendSql( " like " );
		likePredicate.getPattern().accept( this, true );
		if ( likePredicate.getEscapeCharacter() != null ) {
			appendSql( " escape " );
			likePredicate.getEscapeCharacter().accept( this, true );
		}
	}

	public void visitNegatedPredicate(NegatedPredicate negatedPredicate) {
		if ( negatedPredicate.isEmpty() ) {
			return;
		}

		appendSql( "not(" );
		negatedPredicate.getPredicate().accept( this );
		appendSql( ")" );
	}

	public void visitNullnessPredicate(NullnessPredicate nullnessPredicate) {
		nullnessPredicate.getExpression().accept( this, true );
		if ( nullnessPredicate.isNegated() ) {
			appendSql( " is not null" );
		}
		else {
			appendSql( " is null" );
		}
	}

	public void visitRelationalPredicate(RelationalPredicate relationalPredicate) {
		relationalPredicate.getLeftHandExpression().accept( this, true );
		appendSql( relationalPredicate.getOperator().sqlText() );
		relationalPredicate.getRightHandExpression().accept( this, true );
	}



	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// DomainReferenceRenderer.RenderingContext impl


	@Override
	public SessionFactoryImplementor getSessionFactory() {
		return sessionFactory;
	}

	@Override
	public void renderColumnBinding(ColumnBinding binding) {
		sqlSelectableCollectorStack.getCurrent().addSelectables( binding );
		appendSql( binding.getColumn().render( binding.getIdentificationVariable() ) );
	}

	@Override
	public void renderColumnBindings(List<ColumnBinding> bindings) {
		final boolean needsParens = bindings.size() > 1 && currentlyInPredicate;
		if ( needsParens ) {
			appendSql( "(" );
		}

		String separator = "";
		for ( ColumnBinding columnBinding : bindings ) {
			appendSql( separator );
			renderColumnBinding( columnBinding );
			separator = ", ";
		}

		if ( needsParens ) {
			appendSql( ")" );
		}
	}

	@Override
	public void renderColumnBindings(ColumnBinding... bindings) {
		final boolean needsParens = bindings.length > 1 && currentlyInPredicate;
		if ( needsParens ) {
			appendSql( "(" );
		}

		String separator = "";
		for ( ColumnBinding columnBinding : bindings ) {
			appendSql( separator );
			renderColumnBinding( columnBinding );
			separator = ", ";
		}

		if ( needsParens ) {
			appendSql( ")" );
		}
	}
}
