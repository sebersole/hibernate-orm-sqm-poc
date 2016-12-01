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
import org.hibernate.query.proposed.QueryParameter;
import org.hibernate.query.proposed.internal.ParameterMetadataImpl;
import org.hibernate.query.proposed.internal.QueryParameterBindingsImpl;
import org.hibernate.query.proposed.internal.QueryParameterNamedImpl;
import org.hibernate.query.proposed.internal.QueryParameterPositionalImpl;
import org.hibernate.sql.QueryParameterBindingTypeResolverImpl;
import org.hibernate.sql.ast.SelectQuery;
import org.hibernate.query.proposed.spi.QueryParameterBindings;
import org.hibernate.sql.ast.expression.NamedParameter;
import org.hibernate.sql.ast.expression.PositionalParameter;
import org.hibernate.sql.convert.spi.SqmSelectToSqlAstConverter;
import org.hibernate.sql.exec.spi.SqlSelectInterpretation;
import org.hibernate.sql.exec.spi.SqlAstSelectInterpreter;
import org.hibernate.sqm.query.SqmSelectStatement;
import org.hibernate.sqm.query.SqmStatement;

import org.hibernate.testing.FailureExpected;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.instanceOf;
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
		final SqlSelectInterpretation sqlSelectInterpretation = buildSqlSelectInterpretation( "select p.name from Person p" );

		System.out.println( FormatStyle.BASIC.getFormatter().format( sqlSelectInterpretation.getSql() ) );

		assertThat( sqlSelectInterpretation.getSql(), notNullValue() );
		assertThat( sqlSelectInterpretation.getReturns().size(), is(1) );
	}

	private SqlSelectInterpretation buildSqlSelectInterpretation(String queryString) {
		return buildSqlSelectInterpretation( queryString, false );
	}

	private SqlSelectInterpretation buildSqlSelectInterpretation(String queryString, boolean shallow) {
		final SqmSelectStatement statement = (SqmSelectStatement) interpret( queryString );

		final SelectQuery sqlTree = SqmSelectToSqlAstConverter.interpret(
				statement,
				getSessionFactory(),
				getConsumerContext().getDomainMetamodel(),
				queryOptions(),
				callBack()
		);

		return SqlAstSelectInterpreter.interpret(
				sqlTree,
				shallow,
				getSessionFactory(),
				buildQueryParameterBindings( statement )
		);
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
		final SqlSelectInterpretation sqlSelectInterpretation = buildSqlSelectInterpretation( qryStr );

		System.out.println( FormatStyle.BASIC.getFormatter().format( sqlSelectInterpretation.getSql() ) );

		assertThat( sqlSelectInterpretation.getSql(), notNullValue() );
		assertThat( sqlSelectInterpretation.getReturns().size(), is(2) );
	}

	@Test
	public void testSqlTreeWalking3() {
		final String qryStr = "select p.name from Person p where p.age between 20 and 39";
		final SqlSelectInterpretation sqlSelectInterpretation = buildSqlSelectInterpretation( qryStr );


		System.out.println( FormatStyle.BASIC.getFormatter().format( sqlSelectInterpretation.getSql() ) );

		assertThat( sqlSelectInterpretation.getSql(), notNullValue() );
		// each literal is transformed into a parameter, so check the number of ParameterBinders
		assertThat( sqlSelectInterpretation.getParameterBinders().size(), is(2) );
		assertThat( sqlSelectInterpretation.getReturns().size(), is(1) );
	}

	@Test
	public void testSqlTreeWalking4() {
		final String qryStr = "select p.name from Person p where (p.age <= 20 and p.age >= 39)";
		final SqlSelectInterpretation sqlSelectInterpretation = buildSqlSelectInterpretation( qryStr );

		System.out.println( FormatStyle.BASIC.getFormatter().format( sqlSelectInterpretation.getSql() ) );

		assertThat( sqlSelectInterpretation.getSql(), notNullValue() );
		// each literal is transformed into a parameter, so check the number of ParameterBinders
		assertThat( sqlSelectInterpretation.getParameterBinders().size(), is(2) );
		assertThat( sqlSelectInterpretation.getReturns().size(), is(1) );
	}

	@Test
	public void testSqlTreeWalking5() {
		final String qryStr = "select p.age from Person p where p.name like 'Steve%'";
		final SqlSelectInterpretation sqlSelectInterpretation = buildSqlSelectInterpretation( qryStr );


		System.out.println( FormatStyle.BASIC.getFormatter().format( sqlSelectInterpretation.getSql() ) );

		assertThat( sqlSelectInterpretation.getSql(), notNullValue() );
		// each literal is transformed into a parameter, so check the number of ParameterBinders
		assertThat( sqlSelectInterpretation.getParameterBinders().size(), is(1) );
		assertThat( sqlSelectInterpretation.getReturns().size(), is(1) );
	}

	@Test
	public void testSqlTreeWalking6() {
		final String qryStr = "select p.age from Person p where p.name like 'Steve%' escape '/'";
		final SqlSelectInterpretation sqlSelectInterpretation = buildSqlSelectInterpretation( qryStr );

		System.out.println( FormatStyle.BASIC.getFormatter().format( sqlSelectInterpretation.getSql() ) );

		assertThat( sqlSelectInterpretation.getSql(), notNullValue() );
		// each literal is transformed into a parameter, so check the number of ParameterBinders
		assertThat( sqlSelectInterpretation.getParameterBinders().size(), is(2) );
		assertThat( sqlSelectInterpretation.getReturns().size(), is(1) );
	}

	@Test
	public void testSqlTreeWalking7() {
		final String qryStr = "select p.age from Person p where p.name is null";
		final SqlSelectInterpretation sqlSelectInterpretation = buildSqlSelectInterpretation( qryStr );

		System.out.println( FormatStyle.BASIC.getFormatter().format( sqlSelectInterpretation.getSql() ) );

		assertThat( sqlSelectInterpretation.getSql(), notNullValue() );
		// each literal is transformed into a parameter, so check the number of ParameterBinders
		assertThat( sqlSelectInterpretation.getParameterBinders().size(), is(0) );
		assertThat( sqlSelectInterpretation.getReturns().size(), is(1) );
	}

	@Test
	public void testSqlTreeWalking8() {
		final String qryStr =  "select p.age from Person p where p.name is not null";
		final SqlSelectInterpretation sqlSelectInterpretation = buildSqlSelectInterpretation( qryStr );

		System.out.println( FormatStyle.BASIC.getFormatter().format( sqlSelectInterpretation.getSql() ) );

		assertThat( sqlSelectInterpretation.getSql(), notNullValue() );
		// each literal is transformed into a parameter, so check the number of ParameterBinders
		assertThat( sqlSelectInterpretation.getParameterBinders().size(), is(0) );
		assertThat( sqlSelectInterpretation.getReturns().size(), is(1) );
	}

	@Test
	public void testSqlTreeWalking9() {
		final String qryStr = "select p.age from Person p where not p.name is not null";
		final SqlSelectInterpretation sqlSelectInterpretation = buildSqlSelectInterpretation( qryStr );

		System.out.println( FormatStyle.BASIC.getFormatter().format( sqlSelectInterpretation.getSql() ) );

		assertThat( sqlSelectInterpretation.getSql(), notNullValue() );
		// each literal is transformed into a parameter, so check the number of ParameterBinders
		assertThat( sqlSelectInterpretation.getParameterBinders().size(), is(0) );
		assertThat( sqlSelectInterpretation.getReturns().size(), is(1) );
	}

	@Test
	@FailureExpected( jiraKey = "none" )
	public void testSqlTreeWalking10() {
		final String qryStr = "select p from Person p";
		final SqlSelectInterpretation sqlSelectInterpretation = buildSqlSelectInterpretation( qryStr );

		System.out.println( FormatStyle.BASIC.getFormatter().format( sqlSelectInterpretation.getSql() ) );

		assertThat( sqlSelectInterpretation.getSql(), notNullValue() );
		assertThat( sqlSelectInterpretation.getSql(), containsString( "select" ) );
		assertThat( sqlSelectInterpretation.getSql(), containsString( "p1.id" ) );
		assertThat( sqlSelectInterpretation.getSql(), containsString( "p1.name" ) );
		assertThat( sqlSelectInterpretation.getSql(), containsString( "p1.age" ) );
	}

	@Test
	@FailureExpected( jiraKey = "none" )
	public void testSqlTreeWalking11() {
		final String qryStr = "select a from Person p join p.address a";
		final SqlSelectInterpretation sqlSelectInterpretation = buildSqlSelectInterpretation( qryStr );

		System.out.println( FormatStyle.BASIC.getFormatter().format( sqlSelectInterpretation.getSql() ) );

		assertThat( sqlSelectInterpretation.getSql(), notNullValue() );
		assertThat( sqlSelectInterpretation.getSql(), containsString( "select" ) );
		assertThat( sqlSelectInterpretation.getSql(), containsString( "a1.id" ) );
		assertThat( sqlSelectInterpretation.getSql(), containsString( "a1.street" ) );
	}

	@Test
	@FailureExpected( jiraKey = "none" )
	public void testSqlTreeWalking12() {
		final String qryStr =  "select r from Person p join p.roles r";
		final SqlSelectInterpretation sqlSelectInterpretation = buildSqlSelectInterpretation( qryStr );

		System.out.println( FormatStyle.BASIC.getFormatter().format( sqlSelectInterpretation.getSql() ) );

		assertThat( sqlSelectInterpretation.getSql(), notNullValue() );
		assertThat( sqlSelectInterpretation.getSql(), containsString( "select" ) );
		assertThat( sqlSelectInterpretation.getSql(), containsString( "r1.id" ) );
		assertThat( sqlSelectInterpretation.getSql(), containsString( "r1.description" ) );
	}

	@Test
	public void testBasicParameterHandling() {
		{
			final SqlSelectInterpretation sqlSelectInterpretation = buildSqlSelectInterpretation(
					"select p.name from Person p where p.name = ?1" );
			System.out.println( FormatStyle.BASIC.getFormatter().format( sqlSelectInterpretation.getSql() ) );
			assertThat( sqlSelectInterpretation.getParameterBinders().size(), is( 1 ) );
			assertThat(
					sqlSelectInterpretation.getParameterBinders().get( 0 ),
					instanceOf( PositionalParameter.class )
			);
		}

		{
			final SqlSelectInterpretation sqlSelectInterpretation = buildSqlSelectInterpretation(
					"select p.name from Person p where p.name = :name" );
			System.out.println( FormatStyle.BASIC.getFormatter().format( sqlSelectInterpretation.getSql() ) );
			assertThat( sqlSelectInterpretation.getParameterBinders().size(), is( 1 ) );
			assertThat(
					sqlSelectInterpretation.getParameterBinders().get( 0 ),
					instanceOf( NamedParameter.class )
			);
		}
	}

	@Entity(name="Person")
	@SuppressWarnings({"WeakerAccess", "unused"})
	public static class Person {
		@Id
		Integer id;
		String name;
		int age;

		@ManyToOne
		Address address;

		@OneToMany
		@JoinColumn
		Set<Role> roles = new HashSet<>();
	}

	@Entity(name="Address")
	@SuppressWarnings({"WeakerAccess", "unused"})
	public static class Address {
		@Id
		Integer id;

		String street;
	}

	@Entity(name="Role")
	@SuppressWarnings({"WeakerAccess", "unused"})
	public static class Role {
		@Id
		Integer id;

		String description;
	}
}
