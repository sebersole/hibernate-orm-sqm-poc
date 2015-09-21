package org.hibernate.sql.gen.internal;

import java.util.List;

import org.hibernate.loader.plan.spi.Return;
import org.hibernate.sql.gen.JdbcSelectPlan;
import org.hibernate.sql.gen.ParameterBinder;
import org.hibernate.sql.gen.QueryOptionBinder;

/**
 * @author Steve Ebersole
 * @author John O'Hara
 */
public class JdbcSelectPlanImpl implements JdbcSelectPlan {
	private final String sql;
	private final List<ParameterBinder> parameterBinders;
	private final List<QueryOptionBinder> queryOptionBinders;
	private final List<Return> returnDescriptors;

	public JdbcSelectPlanImpl(
			String sql,
			List<ParameterBinder> parameterBinders,
			List<QueryOptionBinder> queryOptionBinders,
			List<Return> returnDescriptors) {

		this.sql = sql;
		this.parameterBinders = parameterBinders;
		this.queryOptionBinders = queryOptionBinders;
		this.returnDescriptors = returnDescriptors;
	}

	@Override
	public List<Return> getReturns() {
		return returnDescriptors;
	}

	@Override
	public String getSql() {
		return sql;
	}

	@Override
	public List<ParameterBinder> getParameterBinders() {
		return parameterBinders;
	}

	@Override
	public List<QueryOptionBinder> getQueryOptionBinders() {
		return queryOptionBinders;
	}
}
