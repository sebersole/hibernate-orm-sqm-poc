/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.sql.gen;

import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.sql.ast.SelectQuery;
import org.hibernate.sql.ast.SelectStatementInterpreter;
import org.hibernate.query.internal.ConsumerContextImpl;
import org.hibernate.sqm.SemanticQueryInterpreter;
import org.hibernate.sqm.query.SelectStatement;
import org.hibernate.sqm.query.Statement;

import org.junit.After;
import org.junit.Before;

/**
 * Test for asserting structures, etc.  Does not export the schema
 *
 * @author Steve Ebersole
 */
public class BaseUnitTest {
	private SessionFactoryImplementor sessionFactory;
	private ConsumerContextImpl consumerContext;

	@Before
	public void before() throws Exception {
		final StandardServiceRegistry ssr = new StandardServiceRegistryBuilder()
				.applySetting( AvailableSettings.JPAQL_STRICT_COMPLIANCE, strictJpaCompliance() )
				.build();

		try {
			MetadataSources metadataSources = new MetadataSources( ssr );
			applyMetadataSources( metadataSources );

			this.sessionFactory = (SessionFactoryImplementor) metadataSources.buildMetadata().buildSessionFactory();
		}
		catch (Exception e) {
			StandardServiceRegistryBuilder.destroy( ssr );
			throw e;
		}

		consumerContext = new ConsumerContextImpl( sessionFactory );
	}

	@After
	public void after() {
		if ( sessionFactory != null ) {
			sessionFactory.close();
		}
	}

	protected boolean strictJpaCompliance() {
		return false;
	}

	protected void applyMetadataSources(MetadataSources metadataSources) {
	}

	protected final SessionFactoryImplementor getSessionFactory() {
		return sessionFactory;
	}

	protected final ConsumerContextImpl getConsumerContext() {
		return consumerContext;
	}

	protected Statement interpret(String query) {
		return SemanticQueryInterpreter.interpret( query, getConsumerContext() );
	}

	protected SelectQuery interpretSelectQuery(String query) {
		final SelectStatement statement = (SelectStatement) interpret( query );

		final SelectStatementInterpreter interpreter = new SelectStatementInterpreter( queryOptions(), callBack() );
		interpreter.interpret( statement );

		return interpreter.getSelectQuery();
	}

	protected Callback callBack() {
		return new Callback() {
		};
	}

	protected org.hibernate.sql.exec.spi.QueryOptions queryOptions() {
		return new QueryOptionsTestingImpl();
	}

}
