/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.exec;

import java.util.List;
import java.util.function.Consumer;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.persister.common.internal.PersisterFactoryImpl;
import org.hibernate.persister.internal.PersisterFactoryInitiator;
import org.hibernate.query.proposed.internal.sqm.QuerySqmImpl;
import org.hibernate.sql.ConsumerContextImpl;
import org.hibernate.sql.ExecutionContextTestImpl;
import org.hibernate.sql.QueryProducerTestImpl;
import org.hibernate.sqm.SemanticQueryInterpreter;

import org.hibernate.testing.FailureExpected;
import org.hibernate.testing.junit4.BaseUnitTestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * @author Steve Ebersole
 */
public class EntityReturnExecutionTest extends BaseUnitTestCase {

	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Fixtures

	private SessionFactoryImplementor sessionFactory;
	private ConsumerContextImpl consumerContext;


	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Tests

	@Test
	public void testSelectionOfIdentificationVariable() {
		doInSession(
				session -> {
					final QuerySqmImpl<Employee> query = generateQueryImpl(
							session,
							"select e from Employee e where e.id = :id",
							Employee.class
					);

					query.setParameter( "id", 2 );
					final List<Employee> results = query.list();

					assertThat( results.size(), is( 1 ) );
					assertThat( results.get( 0 ), instanceOf( Employee.class ) );
					Employee result = results.get( 0 );
					assertThat( result.name, is( "George Jetson" ) );
					assertThat( result.manager.id, is(1) );

					// todo : this should not be initialized - but that needs Type changes
					assertTrue( Hibernate.isInitialized( result.manager ) );
				}
		);
	}

	private void doInSession(Consumer<SharedSessionContractImplementor> work) {
		final SharedSessionContractImplementor session = (SharedSessionContractImplementor) sessionFactory.openSession();

		try {
			work.accept( session );
		}
		finally {
			session.close();
		}
	}

	@SuppressWarnings("unchecked")
	private <T> QuerySqmImpl<T> generateQueryImpl(SharedSessionContractImplementor session, String qryStr, Class<T> resultType) {
		return new QuerySqmImpl(
				qryStr,
				SemanticQueryInterpreter.interpret( qryStr, consumerContext ),
				resultType,
				session,
				consumerContext.getDomainMetamodel(),
				new QueryProducerTestImpl( session ),
				new ExecutionContextTestImpl( session )
		);
	}


	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Infrastructure

	@Before
	public void before() throws Exception {
		final StandardServiceRegistry ssr = new StandardServiceRegistryBuilder()
				.applySetting( AvailableSettings.HBM2DDL_AUTO, "create-drop" )
				.applySetting( PersisterFactoryInitiator.IMPL_NAME, PersisterFactoryImpl.INSTANCE )
				.build();

		try {
			MetadataSources metadataSources = new MetadataSources( ssr );
			metadataSources.addAnnotatedClass( Employee.class );

			this.sessionFactory = (SessionFactoryImplementor) metadataSources.buildMetadata().buildSessionFactory();
		}
		catch (Exception e) {
			StandardServiceRegistryBuilder.destroy( ssr );
			throw e;
		}

		Session session = sessionFactory.openSession();
		session.beginTransaction();
		Employee ss = new Employee( 1, "Cosmo G. Spacely" );
		Employee gj = new Employee( 2, "George Jetson", ss );
		session.persist( ss );
		session.persist( gj );
		session.getTransaction().commit();
		session.close();

		consumerContext = new ConsumerContextImpl( sessionFactory );
	}

	@After
	public void after() {
		Session session = sessionFactory.openSession();
		session.beginTransaction();
		session.createQuery( "delete Employee" ).executeUpdate();
		session.getTransaction().commit();
		session.close();

		if ( sessionFactory != null ) {
			sessionFactory.close();
		}
	}

	@Entity(name = "Employee")
	public static class Employee {
		@Id
		private Integer id;
		private String name;
		@ManyToOne
		private Employee manager;

		public Employee() {
		}

		public Employee(Integer id, String name) {
			this.id = id;
			this.name = name;
		}

		public Employee(Integer id, String name, Employee manager) {
			this.id = id;
			this.name = name;
			this.manager = manager;
		}
	}
}
