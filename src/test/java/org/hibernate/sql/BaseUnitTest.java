/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql;

import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.loader.spi.AfterLoadAction;
import org.hibernate.persister.common.internal.PersisterFactoryImpl;
import org.hibernate.persister.internal.PersisterFactoryInitiator;
import org.hibernate.query.proposed.QueryOptions;
import org.hibernate.sql.convert.spi.Callback;
import org.hibernate.sql.convert.spi.SqmSelectInterpretation;
import org.hibernate.sql.convert.spi.SqmSelectToSqlAstConverter;
import org.hibernate.sqm.SemanticQueryInterpreter;
import org.hibernate.sqm.query.SqmSelectStatement;
import org.hibernate.sqm.query.SqmStatement;

import org.hibernate.testing.junit4.CustomRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;

/**
 * Test for asserting structures, etc.  Does not export the schema
 *
 * @author Steve Ebersole
 */
@RunWith( CustomRunner.class )
public abstract class BaseUnitTest {
	private SessionFactoryImplementor sessionFactory;
	private ConsumerContextImpl consumerContext;

	@Before
	public void before() throws Exception {
		final StandardServiceRegistry ssr = new StandardServiceRegistryBuilder()
				.applySetting( AvailableSettings.JPAQL_STRICT_COMPLIANCE, strictJpaCompliance() )
				.applySetting( PersisterFactoryInitiator.IMPL_NAME, PersisterFactoryImpl.INSTANCE )
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

	protected SqmStatement interpret(String query) {
		return SemanticQueryInterpreter.interpret( query, getConsumerContext() );
	}

	protected SqmSelectInterpretation interpretSelectQuery(String query) {
		final SqmSelectStatement statement = (SqmSelectStatement) interpret( query );

		return SqmSelectToSqlAstConverter.interpret(
				statement,
				getSessionFactory(),
				getConsumerContext().getDomainMetamodel(),
				queryOptions(),
				false,
				callBack()
		);
	}

	protected Callback callBack() {
		return new Callback() {
			@Override
			public void registerAfterLoadAction(AfterLoadAction afterLoadAction) {
				// do nothing here
			}
		};
	}

	protected QueryOptions queryOptions() {
		return new QueryOptionsTestingImpl();
	}

}
