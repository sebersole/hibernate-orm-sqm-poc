package org.hibernate.sql.gen.internal;

import org.hibernate.sqm.query.SelectStatement;

/**
 * Created by johara on 27/08/15.
 */
public class ModifySqlPlan extends AbstractSqlPlan{

	protected ModifySqlPlan(SelectStatement selectStatement) {
		super( selectStatement );
	}
}
