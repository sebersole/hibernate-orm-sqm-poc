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
import org.hibernate.sql.ast.expression.AvgFunction;
import org.hibernate.sql.ast.expression.BinaryArithmeticExpression;
import org.hibernate.sql.ast.expression.CountFunction;
import org.hibernate.sql.ast.expression.CountStarFunction;
import org.hibernate.sql.ast.expression.Expression;
import org.hibernate.sql.ast.expression.MaxFunction;
import org.hibernate.sql.ast.expression.MinFunction;
import org.hibernate.sql.ast.expression.NamedParameter;
import org.hibernate.sql.ast.expression.QueryLiteral;
import org.hibernate.sql.ast.expression.SumFunction;
import org.hibernate.sql.ast.predicate.BetweenPredicate;
import org.hibernate.sql.ast.predicate.InListPredicate;
import org.hibernate.sql.ast.predicate.Junction;
import org.hibernate.sql.ast.predicate.LikePredicate;
import org.hibernate.sql.ast.predicate.RelationalPredicate;
import org.hibernate.sql.ast.select.Selection;
import org.hibernate.sql.gen.BaseUnitTest;

import org.junit.Test;

import org.hamcrest.CoreMatchers;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;

/**
 * @author Steve Ebersole
 */
public class BasicPredicateAndExpressionTest extends BaseUnitTest {
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
	public void testBetweenLiterals() {
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
	public void testBetweenParams() {
		SelectQuery query = interpretSelectQuery( "select p from Person p where p.age between :lower and :upper" );

		assertThat( query.getQuerySpec().getWhereClauseRestrictions(), notNullValue() );
		assertThat( query.getQuerySpec().getWhereClauseRestrictions(), instanceOf( BetweenPredicate.class ) );
		BetweenPredicate betweenPredicate = (BetweenPredicate) query.getQuerySpec().getWhereClauseRestrictions();

		assertThat( betweenPredicate.getExpression(), instanceOf( AttributeReference.class ) );
		assertThat( ( (AttributeReference) betweenPredicate.getExpression() ).getReferencedAttribute().getName(), is( "age" ) );

		assertThat( betweenPredicate.getLowerBound(), instanceOf( NamedParameter.class ) );

		assertThat( betweenPredicate.getUpperBound(), instanceOf( NamedParameter.class ) );
	}

	@Test
	public void testInListOfLiterals() {
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

	@Test
	public void testInListOfParameters() {
		SelectQuery query = interpretSelectQuery( "select p from Person p where p.age in (:first, :second, :third)" );

		assertThat( query.getQuerySpec().getWhereClauseRestrictions(), notNullValue() );
		assertThat( query.getQuerySpec().getWhereClauseRestrictions(), instanceOf( InListPredicate.class ) );
		InListPredicate inPredicate = (InListPredicate) query.getQuerySpec().getWhereClauseRestrictions();

		assertThat( inPredicate.getTestExpression(), instanceOf( AttributeReference.class ) );
		assertThat( ( (AttributeReference) inPredicate.getTestExpression() ).getReferencedAttribute().getName(), is( "age" ) );

		assertThat( inPredicate.getListExpressions().size(), is( 3 ) );
		for ( Expression expression : inPredicate.getListExpressions() ) {
			assertThat( expression, instanceOf( NamedParameter.class ) );
		}
	}

	@Test
	public void testLike() {
		SelectQuery query = interpretSelectQuery( "select p from Person p where p.name.last like :name" );

		assertThat( query.getQuerySpec().getWhereClauseRestrictions(), notNullValue() );
		assertThat( query.getQuerySpec().getWhereClauseRestrictions(), instanceOf( LikePredicate.class ) );
		LikePredicate likePredicate = (LikePredicate) query.getQuerySpec().getWhereClauseRestrictions();

		assertThat( likePredicate.getMatchExpression(), instanceOf( AttributeReference.class ) );

		assertThat( likePredicate.getPattern(), instanceOf( NamedParameter.class ) );
	}

	@Test
	public void testNamedParameterCreatesUniqueInstances() {
		SelectQuery query = interpretSelectQuery( "select p from Person p where p.name.last = :name or p.name.last = :name" );

		assertThat( query.getQuerySpec().getWhereClauseRestrictions(), notNullValue() );
		assertThat( query.getQuerySpec().getWhereClauseRestrictions(), instanceOf( Junction.class ) );
		Junction mainPredicate = (Junction) query.getQuerySpec().getWhereClauseRestrictions();

		assertThat( mainPredicate.getPredicates().size(), is(2) );

		RelationalPredicate firstPredicate = (RelationalPredicate) mainPredicate.getPredicates().get( 0);
		assertThat( firstPredicate.getRightHandExpression(), instanceOf( NamedParameter.class ) );
		NamedParameter firstParamInstance = (NamedParameter) firstPredicate.getRightHandExpression();

		RelationalPredicate secondPredicate = (RelationalPredicate) mainPredicate.getPredicates().get(1);
		assertThat( secondPredicate.getRightHandExpression(), instanceOf( NamedParameter.class ) );
		NamedParameter secondParamInstance = (NamedParameter) secondPredicate.getRightHandExpression();

		assertThat( firstParamInstance, not( sameInstance( secondParamInstance ) ) );
	}

	@Test
	public void testBinaryArithmeticInSelect() {
		SelectQuery query = interpretSelectQuery( "select p.age+1 from Person p" );

		assertThat( query.getQuerySpec().getSelectClause().getSelections().size(), is(1) );
		Selection selection = query.getQuerySpec().getSelectClause().getSelections().get(0);

		assertThat( selection.getSelectExpression(), instanceOf( BinaryArithmeticExpression.class ) );
		BinaryArithmeticExpression expression = (BinaryArithmeticExpression) selection.getSelectExpression();
		assertThat( expression.getOperation(), is( BinaryArithmeticExpression.Operation.ADD ) );

		assertThat( expression.getLeftHandOperand(), instanceOf( AttributeReference.class ) );
		assertThat( expression.getRightHandOperand(), instanceOf( QueryLiteral.class ) );
	}

	@Test
	public void testAvgInSelect() {
		SelectQuery query = interpretSelectQuery( "select avg(p.age) from Person p" );

		assertThat( query.getQuerySpec().getSelectClause().getSelections().size(), is(1) );
		Selection selection = query.getQuerySpec().getSelectClause().getSelections().get(0);

		assertThat( selection.getSelectExpression(), instanceOf( AvgFunction.class ) );
		AvgFunction expression = (AvgFunction) selection.getSelectExpression();
		assertThat( expression.getArgument(), instanceOf( AttributeReference.class ) );
	}

	@Test
	public void testCountInSelect() {
		SelectQuery query = interpretSelectQuery( "select count(p.age) from Person p" );

		assertThat( query.getQuerySpec().getSelectClause().getSelections().size(), is(1) );
		Selection selection = query.getQuerySpec().getSelectClause().getSelections().get(0);

		assertThat( selection.getSelectExpression(), instanceOf( CountFunction.class ) );
		CountFunction expression = (CountFunction) selection.getSelectExpression();
		assertThat( expression.getArgument(), instanceOf( AttributeReference.class ) );
	}

	@Test
	public void testCountStarInSelect() {
		SelectQuery query = interpretSelectQuery( "select count(*) from Person p" );

		assertThat( query.getQuerySpec().getSelectClause().getSelections().size(), is(1) );
		Selection selection = query.getQuerySpec().getSelectClause().getSelections().get(0);

		assertThat( selection.getSelectExpression(), instanceOf( CountStarFunction.class ) );
	}

	@Test
	public void testMaxInSelect() {
		SelectQuery query = interpretSelectQuery( "select max(p.age) from Person p" );

		assertThat( query.getQuerySpec().getSelectClause().getSelections().size(), is(1) );
		Selection selection = query.getQuerySpec().getSelectClause().getSelections().get(0);

		assertThat( selection.getSelectExpression(), instanceOf( MaxFunction.class ) );
		MaxFunction expression = (MaxFunction) selection.getSelectExpression();
		assertThat( expression.getArgument(), instanceOf( AttributeReference.class ) );
	}

	@Test
	public void testMinInSelect() {
		SelectQuery query = interpretSelectQuery( "select min(p.age) from Person p" );

		assertThat( query.getQuerySpec().getSelectClause().getSelections().size(), is(1) );
		Selection selection = query.getQuerySpec().getSelectClause().getSelections().get(0);

		assertThat( selection.getSelectExpression(), instanceOf( MinFunction.class ) );
		MinFunction expression = (MinFunction) selection.getSelectExpression();
		assertThat( expression.getArgument(), instanceOf( AttributeReference.class ) );
	}

	@Test
	public void testSumInSelect() {
		SelectQuery query = interpretSelectQuery( "select sum(p.age) from Person p" );

		assertThat( query.getQuerySpec().getSelectClause().getSelections().size(), is(1) );
		Selection selection = query.getQuerySpec().getSelectClause().getSelections().get(0);

		assertThat( selection.getSelectExpression(), instanceOf( SumFunction.class ) );
		SumFunction expression = (SumFunction) selection.getSelectExpression();
		assertThat( expression.getArgument(), instanceOf( AttributeReference.class ) );
	}
}
