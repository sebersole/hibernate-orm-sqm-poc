package org.hibernate.sql.gen.internal;

import org.hibernate.loader.plan.spi.LoadPlan;
import org.hibernate.sql.gen.JdbcSelectPlan;
import org.hibernate.sql.gen.NotYetImplementedException;

/**
 * Created by John O'Hara on 27/08/15.
 */
public class JdbcSelectPlanImpl extends JdbcOperationPlanImpl implements JdbcSelectPlan {
//
//	private LoadPlan loadPlan;
//
//	public JdbcSelectPlanImpl(SelectStatement selectStatement) {
//		super( selectStatement );
//
//		//populate SqlPlan from selectstatment
//		generateSqlPlan();
//	}

	@Override
	public LoadPlan getLoadPlan() {
		throw new NotYetImplementedException();
	}
//
//	@Override
//	public void generateSqlPlan() {
//		super.generateSqlPlan();
////		TODO: generate LoadPlan
//	}
//


}
