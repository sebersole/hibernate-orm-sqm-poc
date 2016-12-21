/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.orm.test.sql;

import java.util.function.Consumer;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.orm.test.sql.support.ExecutionContextTestingImpl;
import org.hibernate.orm.test.sql.support.QueryProducerTestingImpl;
import org.hibernate.query.proposed.internal.sqm.QuerySqmImpl;
import org.hibernate.sqm.SemanticQueryInterpreter;

/**
 * @author Steve Ebersole
 */
public abstract class BaseExecutionTest extends BaseUnitTest {
	@Override
	protected boolean exportSchema() {
		return true;
	}

	protected void doInSession(Consumer<SharedSessionContractImplementor> work) {
		final SharedSessionContractImplementor session = (SharedSessionContractImplementor) getSessionFactory().openSession();

		try {
			work.accept( session );
		}
		finally {
			session.close();
		}
	}

	@SuppressWarnings("unchecked")
	protected <T> QuerySqmImpl<T> generateQueryImpl(SharedSessionContractImplementor session, String qryStr, Class<T> resultType) {
		return new QuerySqmImpl(
				qryStr,
				SemanticQueryInterpreter.interpret( qryStr, getConsumerContext() ),
				resultType,
				session,
				getConsumerContext().getDomainMetamodel(),
				new QueryProducerTestingImpl( session ),
				new ExecutionContextTestingImpl( session )
		);
	}
}
