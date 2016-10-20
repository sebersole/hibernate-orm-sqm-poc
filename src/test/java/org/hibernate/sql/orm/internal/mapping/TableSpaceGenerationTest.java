/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.orm.internal.mapping;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.boot.MetadataSources;
import org.hibernate.persister.common.internal.PhysicalColumn;
import org.hibernate.persister.common.internal.PhysicalTable;
import org.hibernate.sql.ast.SelectQuery;
import org.hibernate.sql.ast.expression.AttributeReference;
import org.hibernate.sql.ast.expression.ColumnBindingExpression;
import org.hibernate.sql.ast.expression.Expression;
import org.hibernate.sql.ast.from.AbstractTableGroup;
import org.hibernate.sql.ast.from.CollectionTableGroup;
import org.hibernate.sql.ast.from.EntityTableGroup;
import org.hibernate.sql.ast.from.TableBinding;
import org.hibernate.sql.ast.from.TableGroup;
import org.hibernate.sql.ast.from.TableGroupJoin;
import org.hibernate.sql.ast.from.TableJoin;
import org.hibernate.sql.ast.from.TableSpace;
import org.hibernate.sql.ast.predicate.Junction;
import org.hibernate.sql.ast.predicate.Predicate;
import org.hibernate.sql.ast.predicate.RelationalPredicate;
import org.hibernate.sql.ast.select.SelectClause;
import org.hibernate.sql.convert.spi.SelectStatementInterpreter;
import org.hibernate.sql.gen.BaseUnitTest;
import org.hibernate.sqm.query.JoinType;
import org.hibernate.sqm.query.SqmSelectStatement;

import org.junit.Test;

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

		final TableGroup joinedGroup = checkTableGroupJoin(
				tableGroupJoin,
				JoinType.INNER,
				CollectionTableGroup.class,
				"PERSON_ADDRESS",
				"a1"
		);

		// Let's check the join predicate which should join PERSON(id) -> PERSON_ADDRESS(Person_id)
		{
			checkConjunctionPredicateWithOneRelationPredicate(
					getJunctionJoinPredicate( tableGroupJoin ),
					"p1",
					"id",
					"a1",
					"Person_id"
			);
		}

		assertThat( joinedGroup.getTableJoins().size(), is( 1 ) );

		// Let's check the join predicate which should join PERSON_ADDRESS(addresses_id) -> ADDRESS(id)
		{
			checkConjunctionPredicateWithOneRelationPredicate(
					getJunctionJoinPredicate( joinedGroup.getTableJoins().get( 0 ) ),
					"a1",
					"addresses_id",
					"a1_0",
					"id"
			);
		}

		final TableBinding joinedTableBinding = joinedGroup.getTableJoins().get( 0 ).getJoinedTableBinding();
		checkTableName( "ADDRESS", joinedTableBinding );
		assertThat( joinedTableBinding.getIdentificationVariable(), is( "a1_0" ) );
	}

	@Test
	public void joinOneToManyNoJoinColumnJoinManyToOneTest() {
		final TableSpace tableSpace = getTableSpace( "from Person p join p.addresses a join a.code" );

		final TableGroup rootTableGroup = tableSpace.getRootTableGroup();
		assertThat( rootTableGroup.getTableJoins().size(), is( 0 ) );

		checkRootTableName( "PERSON", rootTableGroup );
		assertThat( rootTableGroup.getRootTableBinding().getIdentificationVariable(), is( "p1" ) );

		assertThat( tableSpace.getJoinedTableGroups().size(), is( 2 ) );
		// Let's check the first TableGroupJoin
		{
			final TableGroupJoin tableGroupJoin = tableSpace.getJoinedTableGroups().get( 0 );
			checkTableGroupJoin( tableGroupJoin, JoinType.INNER, CollectionTableGroup.class, "PERSON_ADDRESS", "a1" );

			// Let's check the join predicate which should join PERSON(id) -> PERSON_ADDRESS(Person_id)
			{
				checkConjunctionPredicateWithOneRelationPredicate(
						getJunctionJoinPredicate( tableGroupJoin ),
						"p1",
						"id",
						"a1",
						"Person_id"
				);
			}

			final TableGroup joinedGroup = tableGroupJoin.getJoinedGroup();
			assertThat( joinedGroup.getTableJoins().size(), is( 1 ) );

			// Let's check the join predicate which should join PERSON_ADDRESS(addresses_id) -> ADDRESS(id)
			{
				checkConjunctionPredicateWithOneRelationPredicate(
						getJunctionJoinPredicate( joinedGroup.getTableJoins().get( 0 ) ),
						"a1",
						"addresses_id",
						"a1_0",
						"id"
				);
			}

			final TableBinding joinedTableBinding = joinedGroup.getTableJoins().get( 0 ).getJoinedTableBinding();
			checkTableName( "ADDRESS", joinedTableBinding );
			assertThat( joinedTableBinding.getIdentificationVariable(), is( "a1_0" ) );
		}

		// Let's check the second TableGroupJoin is the ManyToOne between ADDRESS -> ZIPCODE
		{
			final TableGroupJoin tableGroupJoin2 = tableSpace.getJoinedTableGroups().get( 1 );
			checkTableGroupJoin( tableGroupJoin2, JoinType.INNER, EntityTableGroup.class, "ZIP_CODE", "z1" );

			// Let's check the join predicate which should join ADDRESS(code_id) -> ZIP_CODE(id)
			{
				checkConjunctionPredicateWithOneRelationPredicate(
						getJunctionJoinPredicate( tableGroupJoin2 ),
						"a1_0",
						"code_id",
						"z1",
						"id"
				);
			}


			final TableGroup joinedGroup = tableGroupJoin2.getJoinedGroup();
			assertThat( joinedGroup.getTableJoins().size(), is( 0 ) );
		}
	}

	@Test
	public void joinOneToManyWithJoinColumnTest() {
		final TableSpace tableSpace = getTableSpace( "from Person p join p.pastRoles" );

		final TableGroup rootTableGroup = tableSpace.getRootTableGroup();
		assertThat( rootTableGroup.getTableJoins().size(), is( 0 ) );

		checkRootTableName( "PERSON", rootTableGroup );

		assertThat( tableSpace.getJoinedTableGroups().size(), is( 1 ) );

		final TableGroupJoin tableGroupJoin = tableSpace.getJoinedTableGroups().get( 0 );
		checkTableGroupJoin( tableGroupJoin, JoinType.INNER, CollectionTableGroup.class, "ROLE", "r1" );

		// Let's check the join predicate which should join PERSON(id) -> ROLE(person_id)
		{
			checkConjunctionPredicateWithOneRelationPredicate(
					getJunctionJoinPredicate( tableGroupJoin ),
					"p1",
					"id",
					"r1",
					"person_id"
			);
		}

		final TableGroup joinedGroup = tableGroupJoin.getJoinedGroup();
		assertThat( joinedGroup.getTableJoins().size(), is( 0 ) );
	}

	@Test
	public void joinOneToOneAssociationWithPrimaryKeyJoinColumn() {
		final TableSpace tableSpace = getTableSpace( "from Person p join p.actualRole" );

		final TableGroup rootTableGroup = tableSpace.getRootTableGroup();
		assertThat( rootTableGroup.getTableJoins().size(), is( 0 ) );

		checkRootTableName( "PERSON", rootTableGroup );

		assertThat( tableSpace.getJoinedTableGroups().size(), is( 1 ) );

		final TableGroupJoin tableGroupJoin = tableSpace.getJoinedTableGroups().get( 0 );
		checkTableGroupJoin( tableGroupJoin, JoinType.INNER, EntityTableGroup.class, "ROLE", "r1" );

		// Let's check the join predicate which should join PERSON(id) -> Role(id)
		{
			checkConjunctionPredicateWithOneRelationPredicate(
					getJunctionJoinPredicate( tableGroupJoin ),
					"p1",
					"id",
					"r1",
					"id"
			);
		}

		final TableGroup joinedGroup = tableGroupJoin.getJoinedGroup();
		assertThat( joinedGroup.getTableJoins().size(), is( 0 ) );
	}

	@Test
	public void joinOneToOneAssociationNoPrimaryKeyJoinColumn() {
		final TableSpace tableSpace = getTableSpace( "from Person p join p.actualRole2" );

		final TableGroup rootTableGroup = tableSpace.getRootTableGroup();
		assertThat( rootTableGroup.getTableJoins().size(), is( 0 ) );

		checkRootTableName( "PERSON", rootTableGroup );

		assertThat( tableSpace.getJoinedTableGroups().size(), is( 1 ) );

		final TableGroupJoin tableGroupJoin = tableSpace.getJoinedTableGroups().get( 0 );
		checkTableGroupJoin( tableGroupJoin, JoinType.INNER, EntityTableGroup.class, "ROLE", "r1" );

		// Let's check the join predicate which should join PERSON(id) -> Role(id)
		{
			checkConjunctionPredicateWithOneRelationPredicate(
					getJunctionJoinPredicate( tableGroupJoin ),
					"p1",
					"actualRole2_id",
					"r1",
					"id"
			);
		}

		final TableGroup joinedGroup = tableGroupJoin.getJoinedGroup();
		assertThat( joinedGroup.getTableJoins().size(), is( 0 ) );
	}

	@Test
	public void joinManyToOneAssociationWithJoinColumn() {
		final TableSpace tableSpace = getTableSpace( "from Person p join p.nextRole" );

		final TableGroup rootTableGroup = tableSpace.getRootTableGroup();
		assertThat( rootTableGroup.getTableJoins().size(), is( 0 ) );

		checkRootTableName( "PERSON", rootTableGroup );

		final TableGroupJoin tableGroupJoin = tableSpace.getJoinedTableGroups().get( 0 );
		checkTableGroupJoin( tableGroupJoin, JoinType.INNER, EntityTableGroup.class, "ROLE", "r1" );

		// Let's check the join predicate which should join PERSON(nextRole_id) -> Role(id)
		{
			checkConjunctionPredicateWithOneRelationPredicate(
					getJunctionJoinPredicate( tableGroupJoin ),
					"p1",
					"nextRole_id",
					"r1",
					"id"
			);
		}

		final TableGroup joinedGroup = tableGroupJoin.getJoinedGroup();
		assertThat( joinedGroup.getTableJoins().size(), is( 0 ) );
	}

	@Test
	public void joinManyToOneAssociationNoJoinColumn() {
		final TableSpace tableSpace = getTableSpace( "from Person p join p.lastRole" );

		final TableGroup rootTableGroup = tableSpace.getRootTableGroup();
		assertThat( rootTableGroup.getTableJoins().size(), is( 0 ) );

		checkRootTableName( "PERSON", rootTableGroup );

		final TableGroupJoin tableGroupJoin = tableSpace.getJoinedTableGroups().get( 0 );
		checkTableGroupJoin( tableGroupJoin, JoinType.INNER, EntityTableGroup.class, "ROLE", "r1" );

		// Let's check the join predicate which should join PERSON(lastRole_id) -> Role(id)
		{
			checkConjunctionPredicateWithOneRelationPredicate(
					getJunctionJoinPredicate( tableGroupJoin ),
					"p1",
					"lastRole_id",
					"r1",
					"id"
			);
		}

		final TableGroup joinedGroup = tableGroupJoin.getJoinedGroup();
		assertThat( joinedGroup.getTableJoins().size(), is( 0 ) );
	}

	@Test
	public void crossJoinTest() {
		final TableSpace tableSpace = getTableSpace( "from Person cross join Role" );

		final TableGroup rootTableGroup = tableSpace.getRootTableGroup();
		assertThat( rootTableGroup.getTableJoins().size(), is( 0 ) );

		checkRootTableName( "PERSON", rootTableGroup );

		final TableGroupJoin tableGroupJoin = tableSpace.getJoinedTableGroups().get( 0 );
		checkTableGroupJoin( tableGroupJoin, JoinType.CROSS, EntityTableGroup.class, "ROLE", "r1" );

		final TableGroup joinedGroup = tableGroupJoin.getJoinedGroup();
		assertThat( joinedGroup.getTableJoins().size(), is( 0 ) );
	}

	@Test
	public void testSimpleAttributeReference() {
		final SqmSelectStatement statement = (SqmSelectStatement) interpret( "select p.email from Person p" );

		final SelectStatementInterpreter interpreter = new SelectStatementInterpreter( queryOptions(), callBack() );
		interpreter.interpret( statement );

		final SelectClause selectClause = interpreter.getSelectQuery().getQuerySpec().getSelectClause();

		assertThat( selectClause.getSelections().size(), is( 1 ) );
		assertThat( selectClause.getSelections().get( 0 ).getResultVariable(), startsWith( "<gen:" ) );
		assertThat(
				selectClause.getSelections().get( 0 ).getSelectExpression(),
				instanceOf( AttributeReference.class )
		);
	}

	@Override
	protected void applyMetadataSources(MetadataSources metadataSources) {
		metadataSources.addAnnotatedClass( Person.class );
		metadataSources.addAnnotatedClass( Address.class );
		metadataSources.addAnnotatedClass( Role.class );
		metadataSources.addAnnotatedClass( ZipCode.class );
	}

	@Entity(name = "Person")
	@javax.persistence.Table(name = "PERSON")
	public static class Person {
		@Id
		@GeneratedValue
		long id;

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

	@Entity(name = "Address")
	@javax.persistence.Table(name = "ADDRESS")
	public static class Address {
		@Id
		@GeneratedValue
		long id;

		@ManyToOne
		ZipCode code;
	}

	@Entity(name = "ZipCode")
	@javax.persistence.Table(name = "ZIP_CODE")
	public static class ZipCode {
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

	private TableGroup checkTableGroupJoin(
			TableGroupJoin tableGroupJoin,
			JoinType joinType,
			Class<? extends AbstractTableGroup> joinGroupType,
			String joinGroupRootTableName,
			String joinedGroupRootTableAlias) {

		assertThat( tableGroupJoin.getJoinType(), is( joinType ) );

		final TableGroup joinedGroup = tableGroupJoin.getJoinedGroup();
		assertThat( joinedGroup, is( instanceOf( joinGroupType ) ) );

		checkRootTableName( joinGroupRootTableName, joinedGroup );
		assertThat( joinedGroup.getRootTableBinding().getIdentificationVariable(), is( joinedGroupRootTableAlias ) );
		return joinedGroup;
	}

	private void checkRootTableName(String expectedTableName, TableGroup tableGroup) {
		final TableBinding tableBinding = tableGroup.getRootTableBinding();
		checkTableName( expectedTableName, tableBinding );
	}

	private void checkTableName(String expectedTableName, TableBinding tableBinding) {
		assertThat( tableBinding.getTable(), is( instanceOf( PhysicalTable.class ) ) );
		assertThat( ((PhysicalTable) tableBinding.getTable()).getTableName(), is( expectedTableName ) );
	}

	private void checkConjunctionPredicateWithOneRelationPredicate(
			Junction junction,
			String leftHandColumnAlias,
			String leftHandTableName,
			String rightHandTableAlias,
			String rightHandTableName) {
		checkJoinPredicateIsAConjunctionWithOneRelationPredicate( junction );
		final RelationalPredicate joinPredicate = (RelationalPredicate) junction.getPredicates().get( 0 );
		checkJoinPredicateColumn( leftHandColumnAlias, leftHandTableName, joinPredicate.getLeftHandExpression() );
		checkJoinPredicateColumn( rightHandTableAlias, rightHandTableName, joinPredicate.getRightHandExpression() );
	}

	private void checkJoinPredicateColumn(
			String JoinPredicateColumnAlias,
			String JoinPredicateColumnName,
			Expression joinPredicateColumn) {
		assertThat( joinPredicateColumn, instanceOf( ColumnBindingExpression.class ) );
		final ColumnBindingExpression columnBindingExpression = (ColumnBindingExpression) joinPredicateColumn;

		assertThat(
				columnBindingExpression.getColumnBinding().getIdentificationVariable(),
				is( JoinPredicateColumnAlias )
		);
		assertThat(
				((PhysicalColumn) columnBindingExpression.getColumnBinding().getColumn()).getName(),
				is( JoinPredicateColumnName )
		);
	}

	private void checkJoinPredicateIsAConjunctionWithOneRelationPredicate(Junction junction) {
		assertThat( junction.getNature(), is( Junction.Nature.CONJUNCTION ) );
		assertThat( junction.getPredicates().size(), is( 1 ) );
		assertThat( junction.getPredicates().get( 0 ), instanceOf( RelationalPredicate.class ) );
	}

	private Junction getJunctionJoinPredicate(TableJoin tableJoin) {
		assertThat( tableJoin.getJoinPredicate(), notNullValue() );
		final Predicate joinPredicate = tableJoin.getJoinPredicate();
		assertThat( joinPredicate, instanceOf( Junction.class ) );
		return (Junction) joinPredicate;
	}

	private Junction getJunctionJoinPredicate(TableGroupJoin tableGroupJoin) {
		assertThat( tableGroupJoin.getPredicate(), notNullValue() );
		final Predicate joinPredicate = tableGroupJoin.getPredicate();
		assertThat( joinPredicate, instanceOf( Junction.class ) );
		return (Junction) joinPredicate;
	}
}