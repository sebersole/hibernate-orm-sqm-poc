/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.gen.sqm;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import javax.persistence.Entity;
import javax.persistence.Id;

import org.hibernate.LockOptions;
import org.hibernate.ScrollMode;
import org.hibernate.Session;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;
import org.hibernate.engine.jdbc.internal.FormatStyle;
import org.hibernate.engine.spi.RowSelection;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.jdbc.Work;
import org.hibernate.sql.ast.SelectQuery;
import org.hibernate.sql.gen.Callback;
import org.hibernate.sql.gen.SqlTreeWalker;
import org.hibernate.sql.gen.internal.SelectStatementInterpreter;
import org.hibernate.sql.orm.QueryOptions;
import org.hibernate.sql.orm.QueryParameterBindings;
import org.hibernate.sqm.SemanticQueryInterpreter;
import org.hibernate.sqm.query.SelectStatement;
import org.hibernate.sqm.query.Statement;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
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
		final SelectQuery sqlTree = interpretSelectQuery( "select p.name from Person p where p.age = 20" );
		final SqlTreeWalker sqlTreeWalker = new SqlTreeWalker( sessionFactory );
		sqlTreeWalker.visitSelectQuery( sqlTree );

		assertThat( sqlTreeWalker.getSql(), notNullValue() );
		assertThat( sqlTreeWalker.getParameterBinders().size(), is(1) );
		assertThat( sqlTreeWalker.getReturns().size(), is(1) );

		final Session session = sessionFactory.openSession();
		session.doWork(
				new Work() {
					@Override
					public void execute(Connection connection) throws SQLException {
						final PreparedStatement ps = connection.prepareStatement( sqlTreeWalker.getSql() );
						sqlTreeWalker.getParameterBinders().get( 0 ).bindParameterValue(
								ps,
								1,
								null,
								(SessionImplementor) session
						);
						ResultSet resultSet = ps.executeQuery();
						resultSet.next();
						Object result = sqlTreeWalker.getReturns().get( 0 ).readResult(
								resultSet,
								1,
								(SessionImplementor) session,
								null
						);

						resultSet.close();
						ps.close();

						assertThat( result.toString(), is("Steve") );
					}
				}
		);
	}

	protected SelectQuery interpretSelectQuery(String query) {
		final SelectStatement statement = (SelectStatement) interpret( query );

		final SelectStatementInterpreter interpreter = new SelectStatementInterpreter( queryOption(), callBack() );
		interpreter.interpret( statement );

		return interpreter.getSelectQuery();
	}

	protected Statement interpret(String query) {
		return SemanticQueryInterpreter.interpret( query, consumerContext );
	}

	protected Callback callBack() {
		return new Callback() {
		};
	}

	protected QueryOptions queryOption() {
		return new QueryOptions() {
			@Override
			public QueryParameterBindings getParameterBindings() {
				return null;
			}

			@Override
			public LockOptions getLockOptions() {
				return null;
			}

			@Override
			public RowSelection getRowSelection() {
				return null;
			}

			@Override
			public ScrollMode getScrollMode() {
				return null;
			}

			@Override
			public boolean isCacheable() {
				return false;
			}

			@Override
			public String getCacheRegion() {
				return null;
			}

			@Override
			public String getComment() {
				return null;
			}

			@Override
			public List<String> getSqlHints() {
				return null;
			}
		};
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
