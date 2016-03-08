package org.hibernate.sql.gen.internal;

import org.hibernate.sql.gen.JdbcOperationPlan;
import org.hibernate.sql.gen.NotYetImplementedException;
import org.hibernate.sql.gen.ParameterBinder;
import org.hibernate.sql.gen.QueryOptionBinder;

import java.util.List;

/**
 *
 * @author Steve Ebersole
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

	@Override
	public List<QueryOptionBinder> getQueryOptionBinders() {
		throw new NotYetImplementedException();
	}
}
