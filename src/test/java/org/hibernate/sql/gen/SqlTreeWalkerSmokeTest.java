/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.gen;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.hibernate.boot.MetadataSources;
import org.hibernate.engine.jdbc.internal.FormatStyle;
import org.hibernate.query.proposed.ParameterMetadata;
import org.hibernate.query.proposed.QueryParameter;
import org.hibernate.query.proposed.internal.ParameterMetadataImpl;
import org.hibernate.query.proposed.internal.QueryParameterBindingsImpl;
import org.hibernate.query.proposed.internal.QueryParameterNamedImpl;
import org.hibernate.query.proposed.internal.QueryParameterPositionalImpl;
import org.hibernate.sql.QueryParameterBindingTypeResolverImpl;
import org.hibernate.sql.ast.SelectQuery;
import org.hibernate.query.proposed.spi.QueryParameterBindings;
import org.hibernate.sql.convert.spi.SelectStatementInterpreter;
import org.hibernate.sql.convert.spi.SqlTreeWalker;
import org.hibernate.sqm.query.SqmSelectStatement;
import org.hibernate.sqm.query.SqmStatement;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.containsString;
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
		metadataSources.addAnnotatedClass( Address.class );
		metadataSources.addAnnotatedClass( Role.class );
	}

	@Test
	public void testSqlTreeWalking1() {
		final SqmSelectStatement statement = (SqmSelectStatement) interpret( "select p.name from Person p" );

		final SelectStatementInterpreter interpreter = new SelectStatementInterpreter( queryOptions(), callBack() );
		interpreter.interpret( statement );

		SelectQuery sqlTree = interpreter.getSelectQuery();

		SqlTreeWalker sqlTreeWalker = new SqlTreeWalker(
				getSessionFactory(),
				buildQueryParameterBindings( statement )
		);
		sqlTreeWalker.visitSelectQuery( sqlTree );

		System.out.println( FormatStyle.BASIC.getFormatter().format( sqlTreeWalker.getSql() ) );

		assertThat( sqlTreeWalker.getSql(), notNullValue() );
		assertThat( sqlTreeWalker.getReturns().size(), is(1) );
	}

	private QueryParameterBindings buildQueryParameterBindings(SqmSelectStatement statement) {
		return QueryParameterBindingsImpl.from(
				buildParameterMetadata( statement ),
				new QueryParameterBindingTypeResolverImpl( getSessionFactory() )
		);
	}


	private static ParameterMetadataImpl buildParameterMetadata(SqmStatement sqm) {
		Map<String, QueryParameter> namedQueryParameters = null;
		Map<Integer, QueryParameter> positionalQueryParameters = null;

		for ( org.hibernate.sqm.query.Parameter parameter : sqm.getQueryParameters() ) {
			if ( parameter.getName() != null ) {
				if ( namedQueryParameters == null ) {
					namedQueryParameters = new HashMap<>();
				}
				namedQueryParameters.put(
						parameter.getName(),
						QueryParameterNamedImpl.fromSqm( parameter )
				);
			}
			else if ( parameter.getPosition() != null ) {
				if ( positionalQueryParameters == null ) {
					positionalQueryParameters = new HashMap<>();
				}
				positionalQueryParameters.put(
						parameter.getPosition(),
						QueryParameterPositionalImpl.fromSqm( parameter )
				);
			}
		}

		return new ParameterMetadataImpl( namedQueryParameters, positionalQueryParameters );
	}

	@Test
	public void testSqlTreeWalking2() {
		final String qryStr = "select p.name, p2.name from Person p, Person p2";

		final SqmSelectStatement statement = (SqmSelectStatement) interpret( qryStr );
		final SelectStatementInterpreter interpreter = new SelectStatementInterpreter( queryOptions(), callBack() );
		interpreter.interpret( statement );
		final SelectQuery sqlTree = interpreter.getSelectQuery();
		SqlTreeWalker sqlTreeWalker = new SqlTreeWalker(
				getSessionFactory(),
				buildQueryParameterBindings( statement )
		);
		sqlTreeWalker.visitSelectQuery( sqlTree );

		System.out.println( FormatStyle.BASIC.getFormatter().format( sqlTreeWalker.getSql() ) );

		assertThat( sqlTreeWalker.getSql(), notNullValue() );
		assertThat( sqlTreeWalker.getReturns().size(), is(2) );
	}

	@Test
	public void testSqlTreeWalking3() {
		final String qryStr = "select p.name from Person p where p.age between 20 and 39";
		final SqmSelectStatement statement = (SqmSelectStatement) interpret( qryStr );
		final SelectStatementInterpreter interpreter = new SelectStatementInterpreter( queryOptions(), callBack() );
		interpreter.interpret( statement );
		final SelectQuery sqlTree = interpreter.getSelectQuery();
		SqlTreeWalker sqlTreeWalker = new SqlTreeWalker(
				getSessionFactory(),
				buildQueryParameterBindings( statement )
		);
		sqlTreeWalker.visitSelectQuery( sqlTree );


		System.out.println( FormatStyle.BASIC.getFormatter().format( sqlTreeWalker.getSql() ) );

		assertThat( sqlTreeWalker.getSql(), notNullValue() );
		// each literal is transformed into a parameter, so check the number of ParameterBinders
		assertThat( sqlTreeWalker.getParameterBinders().size(), is(2) );
		assertThat( sqlTreeWalker.getReturns().size(), is(1) );
	}

	@Test
	public void testSqlTreeWalking4() {
		final String qryStr = "select p.name from Person p where (p.age <= 20 and p.age >= 39)";
		final SqmSelectStatement statement = (SqmSelectStatement) interpret( qryStr );
		final SelectStatementInterpreter interpreter = new SelectStatementInterpreter( queryOptions(), callBack() );
		interpreter.interpret( statement );
		final SelectQuery sqlTree = interpreter.getSelectQuery();
		SqlTreeWalker sqlTreeWalker = new SqlTreeWalker(
				getSessionFactory(),
				buildQueryParameterBindings( statement )
		);
		sqlTreeWalker.visitSelectQuery( sqlTree );


		System.out.println( FormatStyle.BASIC.getFormatter().format( sqlTreeWalker.getSql() ) );

		assertThat( sqlTreeWalker.getSql(), notNullValue() );
		// each literal is transformed into a parameter, so check the number of ParameterBinders
		assertThat( sqlTreeWalker.getParameterBinders().size(), is(2) );
		assertThat( sqlTreeWalker.getReturns().size(), is(1) );
	}

	@Test
	public void testSqlTreeWalking5() {
		final String qryStr = "select p.age from Person p where p.name like 'Steve%'";
		final SqmSelectStatement statement = (SqmSelectStatement) interpret( qryStr );
		final SelectStatementInterpreter interpreter = new SelectStatementInterpreter( queryOptions(), callBack() );
		interpreter.interpret( statement );
		final SelectQuery sqlTree = interpreter.getSelectQuery();
		SqlTreeWalker sqlTreeWalker = new SqlTreeWalker(
				getSessionFactory(),
				buildQueryParameterBindings( statement )
		);
		sqlTreeWalker.visitSelectQuery( sqlTree );


		System.out.println( FormatStyle.BASIC.getFormatter().format( sqlTreeWalker.getSql() ) );

		assertThat( sqlTreeWalker.getSql(), notNullValue() );
		// each literal is transformed into a parameter, so check the number of ParameterBinders
		assertThat( sqlTreeWalker.getParameterBinders().size(), is(1) );
		assertThat( sqlTreeWalker.getReturns().size(), is(1) );
	}

	@Test
	public void testSqlTreeWalking6() {
		final String qryStr = "select p.age from Person p where p.name like 'Steve%' escape '/'";
		final SqmSelectStatement statement = (SqmSelectStatement) interpret( qryStr );
		final SelectStatementInterpreter interpreter = new SelectStatementInterpreter( queryOptions(), callBack() );
		interpreter.interpret( statement );
		final SelectQuery sqlTree = interpreter.getSelectQuery();
		SqlTreeWalker sqlTreeWalker = new SqlTreeWalker(
				getSessionFactory(),
				buildQueryParameterBindings( statement )
		);
		sqlTreeWalker.visitSelectQuery( sqlTree );


		System.out.println( FormatStyle.BASIC.getFormatter().format( sqlTreeWalker.getSql() ) );

		assertThat( sqlTreeWalker.getSql(), notNullValue() );
		// each literal is transformed into a parameter, so check the number of ParameterBinders
		assertThat( sqlTreeWalker.getParameterBinders().size(), is(2) );
		assertThat( sqlTreeWalker.getReturns().size(), is(1) );
	}

	@Test
	public void testSqlTreeWalking7() {
		final String qryStr = "select p.age from Person p where p.name is null";
		final SqmSelectStatement statement = (SqmSelectStatement) interpret( qryStr );
		final SelectStatementInterpreter interpreter = new SelectStatementInterpreter( queryOptions(), callBack() );
		interpreter.interpret( statement );
		final SelectQuery sqlTree = interpreter.getSelectQuery();
		SqlTreeWalker sqlTreeWalker = new SqlTreeWalker(
				getSessionFactory(),
				buildQueryParameterBindings( statement )
		);
		sqlTreeWalker.visitSelectQuery( sqlTree );


		System.out.println( FormatStyle.BASIC.getFormatter().format( sqlTreeWalker.getSql() ) );

		assertThat( sqlTreeWalker.getSql(), notNullValue() );
		// each literal is transformed into a parameter, so check the number of ParameterBinders
		assertThat( sqlTreeWalker.getParameterBinders().size(), is(0) );
		assertThat( sqlTreeWalker.getReturns().size(), is(1) );
	}

	@Test
	public void testSqlTreeWalking8() {
		final String qryStr =  "select p.age from Person p where p.name is not null";
		final SqmSelectStatement statement = (SqmSelectStatement) interpret( qryStr );
		final SelectStatementInterpreter interpreter = new SelectStatementInterpreter( queryOptions(), callBack() );
		interpreter.interpret( statement );
		final SelectQuery sqlTree = interpreter.getSelectQuery();
		SqlTreeWalker sqlTreeWalker = new SqlTreeWalker(
				getSessionFactory(),
				buildQueryParameterBindings( statement )
		);
		sqlTreeWalker.visitSelectQuery( sqlTree );


		System.out.println( FormatStyle.BASIC.getFormatter().format( sqlTreeWalker.getSql() ) );

		assertThat( sqlTreeWalker.getSql(), notNullValue() );
		// each literal is transformed into a parameter, so check the number of ParameterBinders
		assertThat( sqlTreeWalker.getParameterBinders().size(), is(0) );
		assertThat( sqlTreeWalker.getReturns().size(), is(1) );
	}

	@Test
	public void testSqlTreeWalking9() {
		final String qryStr = "select p.age from Person p where not p.name is not null";
		final SqmSelectStatement statement = (SqmSelectStatement) interpret( qryStr );
		final SelectStatementInterpreter interpreter = new SelectStatementInterpreter( queryOptions(), callBack() );
		interpreter.interpret( statement );
		final SelectQuery sqlTree = interpreter.getSelectQuery();
		SqlTreeWalker sqlTreeWalker = new SqlTreeWalker(
				getSessionFactory(),
				buildQueryParameterBindings( statement )
		);
		sqlTreeWalker.visitSelectQuery( sqlTree );


		System.out.println( FormatStyle.BASIC.getFormatter().format( sqlTreeWalker.getSql() ) );

		assertThat( sqlTreeWalker.getSql(), notNullValue() );
		// each literal is transformed into a parameter, so check the number of ParameterBinders
		assertThat( sqlTreeWalker.getParameterBinders().size(), is(0) );
		assertThat( sqlTreeWalker.getReturns().size(), is(1) );
	}

	@Test
	public void testSqlTreeWalking10() {
		final String qryStr = "from Person p";
		final SqmSelectStatement statement = (SqmSelectStatement) interpret( qryStr );
		final SelectStatementInterpreter interpreter = new SelectStatementInterpreter( queryOptions(), callBack() );
		interpreter.interpret( statement );
		final SelectQuery sqlTree = interpreter.getSelectQuery();
		SqlTreeWalker sqlTreeWalker = new SqlTreeWalker(
				getSessionFactory(),
				buildQueryParameterBindings( statement )
		);
		sqlTreeWalker.visitSelectQuery( sqlTree );


		System.out.println( FormatStyle.BASIC.getFormatter().format( sqlTreeWalker.getSql() ) );

		assertThat( sqlTreeWalker.getSql(), notNullValue() );
		assertThat( sqlTreeWalker.getSql(), containsString( "select" ) );
		assertThat( sqlTreeWalker.getSql(), containsString( "p1.id" ) );
		assertThat( sqlTreeWalker.getSql(), containsString( "p1.name" ) );
		assertThat( sqlTreeWalker.getSql(), containsString( "p1.age" ) );
	}

	@Test
	public void testSqlTreeWalking11() {
		final String qryStr = "select a from Person p join p.address a";
		final SqmSelectStatement statement = (SqmSelectStatement) interpret( qryStr );
		final SelectStatementInterpreter interpreter = new SelectStatementInterpreter( queryOptions(), callBack() );
		interpreter.interpret( statement );
		final SelectQuery sqlTree = interpreter.getSelectQuery();
		SqlTreeWalker sqlTreeWalker = new SqlTreeWalker(
				getSessionFactory(),
				buildQueryParameterBindings( statement )
		);
		sqlTreeWalker.visitSelectQuery( sqlTree );


		System.out.println( FormatStyle.BASIC.getFormatter().format( sqlTreeWalker.getSql() ) );

		assertThat( sqlTreeWalker.getSql(), notNullValue() );
		assertThat( sqlTreeWalker.getSql(), containsString( "select" ) );
		assertThat( sqlTreeWalker.getSql(), containsString( "a1.id" ) );
		assertThat( sqlTreeWalker.getSql(), containsString( "a1.street" ) );
	}

	@Test
	public void testSqlTreeWalking12() {
		final String qryStr =  "select r from Person p join p.roles r";
		final SqmSelectStatement statement = (SqmSelectStatement) interpret( qryStr );
		final SelectStatementInterpreter interpreter = new SelectStatementInterpreter( queryOptions(), callBack() );
		interpreter.interpret( statement );
		final SelectQuery sqlTree = interpreter.getSelectQuery();
		SqlTreeWalker sqlTreeWalker = new SqlTreeWalker(
				getSessionFactory(),
				buildQueryParameterBindings( statement )
		);
		sqlTreeWalker.visitSelectQuery( sqlTree );


		System.out.println( FormatStyle.BASIC.getFormatter().format( sqlTreeWalker.getSql() ) );

		assertThat( sqlTreeWalker.getSql(), notNullValue() );
		assertThat( sqlTreeWalker.getSql(), containsString( "select" ) );
		assertThat( sqlTreeWalker.getSql(), containsString( "r1.id" ) );
		assertThat( sqlTreeWalker.getSql(), containsString( "r1.description" ) );
	}

	@Entity(name="Person")
	public static class Person {
		@Id
		Integer id;
		String name;
		int age;

		@ManyToOne
		Address address;

		@OneToMany
		@JoinColumn
		Set<Role> roles = new HashSet<Role>();
	}

	@Entity(name="Address")
	public static class Address {
		@Id
		Integer id;

		String street;
	}

	@Entity(name="Role")
	public static class Role {
		@Id
		Integer id;

		String description;
	}
}
