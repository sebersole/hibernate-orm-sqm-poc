package org.hibernate.sql.gen.internal;

import org.hibernate.loader.plan.spi.LoadPlan;
import org.hibernate.sql.gen.SelectSqlPlan;
import org.hibernate.sqm.query.SelectStatement;

/**
 * Created by johara on 27/08/15.
 */
public class SelectSqlPlanImpl extends AbstractSqlPlan implements SelectSqlPlan {

	private LoadPlan loadPlan;

	public SelectSqlPlanImpl(SelectStatement selectStatement) {
		super( selectStatement );

		//populate SqlPlan from selectstatment
		generateSqlPlan();
	}

	@Override
	public LoadPlan getLoadPlan() {
		return  this.loadPlan;
	}

	@Override
	public void generateSqlPlan() {
		super.generateSqlPlan();
//		TODO: generate LoadPlan
	}



}
