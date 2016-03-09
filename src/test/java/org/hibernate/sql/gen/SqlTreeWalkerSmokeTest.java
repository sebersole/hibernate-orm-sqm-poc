/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.gen;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.persistence.Entity;
import javax.persistence.Id;

import org.hibernate.Session;
import org.hibernate.boot.MetadataSources;
import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;
import org.hibernate.engine.jdbc.internal.FormatStyle;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.jdbc.Work;
import org.hibernate.sql.ast.SelectQuery;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * @author Steve Ebersole
 */
public class SqlTreeWalkerSmokeTest extends BaseUnitTest {
	@Override
	protected void applyMetadataSources(MetadataSources metadataSources) {
		super.applyMetadataSources( metadataSources );
		metadataSources.addAnnotatedClass( Person.class );
	}

	@Test
	public void testSqlTreeWalking1() {
		SelectQuery sqlTree = interpretSelectQuery( "select p.name from Person p" );
		SqlTreeWalker sqlTreeWalker = new SqlTreeWalker( getSessionFactory() );
		sqlTreeWalker.visitSelectQuery( sqlTree );

		System.out.println( FormatStyle.BASIC.getFormatter().format( sqlTreeWalker.getSql() ) );

		assertThat( sqlTreeWalker.getSql(), notNullValue() );
		assertThat( sqlTreeWalker.getReturns().size(), is(1) );
	}

	@Test
	public void testSqlTreeWalking2() {
		SelectQuery sqlTree = interpretSelectQuery( "select p.name, p2.name from Person p, Person p2" );
		SqlTreeWalker sqlTreeWalker = new SqlTreeWalker( getSessionFactory() );
		sqlTreeWalker.visitSelectQuery( sqlTree );

		System.out.println( FormatStyle.BASIC.getFormatter().format( sqlTreeWalker.getSql() ) );

		assertThat( sqlTreeWalker.getSql(), notNullValue() );
		assertThat( sqlTreeWalker.getReturns().size(), is(2) );
	}

	@Test
	public void testSqlTreeWalking3() {
		SelectQuery sqlTree = interpretSelectQuery( "select p.name from Person p where p.age between 20 and 39" );
		SqlTreeWalker sqlTreeWalker = new SqlTreeWalker( getSessionFactory() );
		sqlTreeWalker.visitSelectQuery( sqlTree );

		System.out.println( FormatStyle.BASIC.getFormatter().format( sqlTreeWalker.getSql() ) );

		assertThat( sqlTreeWalker.getSql(), notNullValue() );
		// each literal is transformed into a parameter, so check the number of ParameterBinders
		assertThat( sqlTreeWalker.getParameterBinders().size(), is(2) );
		assertThat( sqlTreeWalker.getReturns().size(), is(1) );
	}

	@Test
	public void testSqlTreeWalking4() {
		SelectQuery sqlTree = interpretSelectQuery( "select p.name from Person p where (p.age <= 20 and p.age >= 39)" );
		SqlTreeWalker sqlTreeWalker = new SqlTreeWalker( getSessionFactory() );
		sqlTreeWalker.visitSelectQuery( sqlTree );

		System.out.println( FormatStyle.BASIC.getFormatter().format( sqlTreeWalker.getSql() ) );

		assertThat( sqlTreeWalker.getSql(), notNullValue() );
		// each literal is transformed into a parameter, so check the number of ParameterBinders
		assertThat( sqlTreeWalker.getParameterBinders().size(), is(2) );
		assertThat( sqlTreeWalker.getReturns().size(), is(1) );
	}

	@Test
	public void testSqlTreeWalking5() {
		SelectQuery sqlTree = interpretSelectQuery( "select p.age from Person p where p.name like 'Steve%'" );
		SqlTreeWalker sqlTreeWalker = new SqlTreeWalker( getSessionFactory() );
		sqlTreeWalker.visitSelectQuery( sqlTree );

		System.out.println( FormatStyle.BASIC.getFormatter().format( sqlTreeWalker.getSql() ) );

		assertThat( sqlTreeWalker.getSql(), notNullValue() );
		// each literal is transformed into a parameter, so check the number of ParameterBinders
		assertThat( sqlTreeWalker.getParameterBinders().size(), is(1) );
		assertThat( sqlTreeWalker.getReturns().size(), is(1) );
	}

	@Test
	public void testSqlTreeWalking6() {
		SelectQuery sqlTree = interpretSelectQuery( "select p.age from Person p where p.name like 'Steve%' escape '/'" );
		SqlTreeWalker sqlTreeWalker = new SqlTreeWalker( getSessionFactory() );
		sqlTreeWalker.visitSelectQuery( sqlTree );

		System.out.println( FormatStyle.BASIC.getFormatter().format( sqlTreeWalker.getSql() ) );

		assertThat( sqlTreeWalker.getSql(), notNullValue() );
		// each literal is transformed into a parameter, so check the number of ParameterBinders
		assertThat( sqlTreeWalker.getParameterBinders().size(), is(2) );
		assertThat( sqlTreeWalker.getReturns().size(), is(1) );
	}

	@Test
	public void testSqlTreeWalking7() {
		SelectQuery sqlTree = interpretSelectQuery( "select p.age from Person p where p.name is null" );
		SqlTreeWalker sqlTreeWalker = new SqlTreeWalker( getSessionFactory() );
		sqlTreeWalker.visitSelectQuery( sqlTree );

		System.out.println( FormatStyle.BASIC.getFormatter().format( sqlTreeWalker.getSql() ) );

		assertThat( sqlTreeWalker.getSql(), notNullValue() );
		// each literal is transformed into a parameter, so check the number of ParameterBinders
		assertThat( sqlTreeWalker.getParameterBinders().size(), is(0) );
		assertThat( sqlTreeWalker.getReturns().size(), is(1) );
	}

	@Test
	public void testSqlTreeWalking8() {
		SelectQuery sqlTree = interpretSelectQuery( "select p.age from Person p where p.name is not null" );
		SqlTreeWalker sqlTreeWalker = new SqlTreeWalker( getSessionFactory() );
		sqlTreeWalker.visitSelectQuery( sqlTree );

		System.out.println( FormatStyle.BASIC.getFormatter().format( sqlTreeWalker.getSql() ) );

		assertThat( sqlTreeWalker.getSql(), notNullValue() );
		// each literal is transformed into a parameter, so check the number of ParameterBinders
		assertThat( sqlTreeWalker.getParameterBinders().size(), is(0) );
		assertThat( sqlTreeWalker.getReturns().size(), is(1) );
	}

	@Test
	public void testSqlTreeWalking9() {
		SelectQuery sqlTree = interpretSelectQuery( "select p.age from Person p where not p.name is not null" );
		SqlTreeWalker sqlTreeWalker = new SqlTreeWalker( getSessionFactory() );
		sqlTreeWalker.visitSelectQuery( sqlTree );

		System.out.println( FormatStyle.BASIC.getFormatter().format( sqlTreeWalker.getSql() ) );

		assertThat( sqlTreeWalker.getSql(), notNullValue() );
		// each literal is transformed into a parameter, so check the number of ParameterBinders
		assertThat( sqlTreeWalker.getParameterBinders().size(), is(0) );
		assertThat( sqlTreeWalker.getReturns().size(), is(1) );
	}

	@Entity(name="Person")
	public static class Person {
		@Id
		Integer id;
		String name;
		int age;
	}
}
