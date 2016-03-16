/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.gen;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Tuple;

import org.hibernate.Session;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.query.internal.QueryImpl;
import org.hibernate.query.internal.ConsumerContextImpl;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.hamcrest.CoreMatchers;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

/**
 * @author Steve Ebersole
 */
public class FullStackTest {
	private SessionFactoryImplementor sessionFactory;
	private ConsumerContextImpl consumerContext;

	@Before
	public void before() throws Exception {
		final StandardServiceRegistry ssr = new StandardServiceRegistryBuilder()
				.applySetting( AvailableSettings.HBM2DDL_AUTO, "create-drop" )
				.build();

		try {
			MetadataSources metadataSources = new MetadataSources( ssr );
			metadataSources.addAnnotatedClass( Person.class );

			this.sessionFactory = (SessionFactoryImplementor) metadataSources.buildMetadata().buildSessionFactory();
		}
		catch (Exception e) {
			StandardServiceRegistryBuilder.destroy( ssr );
			throw e;
		}

		insertRow();

		consumerContext = new ConsumerContextImpl( sessionFactory );
	}

	private void insertRow() {
		Session session = sessionFactory.openSession();
		session.beginTransaction();
		session.persist( new Person( 1, "Steve", 20 ) );
		session.getTransaction().commit();
		session.close();
	}

	@After
	public void after() {
		Session session = sessionFactory.openSession();
		session.beginTransaction();
		session.createQuery( "delete Person" ).executeUpdate();
		session.getTransaction().commit();
		session.close();

		if ( sessionFactory != null ) {
			sessionFactory.close();
		}
	}

	@Test
	public void testFullStack() throws SQLException {
		final Session session = sessionFactory.openSession();

		QueryImpl query = new QueryImpl(
				"select p.name from Person p where p.age >= 20 and p.age <= ?1",
				(SessionImplementor) session,
				consumerContext
		);

		query.setParameter( 1, 39 );
		final List results = query.list();

		assertThat( results.size(), is( 1 ) );
		assertThat( results.get( 0 ), instanceOf( Object[].class ) );
		Object[] row = (Object[]) results.get( 0 );
		assertThat( row.length, is( 1 ) );
		assertThat( row[0], instanceOf( String.class ) );
		assertThat( (String)row[0], is("Steve") );
	}

	@Test
	public void testFullStackTyped() throws SQLException {
		final Session session = sessionFactory.openSession();

		QueryImpl<String> query = new QueryImpl<String>(
				"select p.name from Person p where p.age >= 20 and p.age <= ?1",
				String.class,
				(SessionImplementor) session,
				consumerContext
		);

		query.setParameter( 1, 39 );
		final List results = query.list();

		assertThat( results.size(), is( 1 ) );
		assertThat( results.get( 0 ), instanceOf( String.class ) );
		String name = (String) results.get( 0 );
		assertThat( name, is("Steve") );
	}

	@Test
	public void testFullStackTupleTyped() throws SQLException {
		final Session session = sessionFactory.openSession();

		QueryImpl<Tuple> query = new QueryImpl<Tuple>(
				"select p.name as name from Person p where p.age >= 20 and p.age <= ?1",
				Tuple.class,
				(SessionImplementor) session,
				consumerContext
		);

		query.setParameter( 1, 39 );
		final List<Tuple> results = query.list();
		assertThat( results.size(), is( 1 ) );
		Tuple tuple = results.get( 0 );
		assertThat( (String) tuple.get( "name" ), is("Steve") );
	}

	@Test
	public void testFullStackDynamicInstantiation() throws SQLException {
		final Session session = sessionFactory.openSession();

		QueryImpl<Person> query = new QueryImpl<Person>(
				"select new Person( p.id, p.name, p.age ) from Person p where p.age >= 20 and p.age <= ?1",
				Person.class,
				(SessionImplementor) session,
				consumerContext
		);

		query.setParameter( 1, 39 );
		final List<Person> results = query.list();
		assertThat( results.size(), is( 1 ) );
		Person person = results.get( 0 );
		assertThat( person.name, is("Steve") );
		assertThat( person.id, is(1) );
		assertThat( person.age, is(20) );
	}

	@Test
	public void testFullStackDynamicInstantiationInjection() throws SQLException {
		final Session session = sessionFactory.openSession();

		QueryImpl<Person> query = new QueryImpl<Person>(
				"select new Person( p.name as name, p.age as age ) from Person p where p.age >= 20 and p.age <= ?1",
				Person.class,
				(SessionImplementor) session,
				consumerContext
		);

		query.setParameter( 1, 39 );
		final List<Person> results = query.list();
		assertThat( results.size(), is( 1 ) );
		Person person = results.get( 0 );
		assertThat( person.id, is(nullValue()) );
		assertThat( person.name, is("Steve") );
		assertThat( person.age, is(20) );
	}

	@Test
	public void testFullStackDynamicInstantiationList() throws SQLException {
		final Session session = sessionFactory.openSession();

		QueryImpl<List> query = new QueryImpl<List>(
				"select new list(p.name, p.age) from Person p where p.age >= 20 and p.age <= ?1",
				List.class,
				(SessionImplementor) session,
				consumerContext
		);

		query.setParameter( 1, 39 );
		final List<List> results = query.list();
		assertThat( results.size(), is( 1 ) );
		List tuples = results.get( 0 );
		assertThat( tuples.size(), is(2) );
		assertThat( tuples.get(0), CoreMatchers.<Object>is("Steve") );
		assertThat( tuples.get(1), CoreMatchers.<Object>is(20) );
	}

	@Test
	public void testFullStackDynamicInstantiationMap() throws SQLException {
		final Session session = sessionFactory.openSession();

		QueryImpl<Map> query = new QueryImpl<Map>(
				"select new map(p.name as name, p.age as age) from Person p where p.age >= 20 and p.age <= ?1",
				Map.class,
				(SessionImplementor) session,
				consumerContext
		);

		query.setParameter( 1, 39 );
		final List<Map> results = query.list();
		assertThat( results.size(), is( 1 ) );
		Map tuples = results.get( 0 );
		assertThat( tuples.size(), is(2) );
		assertThat( tuples.get("name"), CoreMatchers.<Object>is("Steve") );
		assertThat( tuples.get("age"), CoreMatchers.<Object>is(20) );
	}

	@Entity(name="Person")
	public static class Person {
		@Id
		Integer id;
		String name;
		int age;

		public Person() {
		}

		public Person(Integer id, String name, int age) {
			this.id = id;
			this.name = name;
			this.age = age;
		}
	}

}
