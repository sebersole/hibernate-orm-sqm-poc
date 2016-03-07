/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.orm.internal.mapping;

import java.util.Map;
import java.util.Set;
import javax.persistence.ElementCollection;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import org.hibernate.boot.MetadataSources;
import org.hibernate.sql.ast.SelectQuery;
import org.hibernate.sql.ast.expression.AttributeReference;
import org.hibernate.sql.ast.expression.Expression;
import org.hibernate.sql.ast.expression.QueryLiteral;
import org.hibernate.sql.ast.predicate.BetweenPredicate;
import org.hibernate.sql.ast.predicate.InListPredicate;
import org.hibernate.sql.gen.BaseUnitTest;

import org.junit.Test;

import org.hamcrest.CoreMatchers;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * @author Steve Ebersole
 */
public class BasicPredicateTest extends BaseUnitTest {
	@Override
	protected void applyMetadataSources(MetadataSources metadataSources) {
		super.applyMetadataSources( metadataSources );
		metadataSources.addAnnotatedClass( Person.class );
		metadataSources.addAnnotatedClass( Name.class );
		metadataSources.addAnnotatedClass( Address.class );
	}

	@Entity( name = "Person" )
	public static class Person {
		@Id
		public Integer id;

		public int age;

		@Embedded
		public Name name;

		@ElementCollection
		public Set<Name> aliases;

		@OneToOne
		public Person partner;

		@OneToMany
		@JoinColumn
		public Map<String,Address> addresses;
	}

	@Embeddable
	public static class Name {
		public String first;
		public String last;
	}

	@Entity( name = "Address" )
	public static class Address {
		@Id
		public Integer id;

		public String street;
	}

	@Test
	public void testBetween() {
		SelectQuery query = interpretSelectQuery( "select p from Person p where p.age between 20 and 39" );

		assertThat( query.getQuerySpec().getWhereClauseRestrictions(), notNullValue() );
		assertThat( query.getQuerySpec().getWhereClauseRestrictions(), instanceOf( BetweenPredicate.class ) );
		BetweenPredicate betweenPredicate = (BetweenPredicate) query.getQuerySpec().getWhereClauseRestrictions();

		assertThat( betweenPredicate.getExpression(), instanceOf( AttributeReference.class ) );
		assertThat( ( (AttributeReference) betweenPredicate.getExpression() ).getReferencedAttribute().getName(), is( "age" ) );

		assertThat( betweenPredicate.getLowerBound(), instanceOf( QueryLiteral.class ) );
		assertThat( ( (QueryLiteral) betweenPredicate.getLowerBound() ).getValue(), CoreMatchers.<Object>is(20) );

		assertThat( betweenPredicate.getUpperBound(), instanceOf( QueryLiteral.class ) );
		assertThat( ( (QueryLiteral) betweenPredicate.getUpperBound() ).getValue(), CoreMatchers.<Object>is(39) );
	}

	@Test
	public void testInLiteralList() {
		SelectQuery query = interpretSelectQuery( "select p from Person p where p.age in (20, 30, 40, 50, 60)" );

		assertThat( query.getQuerySpec().getWhereClauseRestrictions(), notNullValue() );
		assertThat( query.getQuerySpec().getWhereClauseRestrictions(), instanceOf( InListPredicate.class ) );
		InListPredicate inPredicate = (InListPredicate) query.getQuerySpec().getWhereClauseRestrictions();

		assertThat( inPredicate.getTestExpression(), instanceOf( AttributeReference.class ) );
		assertThat( ( (AttributeReference) inPredicate.getTestExpression() ).getReferencedAttribute().getName(), is( "age" ) );

		assertThat( inPredicate.getListExpressions().size(), is( 5) );
		for ( Expression expression : inPredicate.getListExpressions() ) {
			assertThat( expression, instanceOf( QueryLiteral.class ) );
		}
	}
}
