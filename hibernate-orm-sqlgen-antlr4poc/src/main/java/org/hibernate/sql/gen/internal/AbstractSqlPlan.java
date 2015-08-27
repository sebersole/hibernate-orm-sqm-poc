package org.hibernate.sql.gen.internal;

import org.hibernate.sql.gen.NotYetImplementedException;
import org.hibernate.sql.gen.ParameterBinding;
import org.hibernate.sql.gen.SqlPlan;
import org.hibernate.sql.gen.internal.predicate.PredicateGenerator;
import org.hibernate.sqm.query.SelectStatement;
import org.hibernate.sqm.query.from.FromElementSpace;
import org.hibernate.sqm.query.select.Selection;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by johara on 27/08/15.
 */
public class AbstractSqlPlan implements SqlPlan {

	private final SelectStatement selectStatement;
	private List<String> sqlStatments;
	private Set<ParameterBinding> parameterBindingSet;


	protected AbstractSqlPlan(SelectStatement selectStatement) {
		this.selectStatement = selectStatement;
		sqlStatments = new ArrayList<String>();
	}

	@Override
	public List<String> getSqlStatements() {
		throw new NotYetImplementedException();
	}

	@Override
	public Set<ParameterBinding> getParameterBindings() {
		throw new NotYetImplementedException();
	}

	public List<String> getSqlStatments() {
		return sqlStatments;
	}

	public Set<ParameterBinding> getParameterBindingSet() {
		return parameterBindingSet;
	}

	public void generateSqlPlan() {

		StringBuilder sqlBuilder = new StringBuilder(  );

		sqlBuilder.append( generateSelectClause() );

		sqlBuilder.append( generateFromClause());

		if(selectStatement.getQuerySpec().getWhereClause() != null){

			sqlBuilder.append( generateWhereClause());
		}

		sqlStatments.add( sqlBuilder.toString() );

	}

	private String generateSelectClause() {
		StringBuilder selectStatementBuilder = new StringBuilder();

//		TODO: remove hard coded literal
		selectStatementBuilder.append( "select  ");

		for (Selection selection : selectStatement.getQuerySpec().getSelectClause().getSelections()) {
			selectStatementBuilder.append( selection.getExpression().getTypeDescriptor().getTypeName() );
		}
		return selectStatementBuilder.toString();

	}

	private String generateFromClause() {

		StringBuilder fromClauseBuilder = new StringBuilder();

//		TODO: remove hard coded literal
		fromClauseBuilder.append( " from " );

		for (FromElementSpace fromElementSpace : selectStatement.getQuerySpec().getFromClause().getFromElementSpaces()) {
//			TODO: map to table name
			fromClauseBuilder.append( fromElementSpace.getRoot().getEntityName());
		}

		return fromClauseBuilder.toString();

	}

	private String generateWhereClause() {
		StringBuilder wherelauseBuilder = new StringBuilder();

//		TODO: remove hard coded literal
		wherelauseBuilder.append( " where " );

		wherelauseBuilder.append( PredicateGenerator.generatePredicateClause( selectStatement.getQuerySpec().getWhereClause().getPredicate() ) );

		return wherelauseBuilder.toString();
	}
}
