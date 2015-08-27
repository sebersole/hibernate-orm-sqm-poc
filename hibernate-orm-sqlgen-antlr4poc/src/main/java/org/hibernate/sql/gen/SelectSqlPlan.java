package org.hibernate.sql.gen;

import org.hibernate.loader.plan.spi.LoadPlan;

/**
 * Created by johara on 27/08/15.
 */
public interface SelectSqlPlan extends  SqlPlan {

	LoadPlan getLoadPlan();


}
