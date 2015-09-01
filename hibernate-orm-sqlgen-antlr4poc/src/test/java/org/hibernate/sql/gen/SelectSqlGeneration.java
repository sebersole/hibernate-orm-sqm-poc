package org.hibernate.sql.gen;

import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.model.naming.ImplicitNamingStrategyJpaCompliantImpl;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.query.parser.ConsumerContext;
import org.hibernate.query.parser.SemanticQueryInterpreter;
import org.hibernate.sql.gen.sqm.ConsumerContextTestingImpl;
import org.hibernate.sqm.query.SelectStatement;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

/**
 * Created by John O'Hara on 27/08/15.
 */
public class SelectSqlGeneration {

	@Test
	public void simpleSelectTest() {
		final String query = "from DTO d where d.id = 1";
		final ConsumerContext consumerContext = new ConsumerContextTestingImpl();
		final SelectStatement selectStatement = (SelectStatement) SemanticQueryInterpreter.interpret(
				query,
				consumerContext
		);

		JdbcSelectPlan jdbcSelectPlan = SqmJdbcInterpreter.interpret( selectStatement, null, null );

		assertNotNull( jdbcSelectPlan );
	}

	@Test
	public void simpleBootstrapTest() {
		StandardServiceRegistry standardRegistry = new StandardServiceRegistryBuilder()
				.configure( "hibernate.cfg.xml" )
				.build();

		Metadata metadata = new MetadataSources( standardRegistry )
				.getMetadataBuilder()
				.applyImplicitNamingStrategy( ImplicitNamingStrategyJpaCompliantImpl.INSTANCE )
				.build();

		SessionFactory sessionFactory = metadata.getSessionFactoryBuilder()
				.build();
//		.applyBeanManager( getBeanManagerFromSomewhere() )

		assertNotNull(sessionFactory);

		final String query = "from Book b where b.id = 1";
		final ConsumerContext consumerContext = new ConsumerContextTestingImpl();
		final SelectStatement selectStatement = (SelectStatement) SemanticQueryInterpreter.interpret(
				query,
				consumerContext
		);

		JdbcSelectPlan jdbcSelectPlan = SqmJdbcInterpreter.interpret( selectStatement, null, null);

		assertNotNull( jdbcSelectPlan );

	}

}
