/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.orm.internal.mapping;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;

import org.hibernate.boot.MetadataSources;
import org.hibernate.sql.ast.SelectQuery;
import org.hibernate.sql.ast.expression.AttributeReference;
import org.hibernate.sql.ast.expression.ColumnBindingExpression;
import org.hibernate.sql.ast.from.CollectionTableGroup;
import org.hibernate.sql.ast.from.EntityTableGroup;
import org.hibernate.sql.ast.from.TableBinding;
import org.hibernate.sql.ast.from.TableGroup;
import org.hibernate.sql.ast.from.TableGroupJoin;
import org.hibernate.sql.ast.from.TableSpace;
import org.hibernate.sql.ast.predicate.Junction;
import org.hibernate.sql.ast.predicate.Predicate;
import org.hibernate.sql.ast.predicate.RelationalPredicate;
import org.hibernate.sql.ast.select.SelectClause;
import org.hibernate.sql.gen.BaseUnitTest;
import org.hibernate.sql.gen.internal.SelectStatementInterpreter;
import org.hibernate.sqm.query.JoinType;
import org.hibernate.sqm.query.SelectStatement;

import org.junit.Test;

import org.hamcrest.CoreMatchers;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertThat;

/**
 * @author Andrea Boriero
 */
public class TableSpaceGenerationTest extends BaseUnitTest {

	@Test
	public void joinOneToManyNoJoinColumnTest() {
		final TableSpace tableSpace = getTableSpace( "from Person p join p.addresses" );

		final TableGroup rootTableGroup = tableSpace.getRootTableGroup();
		assertThat( rootTableGroup.getTableJoins().size(), is( 0 ) );

		checkRootTableName( "PERSON", rootTableGroup );
		assertThat( rootTableGroup.getRootTableBinding().getIdentificationVariable(), is( "p1" ) );

		assertThat( tableSpace.getJoinedTableGroups().size(), is( 1 ) );

		final TableGroupJoin tableGroupJoin = tableSpace.getJoinedTableGroups().get( 0 );
		assertThat( tableGroupJoin.getJoinType(), is( JoinType.INNER ) );

		final TableGroup joinedGroup = tableGroupJoin.getJoinedGroup();
		assertThat( joinedGroup, is( instanceOf( CollectionTableGroup.class ) ) );

		checkRootTableName( "PERSON_ADDRESS", joinedGroup );
		assertThat( joinedGroup.getRootTableBinding().getIdentificationVariable(), is( "a1" ) );

		// Let's check the join predicate which should join PERSON(id) -> PERSON_ADDRESS(Person_id)
		{
			assertThat( tableGroupJoin.getPredicate(), notNullValue() );
			assertThat( tableGroupJoin.getPredicate(), instanceOf( Junction.class ) );
			final Junction junction = (Junction) tableGroupJoin.getPredicate();
			assertThat( junction.getNature(), is( Junction.Nature.CONJUNCTION ) );
			assertThat( junction.getPredicates().size(), is( 1 ) );
			assertThat( junction.getPredicates().get( 0 ), instanceOf( RelationalPredicate.class ) );
			final RelationalPredicate joinPredicate = (RelationalPredicate) junction.getPredicates().get( 0 );
			assertThat( joinPredicate.getLeftHandExpression(), instanceOf( ColumnBindingExpression.class ) );
			final ColumnBindingExpression joinPredicateLhsColumn = (ColumnBindingExpression) joinPredicate.getLeftHandExpression();
			assertThat( joinPredicateLhsColumn.getColumnBinding().getIdentificationVariable(), is( "p1" ) );
			assertThat(
					( (PhysicalColumn) joinPredicateLhsColumn.getColumnBinding().getColumn() ).getName(),
					is( "id" )
			);

			assertThat( joinPredicate.getRightHandExpression(), instanceOf( ColumnBindingExpression.class ) );
			final ColumnBindingExpression joinPredicateRhsColumn = (ColumnBindingExpression) joinPredicate.getRightHandExpression();
			assertThat( joinPredicateRhsColumn.getColumnBinding().getIdentificationVariable(), is( "a1" ) );
			assertThat(
					( (PhysicalColumn) joinPredicateRhsColumn.getColumnBinding().getColumn() ).getName(),
					is( "Person_id" )
			);
		}

		assertThat( joinedGroup.getTableJoins().size(), is( 1 ) );

		// Let's check the join predicate which should join PERSON_ADDRESS(addresses_id) -> ADDRESS(id)
		{
			assertThat( joinedGroup.getTableJoins().get( 0 ).getJoinPredicate(), notNullValue() );
			final Predicate joinPredicate = joinedGroup.getTableJoins().get( 0 ).getJoinPredicate();
			assertThat( joinPredicate, instanceOf( Junction.class ) );
			final Junction junction = (Junction) joinPredicate;
			assertThat( junction.getNature(), is( Junction.Nature.CONJUNCTION ) );
			assertThat( junction.getPredicates().size(), is( 1 ) );
			assertThat( junction.getPredicates().get( 0 ), instanceOf( RelationalPredicate.class ) );
			final RelationalPredicate singleJoinPredicate = (RelationalPredicate) junction.getPredicates().get( 0 );
			assertThat( singleJoinPredicate.getLeftHandExpression(), instanceOf( ColumnBindingExpression.class ) );
			final ColumnBindingExpression joinPredicateLhsColumn = (ColumnBindingExpression) singleJoinPredicate.getLeftHandExpression();
			assertThat( joinPredicateLhsColumn.getColumnBinding().getIdentificationVariable(), is( "a1" ) );
			assertThat(
					( (PhysicalColumn) joinPredicateLhsColumn.getColumnBinding().getColumn() ).getName(),
					is( "addresses_id" )
			);

			assertThat( singleJoinPredicate.getRightHandExpression(), instanceOf( ColumnBindingExpression.class ) );
			final ColumnBindingExpression joinPredicateRhsColumn = (ColumnBindingExpression) singleJoinPredicate.getRightHandExpression();
			assertThat( joinPredicateRhsColumn.getColumnBinding().getIdentificationVariable(), is( "a1_0" ) );
			assertThat(
					( (PhysicalColumn) joinPredicateRhsColumn.getColumnBinding().getColumn() ).getName(),
					is( "id" )
			);
		}

		final TableBinding joinedTableBinding = joinedGroup.getTableJoins().get( 0 ).getJoinedTableBinding();
		checkTableName( "ADDRESS", joinedTableBinding );
		assertThat( joinedTableBinding.getIdentificationVariable(), is( "a1_0" ) );
	}

	@Test
	public void joinOneToManyWithJoinColumnTest() {
		final TableSpace tableSpace = getTableSpace( "from Person p join p.pastRoles" );

		final TableGroup rootTableGroup = tableSpace.getRootTableGroup();
		assertThat( rootTableGroup.getTableJoins().size(), is( 0 ) );

		checkRootTableName( "PERSON", rootTableGroup );

		assertThat( tableSpace.getJoinedTableGroups().size(), is( 1 ) );

		final TableGroupJoin tableGroupJoin = tableSpace.getJoinedTableGroups().get( 0 );
		assertThat( tableGroupJoin.getJoinType(), is( JoinType.INNER ) );

		final TableGroup joinedGroup = tableGroupJoin.getJoinedGroup();
		assertThat( joinedGroup, is( instanceOf( CollectionTableGroup.class ) ) );

		assertThat( joinedGroup.getTableJoins().size(), is( 0 ) );

		checkRootTableName( "ROLE", joinedGroup );

		// Let's check the join predicate which should join PERSON(id) -> ROLE(person_id)
		{
			assertThat( tableGroupJoin.getPredicate(), notNullValue() );
			final Predicate joinPredicate = tableGroupJoin.getPredicate();
			assertThat( joinPredicate, instanceOf( Junction.class ) );
			final Junction junction = (Junction) joinPredicate;
			assertThat( junction.getNature(), is( Junction.Nature.CONJUNCTION ) );
			assertThat( junction.getPredicates().size(), is( 1 ) );
			assertThat( junction.getPredicates().get( 0 ), instanceOf( RelationalPredicate.class ) );
			final RelationalPredicate singleJoinPredicate = (RelationalPredicate) junction.getPredicates().get( 0 );
			assertThat( singleJoinPredicate.getLeftHandExpression(), instanceOf( ColumnBindingExpression.class ) );
			final ColumnBindingExpression joinPredicateLhsColumn = (ColumnBindingExpression) singleJoinPredicate.getLeftHandExpression();
			assertThat( joinPredicateLhsColumn.getColumnBinding().getIdentificationVariable(), is( "p1" ) );
			assertThat(
					( (PhysicalColumn) joinPredicateLhsColumn.getColumnBinding().getColumn() ).getName(),
					is( "id" )
			);

			assertThat( singleJoinPredicate.getRightHandExpression(), instanceOf( ColumnBindingExpression.class ) );
			final ColumnBindingExpression joinPredicateRhsColumn = (ColumnBindingExpression) singleJoinPredicate.getRightHandExpression();
			assertThat( joinPredicateRhsColumn.getColumnBinding().getIdentificationVariable(), is( "r1" ) );
			assertThat(
					( (PhysicalColumn) joinPredicateRhsColumn.getColumnBinding().getColumn() ).getName(),
					is( "person_id" )
			);
		}
	}

	@Test
	public void joinOneToOneAssociationWithPrimaryKeyJoinColumn() {
		final TableSpace tableSpace = getTableSpace( "from Person p join p.actualRole" );

		final TableGroup rootTableGroup = tableSpace.getRootTableGroup();
		assertThat( rootTableGroup.getTableJoins().size(), is( 0 ) );

		checkRootTableName( "PERSON", rootTableGroup );

		assertThat( tableSpace.getJoinedTableGroups().size(), is( 1 ) );

		final TableGroupJoin tableGroupJoin =
				tableSpace.getJoinedTableGroups().get( 0 );
		assertThat( tableGroupJoin.getJoinType(), is( JoinType.INNER ) );

		final TableGroup joinedGroup = tableGroupJoin.getJoinedGroup();
		assertThat( joinedGroup, is( instanceOf( EntityTableGroup.class ) ) );

		assertThat( joinedGroup.getTableJoins().size(), is( 0 ) );

		checkRootTableName( "ROLE", joinedGroup );


		// Let's check the join predicate which should join PERSON(id) -> Role(id)
		{
			assertThat( tableGroupJoin.getPredicate(), notNullValue() );
			assertThat( tableGroupJoin.getPredicate(), instanceOf( Junction.class ) );
			final Junction junction = (Junction) tableGroupJoin.getPredicate();
			assertThat( junction.getNature(), is( Junction.Nature.CONJUNCTION ) );
			assertThat( junction.getPredicates().size(), is( 1 ) );
			assertThat( junction.getPredicates().get( 0 ), instanceOf( RelationalPredicate.class ) );
			final RelationalPredicate joinPredicate = (RelationalPredicate) junction.getPredicates().get( 0 );
			assertThat( joinPredicate.getLeftHandExpression(), instanceOf( ColumnBindingExpression.class ) );
			final ColumnBindingExpression joinPredicateLhsColumn = (ColumnBindingExpression) joinPredicate.getLeftHandExpression();
			assertThat( joinPredicateLhsColumn.getColumnBinding().getIdentificationVariable(), is( "p1" ) );
			assertThat(
					( (PhysicalColumn) joinPredicateLhsColumn.getColumnBinding().getColumn() ).getName(),
					is( "id" )
			);

			assertThat( joinPredicate.getRightHandExpression(), instanceOf( ColumnBindingExpression.class ) );
			final ColumnBindingExpression joinPredicateRhsColumn = (ColumnBindingExpression) joinPredicate.getRightHandExpression();
			assertThat( joinPredicateRhsColumn.getColumnBinding().getIdentificationVariable(), is( "r1" ) );
			assertThat(
					( (PhysicalColumn) joinPredicateRhsColumn.getColumnBinding().getColumn() ).getName(),
					is( "id" )
			);
		}
	}

	@Test
	public void joinOneToOneAssociationNoPrimaryKeyJoinColumn() {
		final TableSpace tableSpace = getTableSpace( "from Person p join p.actualRole2" );

		final TableGroup rootTableGroup = tableSpace.getRootTableGroup();
		assertThat( rootTableGroup.getTableJoins().size(), is( 0 ) );

		checkRootTableName( "PERSON", rootTableGroup );

		assertThat( tableSpace.getJoinedTableGroups().size(), is( 1 ) );

		final TableGroupJoin tableGroupJoin =
				tableSpace.getJoinedTableGroups().get( 0 );
		assertThat( tableGroupJoin.getJoinType(), is( JoinType.INNER ) );

		final TableGroup joinedGroup = tableGroupJoin.getJoinedGroup();
		assertThat( joinedGroup, is( instanceOf( EntityTableGroup.class ) ) );

		assertThat( joinedGroup.getTableJoins().size(), is( 0 ) );

		checkRootTableName( "ROLE", joinedGroup );


		// Let's check the join predicate which should join PERSON(id) -> Role(id)
		{
			assertThat( tableGroupJoin.getPredicate(), notNullValue() );
			assertThat( tableGroupJoin.getPredicate(), instanceOf( Junction.class ) );
			final Junction junction = (Junction) tableGroupJoin.getPredicate();
			assertThat( junction.getNature(), is( Junction.Nature.CONJUNCTION ) );
			assertThat( junction.getPredicates().size(), is( 1 ) );
			assertThat( junction.getPredicates().get( 0 ), instanceOf( RelationalPredicate.class ) );
			final RelationalPredicate joinPredicate = (RelationalPredicate) junction.getPredicates().get( 0 );
			assertThat( joinPredicate.getLeftHandExpression(), instanceOf( ColumnBindingExpression.class ) );
			final ColumnBindingExpression joinPredicateLhsColumn = (ColumnBindingExpression) joinPredicate.getLeftHandExpression();
			assertThat( joinPredicateLhsColumn.getColumnBinding().getIdentificationVariable(), is( "p1" ) );
			assertThat(
					( (PhysicalColumn) joinPredicateLhsColumn.getColumnBinding().getColumn() ).getName(),
					is( "actualRole2_id" )
			);

			assertThat( joinPredicate.getRightHandExpression(), instanceOf( ColumnBindingExpression.class ) );
			final ColumnBindingExpression joinPredicateRhsColumn = (ColumnBindingExpression) joinPredicate.getRightHandExpression();
			assertThat( joinPredicateRhsColumn.getColumnBinding().getIdentificationVariable(), is( "r1" ) );
			assertThat(
					( (PhysicalColumn) joinPredicateRhsColumn.getColumnBinding().getColumn() ).getName(),
					is( "id" )
			);
		}
	}

	@Test
	public void joinManyToOneAssociationWithJoinColumn() {
		final TableSpace tableSpace = getTableSpace( "from Person p join p.nextRole" );

		final TableGroup rootTableGroup = tableSpace.getRootTableGroup();
		assertThat( rootTableGroup.getTableJoins().size(), is( 0 ) );

		checkRootTableName( "PERSON", rootTableGroup );

		assertThat( tableSpace.getJoinedTableGroups().size(), is( 1 ) );

		final TableGroupJoin tableGroupJoin =
				tableSpace.getJoinedTableGroups().get( 0 );
		assertThat( tableGroupJoin.getJoinType(), is( JoinType.INNER ) );

		final TableGroup joinedGroup = tableGroupJoin.getJoinedGroup();
		assertThat( joinedGroup, is( instanceOf( EntityTableGroup.class ) ) );

		assertThat( joinedGroup.getTableJoins().size(), is( 0 ) );

		checkRootTableName( "ROLE", joinedGroup );


		// Let's check the join predicate which should join PERSON(nextRole_id) -> Role(id)
		{
			assertThat( tableGroupJoin.getPredicate(), notNullValue() );
			assertThat( tableGroupJoin.getPredicate(), instanceOf( Junction.class ) );
			final Junction junction = (Junction) tableGroupJoin.getPredicate();
			assertThat( junction.getNature(), is( Junction.Nature.CONJUNCTION ) );
			assertThat( junction.getPredicates().size(), is( 1 ) );
			assertThat( junction.getPredicates().get( 0 ), instanceOf( RelationalPredicate.class ) );
			final RelationalPredicate joinPredicate = (RelationalPredicate) junction.getPredicates().get( 0 );
			assertThat( joinPredicate.getLeftHandExpression(), instanceOf( ColumnBindingExpression.class ) );
			final ColumnBindingExpression joinPredicateLhsColumn = (ColumnBindingExpression) joinPredicate.getLeftHandExpression();
			assertThat( joinPredicateLhsColumn.getColumnBinding().getIdentificationVariable(), is( "p1" ) );
			assertThat(
					( (PhysicalColumn) joinPredicateLhsColumn.getColumnBinding().getColumn() ).getName(),
					is( "nextRole_id" )
			);

			assertThat( joinPredicate.getRightHandExpression(), instanceOf( ColumnBindingExpression.class ) );
			final ColumnBindingExpression joinPredicateRhsColumn = (ColumnBindingExpression) joinPredicate.getRightHandExpression();
			assertThat( joinPredicateRhsColumn.getColumnBinding().getIdentificationVariable(), is( "r1" ) );
			assertThat(
					( (PhysicalColumn) joinPredicateRhsColumn.getColumnBinding().getColumn() ).getName(),
					is( "id" )
			);
		}
	}

	@Test
	public void joinManyToOneAssociationNoJoinColumn() {
		final TableSpace tableSpace = getTableSpace( "from Person p join p.lastRole" );

		final TableGroup rootTableGroup = tableSpace.getRootTableGroup();
		assertThat( rootTableGroup.getTableJoins().size(), is( 0 ) );

		checkRootTableName( "PERSON", rootTableGroup );

		assertThat( tableSpace.getJoinedTableGroups().size(), is( 1 ) );

		final TableGroupJoin tableGroupJoin =
				tableSpace.getJoinedTableGroups().get( 0 );
		assertThat( tableGroupJoin.getJoinType(), is( JoinType.INNER ) );

		final TableGroup joinedGroup = tableGroupJoin.getJoinedGroup();
		assertThat( joinedGroup, is( instanceOf( EntityTableGroup.class ) ) );

		assertThat( joinedGroup.getTableJoins().size(), is( 0 ) );

		checkRootTableName( "ROLE", joinedGroup );


		// Let's check the join predicate which should join PERSON(lastRole_id) -> Role(id)
		{
			assertThat( tableGroupJoin.getPredicate(), notNullValue() );
			assertThat( tableGroupJoin.getPredicate(), instanceOf( Junction.class ) );
			final Junction junction = (Junction) tableGroupJoin.getPredicate();
			assertThat( junction.getNature(), is( Junction.Nature.CONJUNCTION ) );
			assertThat( junction.getPredicates().size(), is( 1 ) );
			assertThat( junction.getPredicates().get( 0 ), instanceOf( RelationalPredicate.class ) );
			final RelationalPredicate joinPredicate = (RelationalPredicate) junction.getPredicates().get( 0 );
			assertThat( joinPredicate.getLeftHandExpression(), instanceOf( ColumnBindingExpression.class ) );
			final ColumnBindingExpression joinPredicateLhsColumn = (ColumnBindingExpression) joinPredicate.getLeftHandExpression();
			assertThat( joinPredicateLhsColumn.getColumnBinding().getIdentificationVariable(), is( "p1" ) );
			assertThat(
					( (PhysicalColumn) joinPredicateLhsColumn.getColumnBinding().getColumn() ).getName(),
					is( "lastRole_id" )
			);

			assertThat( joinPredicate.getRightHandExpression(), instanceOf( ColumnBindingExpression.class ) );
			final ColumnBindingExpression joinPredicateRhsColumn = (ColumnBindingExpression) joinPredicate.getRightHandExpression();
			assertThat( joinPredicateRhsColumn.getColumnBinding().getIdentificationVariable(), is( "r1" ) );
			assertThat(
					( (PhysicalColumn) joinPredicateRhsColumn.getColumnBinding().getColumn() ).getName(),
					is( "id" )
			);
		}
	}

	@Test
	public void crossJoinTest() {
		final TableSpace tableSpace = getTableSpace( "from Person cross join Role" );

		final TableGroup rootTableGroup = tableSpace.getRootTableGroup();
		assertThat( rootTableGroup.getTableJoins().size(), is( 0 ) );

		checkRootTableName( "PERSON", rootTableGroup );

		assertThat( tableSpace.getJoinedTableGroups().size(), is( 1 ) );

		final TableGroupJoin tableGroupJoin = tableSpace.getJoinedTableGroups().get( 0 );
		assertThat( tableGroupJoin.getJoinType(), is( JoinType.CROSS ) );

		final TableGroup joinedGroup = tableGroupJoin.getJoinedGroup();
		assertThat( joinedGroup, is( instanceOf( EntityTableGroup.class ) ) );

		assertThat( joinedGroup.getTableJoins().size(), is( 0 ) );

		checkRootTableName( "ROLE", joinedGroup );
	}

	@Test
	public void testSimpleAttributeReference() {
		final SelectStatement statement = (SelectStatement) interpret( "select p.email from Person p" );

		final SelectStatementInterpreter interpreter = new SelectStatementInterpreter( queryOption(), callBack() );
		interpreter.interpret( statement );

		final SelectClause selectClause = interpreter.getSelectQuery().getQuerySpec().getSelectClause();

		assertThat( selectClause.getSelections().size(), is(1) );
		assertThat( selectClause.getSelections().get( 0 ).getResultVariable(), startsWith( "<gen:" ) );
		assertThat( selectClause.getSelections().get( 0 ).getSelectExpression(), instanceOf( AttributeReference.class ) );
	}

	@Test
	public void testSimpleEmbeddedDereference() {
		final SelectStatement statement = (SelectStatement) interpret( "select p.name.first from Person p" );

		final SelectStatementInterpreter interpreter = new SelectStatementInterpreter( queryOption(), callBack() );
		interpreter.interpret( statement );

		final SelectClause selectClause = interpreter.getSelectQuery().getQuerySpec().getSelectClause();

		assertThat( selectClause.getSelections().size(), is(1) );
		assertThat( selectClause.getSelections().get( 0 ).getResultVariable(), startsWith( "<gen:" ) );
		assertThat( selectClause.getSelections().get( 0 ).getSelectExpression(), instanceOf( AttributeReference.class ) );
		final AttributeReference attributeReference = (AttributeReference) selectClause.getSelections().get( 0 ).getSelectExpression();
		assertThat( attributeReference.getReferencedAttribute().getName(), is("first") );
		assertThat( attributeReference.getColumnBindings().length, is(1) );
		assertThat( attributeReference.getColumnBindings()[0].getColumn(), CoreMatchers.instanceOf( PhysicalColumn.class ) );
		final PhysicalColumn column = (PhysicalColumn) attributeReference.getColumnBindings()[0].getColumn();
		assertThat( column.getName(), is( "first" ) );
	}

	@Test
	public void testSelectEmbedded() {
		final SelectStatement statement = (SelectStatement) interpret( "select p.name from Person p" );

		final SelectStatementInterpreter interpreter = new SelectStatementInterpreter( queryOption(), callBack() );
		interpreter.interpret( statement );

		final SelectClause selectClause = interpreter.getSelectQuery().getQuerySpec().getSelectClause();

		assertThat( selectClause.getSelections().size(), is(1) );
		assertThat( selectClause.getSelections().get( 0 ).getResultVariable(), startsWith( "<gen:" ) );
		assertThat( selectClause.getSelections().get( 0 ).getSelectExpression(), instanceOf( AttributeReference.class ) );
		final AttributeReference attributeReference = (AttributeReference) selectClause.getSelections().get( 0 ).getSelectExpression();
		assertThat( attributeReference.getReferencedAttribute().getName(), is("name") );
		assertThat( attributeReference.getColumnBindings().length, is(2) );
		assertThat( attributeReference.getColumnBindings()[0].getColumn(), CoreMatchers.instanceOf( PhysicalColumn.class ) );
		PhysicalColumn column = (PhysicalColumn) attributeReference.getColumnBindings()[0].getColumn();
		assertThat( column.getName(), is( "first" ) );
		assertThat( attributeReference.getColumnBindings()[1].getColumn(), CoreMatchers.instanceOf( PhysicalColumn.class ) );
		column = (PhysicalColumn) attributeReference.getColumnBindings()[1].getColumn();
		assertThat( column.getName(), is( "last" ) );
	}

	@Override
	protected void applyMetadataSources(MetadataSources metadataSources) {
		metadataSources.addAnnotatedClass( Person.class );
		metadataSources.addAnnotatedClass( Address.class );
		metadataSources.addAnnotatedClass( Role.class );
	}

	@Entity(name = "Person")
	@javax.persistence.Table(name = "PERSON")
	public static class Person {
		@Id
		@GeneratedValue
		long id;

		@Embedded
		Name name;

		String email;

		@OneToMany
		Set<Address> addresses = new HashSet<Address>();

		@OneToMany
		@JoinColumn(name = "person_id")
		Set<Role> pastRoles = new HashSet<Role>();

		@OneToOne
		@PrimaryKeyJoinColumn
		Role actualRole;

		@OneToOne
		Role actualRole2;

		@ManyToOne
		Role lastRole;

		@ManyToOne
		@JoinColumn
		Role nextRole;
	}

	@Embeddable
	public static class Name {
		String first;
		String last;
	}

	@Entity(name = "Address")
	@javax.persistence.Table(name = "ADDRESS")
	public static class Address {
		@Id
		@GeneratedValue
		long id;
	}

	@Entity(name = "Role")
	@javax.persistence.Table(name = "ROLE")
	public static class Role {
		@Id
		@GeneratedValue
		long id;
	}

	private TableSpace getTableSpace(String query) {
		final SelectQuery selectQuery = interpretSelectQuery( query );
		final List<TableSpace> tableSpaces = selectQuery.getQuerySpec().getFromClause().getTableSpaces();
		assertThat( tableSpaces.size(), is( 1 ) );

		return tableSpaces.get( 0 );
	}

	private void checkRootTableName(String expectedTableName, TableGroup tableGroup) {
		final TableBinding tableBinding = tableGroup.getRootTableBinding();
		checkTableName( expectedTableName, tableBinding );
	}

	private void checkTableName(String expectedTableName, TableBinding tableBinding) {
		assertThat( tableBinding.getTable(), is( instanceOf( PhysicalTable.class ) ) );
		assertThat( ((PhysicalTable) tableBinding.getTable() ).getTableName(), is( expectedTableName ) );
	}
}