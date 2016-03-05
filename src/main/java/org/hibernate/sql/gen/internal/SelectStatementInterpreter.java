package org.hibernate.sql.gen.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.hibernate.AssertionFailure;
import org.hibernate.loader.plan.spi.Return;
import org.hibernate.sql.ast.SelectQuery;
import org.hibernate.sql.ast.from.ColumnBinding;
import org.hibernate.sql.ast.from.EntityTableGroup;
import org.hibernate.sql.ast.from.TableGroup;
import org.hibernate.sql.ast.from.TableGroupJoin;
import org.hibernate.sql.ast.from.TableSpace;
import org.hibernate.sql.ast.predicate.Predicate;
import org.hibernate.sql.gen.Callback;
import org.hibernate.sql.gen.JdbcSelectPlan;
import org.hibernate.sql.gen.NotYetImplementedException;
import org.hibernate.sql.gen.ParameterBinder;
import org.hibernate.sql.gen.QueryOptionBinder;
import org.hibernate.sql.orm.QueryOptions;
import org.hibernate.sql.orm.internal.mapping.ImprovedCollectionPersister;
import org.hibernate.sql.orm.internal.mapping.ImprovedEntityPersister;
import org.hibernate.sql.orm.internal.mapping.SingularAttributeBasic;
import org.hibernate.sql.orm.internal.mapping.SingularAttributeEntity;
import org.hibernate.sql.orm.internal.mapping.Column;
import org.hibernate.sqm.BaseSemanticQueryWalker;
import org.hibernate.sqm.domain.PluralAttribute;
import org.hibernate.sqm.domain.SingularAttribute;
import org.hibernate.sqm.query.DeleteStatement;
import org.hibernate.sqm.query.InsertSelectStatement;
import org.hibernate.sqm.query.QuerySpec;
import org.hibernate.sqm.query.SelectStatement;
import org.hibernate.sqm.query.UpdateStatement;
import org.hibernate.sqm.query.expression.AttributeReferenceExpression;
import org.hibernate.sqm.query.from.CrossJoinedFromElement;
import org.hibernate.sqm.query.from.FromClause;
import org.hibernate.sqm.query.from.FromElement;
import org.hibernate.sqm.query.from.FromElementSpace;
import org.hibernate.sqm.query.from.JoinedFromElement;
import org.hibernate.sqm.query.from.QualifiedAttributeJoinFromElement;
import org.hibernate.sqm.query.from.QualifiedEntityJoinFromElement;
import org.hibernate.sqm.query.from.RootEntityFromElement;
import org.hibernate.sqm.query.order.OrderByClause;
import org.hibernate.sqm.query.order.SortSpecification;
import org.hibernate.sqm.query.predicate.WhereClause;
import org.hibernate.sqm.query.select.SelectClause;

/**
 * @author Steve Ebersole
 * @author John O'Hara
 */
public class SelectStatementInterpreter extends BaseSemanticQueryWalker {

	public static JdbcSelectPlan interpret(SelectStatement statement, QueryOptions queryOptions, Callback callback) {
		final SelectStatementInterpreter walker = new SelectStatementInterpreter( queryOptions, callback );
		return walker.interpret( statement );
	}

	protected JdbcSelectPlan interpret(SelectStatement statement) {
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

	protected SelectStatementInterpreter(QueryOptions queryOptions, Callback callback) {
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

	@Override
	public org.hibernate.sql.ast.QuerySpec visitQuerySpec(QuerySpec querySpec) {
		final org.hibernate.sql.ast.QuerySpec astQuerySpec = new org.hibernate.sql.ast.QuerySpec();

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
				tableSpace.addJoinedTableSpecificationGroup( (TableGroupJoin) joinedFromElement.accept(
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
		final ImprovedEntityPersister entityPersister = (ImprovedEntityPersister) rootEntityFromElement.getBoundModelType();
		if ( fromClauseIndex.isResolved( rootEntityFromElement ) ) {
			return null;
		}

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
		final ImprovedEntityPersister entityPersister = (ImprovedEntityPersister) joinedFromElement.getIntrinsicSubclassIndicator();
		TableGroup group = null;

		if ( joinedFromElement.getBoundAttribute() instanceof PluralAttribute ) {
			final ImprovedCollectionPersister improvedCollectionPersister = (ImprovedCollectionPersister) joinedFromElement.getBoundAttribute();
			group = improvedCollectionPersister.buildTableGroup(
					joinedFromElement,
					tableSpace,
					sqlAliasBaseManager,
					fromClauseIndex
			);
		}
		else {
			group = entityPersister.buildTableGroup(
					joinedFromElement,
					tableSpace,
					sqlAliasBaseManager,
					fromClauseIndex
			);
		}
		// todo : determine the Predicate
		final Predicate predicate = new Predicate() {
		};
		return new TableGroupJoin( joinedFromElement.getJoinType(), group, predicate );
	}

	@Override
	public Object visitCrossJoinedFromElement(CrossJoinedFromElement joinedFromElement) {

		final ImprovedEntityPersister entityPersister = (ImprovedEntityPersister) joinedFromElement.getIntrinsicSubclassIndicator();
		TableGroup group = null;

		group = entityPersister.buildTableGroup(
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
	public Object visitAttributeReferenceExpression(AttributeReferenceExpression expression) {
		// WARNING : lots of faulty assumptions just to get a basic example running
		// todo : fixme

		final FromElement fromElement = expression.getAttributeBindingSource().getFromElement();
		final TableGroup tableGroup = fromClauseIndex.findResolvedTableGroup( fromElement );

		final SingularAttribute attribute = (SingularAttribute) expression.getBoundAttribute();

		final ColumnBinding[] columnBindings = tableGroup.resolveAttribute( attribute );

		return null;
	}
}
