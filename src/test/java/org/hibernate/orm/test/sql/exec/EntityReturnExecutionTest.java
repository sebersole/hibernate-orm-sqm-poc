/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.orm.test.sql.exec;

import java.util.List;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.boot.MetadataSources;
import org.hibernate.orm.test.sql.BaseExecutionTest;
import org.hibernate.query.proposed.internal.sqm.QuerySqmImpl;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * @author Steve Ebersole
 */
public class EntityReturnExecutionTest extends BaseExecutionTest {

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

	@Override
	public void before() throws Exception {
		super.before();

		Session session = getSessionFactory().openSession();
		session.beginTransaction();
		Employee ss = new Employee( 1, "Cosmo G. Spacely" );
		Employee gj = new Employee( 2, "George Jetson", ss );
		session.persist( ss );
		session.persist( gj );
		session.getTransaction().commit();
		session.close();
	}

	@Override
	protected void applyMetadataSources(MetadataSources metadataSources) {
		super.applyMetadataSources( metadataSources );
		metadataSources.addAnnotatedClass( Employee.class );
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
