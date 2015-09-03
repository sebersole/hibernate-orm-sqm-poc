package org.hibernate.sql.gen;

import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.model.naming.ImplicitNamingStrategyJpaCompliantImpl;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.loader.plan.spi.LoadPlan;
import org.hibernate.query.parser.ConsumerContext;
import org.hibernate.query.parser.SemanticQueryInterpreter;
import org.hibernate.sql.gen.sqm.ConsumerContextImpl;
import org.hibernate.sql.gen.sqm.ConsumerContextTestingImpl;
import org.hibernate.sqm.query.SelectStatement;
import org.junit.Test;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

/**
 * @author John O'Hara
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

		LoadPlan loadPlan = jdbcSelectPlan.getLoadPlan();

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

		assertNotNull( sessionFactory );

//		ConsumerContextImpl consumerContext = new ConsumerContextImpl(sessionFactory);

		final String query = "from Message m where m.id = 1";
//		final ConsumerContext consumerContext = new ConsumerContextTestingImpl();
		final ConsumerContext consumerContext = new ConsumerContextImpl( (SessionFactoryImplementor) sessionFactory );

		final SelectStatement selectStatement = (SelectStatement) SemanticQueryInterpreter.interpret(
				query,
				consumerContext
		);

		JdbcSelectPlan jdbcSelectPlan = SqmJdbcInterpreter.interpret( selectStatement, null, null );

		assertNotNull( jdbcSelectPlan );

		String selectSql = jdbcSelectPlan.getSql();

		assertNotNull( selectSql );

		assertSame( "select * from PUBLIC.Message as m where m.id = 1", selectSql );

	}



	@Entity( name = "Message" )
	public static class Message {
		@Id
		private Integer mid;
		private String msgTxt;
		@ManyToOne( cascade = CascadeType.MERGE )
		@JoinColumn
		private Poster poster;
	}

	@Entity( name = "Poster" )
	public static class Poster {
		@Id
		private Integer pid;
		private String name;
		@OneToMany(mappedBy = "poster")
		private List<Message> messages;
	}
}
