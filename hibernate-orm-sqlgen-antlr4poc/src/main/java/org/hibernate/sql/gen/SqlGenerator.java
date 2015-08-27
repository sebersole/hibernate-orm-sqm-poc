package org.hibernate.sql.gen;

import org.hibernate.sql.gen.internal.SelectSqlPlanImpl;
import org.hibernate.sqm.query.SelectStatement;

import javax.persistence.criteria.CriteriaQuery;

/**
 * Main entry point for SQL generator
 *
 * Created by johara on 27/08/15.
 */
public class SqlGenerator {

	public static SqlPlan generateSqlPlan(SelectStatement selectStatement){

		return new SelectSqlPlanImpl(selectStatement);
	}

	public static SqlPlan generateSqlPlan(CriteriaQuery criteriaQuery){
		throw new NotYetImplementedException();
	}
}
