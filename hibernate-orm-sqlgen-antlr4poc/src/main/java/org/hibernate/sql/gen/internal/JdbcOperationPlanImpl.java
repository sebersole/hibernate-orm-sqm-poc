package org.hibernate.sql.gen.internal;

import org.hibernate.sql.gen.JdbcOperationPlan;
import org.hibernate.sql.gen.NotYetImplementedException;
import org.hibernate.sql.gen.ParameterBinder;

import java.util.List;

/**
 * @author John O'Hara
 */
public class JdbcOperationPlanImpl implements JdbcOperationPlan {
	@Override
	public String getSql() {
		throw new NotYetImplementedException();
	}

	@Override
	public List<ParameterBinder> getParameterBinders() {
		throw new NotYetImplementedException();
	}
//
//	public void generateSqlPlan() {
//
//		StringBuilder sqlBuilder = new StringBuilder(  );
//
//		sqlBuilder.append( generateSelectClause() );
//
//		sqlBuilder.append( generateFromClause());
//
//		if(selectStatement.getQuerySpec().getWhereClause() != null){
//
//			sqlBuilder.append( generateWhereClause());
//		}
//
//		sqlStatments.add( sqlBuilder.toString() );
//
//	}
//
//	private String generateSelectClause() {
//		StringBuilder selectStatementBuilder = new StringBuilder();
//
////		TODO: remove hard coded literal
//		selectStatementBuilder.append( "select  ");
//
//		for (Selection selection : selectStatement.getQuerySpec().getSelectClause().getSelections()) {
//			selectStatementBuilder.append( selection.getExpression().getTypeDescriptor().getTypeName() );
//		}
//		return selectStatementBuilder.toString();
//
//	}
//
//	private String generateFromClause() {
//
//		StringBuilder fromClauseBuilder = new StringBuilder();
//
////		TODO: remove hard coded literal
//		fromClauseBuilder.append( " from " );
//
//		for (FromElementSpace fromElementSpace : selectStatement.getQuerySpec().getFromClause().getFromElementSpaces()) {
////			TODO: map to table name
//			fromClauseBuilder.append( fromElementSpace.getRoot().getEntityName());
//		}
//
//		return fromClauseBuilder.toString();
//
//	}
//
//	private String generateWhereClause() {
//		StringBuilder wherelauseBuilder = new StringBuilder();
//
////		TODO: remove hard coded literal
//		wherelauseBuilder.append( " where " );
//
//		wherelauseBuilder.append( PredicateGenerator.generatePredicateClause( selectStatement.getQuerySpec().getWhereClause().getPredicate() ) );
//
//		return wherelauseBuilder.toString();
//	}
}
