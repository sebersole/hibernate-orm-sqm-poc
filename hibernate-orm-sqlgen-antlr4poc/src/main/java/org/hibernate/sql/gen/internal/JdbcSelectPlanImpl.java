package org.hibernate.sql.gen.internal;

import org.hibernate.loader.plan.build.internal.LoadPlanImpl;
import org.hibernate.loader.plan.spi.LoadPlan;
import org.hibernate.loader.plan.spi.QuerySpaces;
import org.hibernate.loader.plan.spi.Return;
import org.hibernate.sql.gen.JdbcSelectPlan;
import org.hibernate.sqm.query.SelectStatement;

import java.util.List;

/**
 * @author John O'Hara
 */
public class JdbcSelectPlanImpl extends JdbcOperationPlanImpl implements JdbcSelectPlan {

	private SelectStatement selectStatement;
	private SqlGeneratorSemanticQueryWalker queryWalker;

	public JdbcSelectPlanImpl(SelectStatement statement) {
		super();
		this.selectStatement = statement;
		queryWalker = new SqlGeneratorSemanticQueryWalker();

	}

	@Override
	public String getSql() {
		visitWalker();

		StringBuilder sqlBuilder = new StringBuilder(  );

		sqlBuilder.append( queryWalker.getSelectString() );
		sqlBuilder.append( queryWalker.getFromString());
		sqlBuilder.append( queryWalker.getWhereString());
		sqlBuilder.append( ";" );

		return sqlBuilder.toString();
	}

	@Override
	public LoadPlan getLoadPlan() {

		visitWalker();

		List<? extends Return> returns = queryWalker.getReturns();
		QuerySpaces querySpaces = queryWalker.getQuerySpaces();
		boolean areLazyAttributesForceFetched = queryWalker.areLazyAttributesForceFetched();

		LoadPlan loadPlan = new LoadPlanImpl(returns, querySpaces, areLazyAttributesForceFetched );

		return loadPlan;
	}

	private void visitWalker(){
		queryWalker.visitSelectStatement( this.selectStatement );
	}
}
