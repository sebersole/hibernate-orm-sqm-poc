package org.hibernate.sql.gen.internal;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.LockOptions;
import org.hibernate.ScrollMode;
import org.hibernate.boot.MetadataSources;
import org.hibernate.engine.spi.RowSelection;
import org.hibernate.sql.ast.SelectQuery;
import org.hibernate.sql.ast.from.CollectionTableGroup;
import org.hibernate.sql.ast.from.EntityTableGroup;
import org.hibernate.sql.orm.internal.mapping.PhysicalTable;
import org.hibernate.sql.ast.from.TableSpace;
import org.hibernate.sql.ast.from.Table;
import org.hibernate.sql.ast.from.TableGroup;
import org.hibernate.sql.ast.from.TableGroupJoin;
import org.hibernate.sql.gen.BaseUnitTest;
import org.hibernate.sql.gen.Callback;
import org.hibernate.sql.orm.QueryOptions;
import org.hibernate.sql.orm.QueryParameterBindings;
import org.hibernate.sqm.SemanticQueryInterpreter;
import org.hibernate.sqm.query.JoinType;
import org.hibernate.sqm.query.SelectStatement;
import org.hibernate.sqm.query.Statement;

import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertThat;

/**
 * @author Andrea Boriero
 */
public class TableSpaceGenerationTest extends BaseUnitTest {

	@Test
	public void joinCollectionValuedFieldTest() {
		final TableSpace tableSpace = getTableSpace( "from Person p join p.addresses" );

		final TableGroup rootTableGroup = tableSpace.getRootTableGroup();
		assertThat( rootTableGroup.getTableJoins().size(), is( 0 ) );

		checkTableName( "PERSON", rootTableGroup );

		assertThat( tableSpace.getJoinedTableSpecificationGroups().size(), is( 1 ) );

		final TableGroupJoin tableGroupJoin =
				tableSpace.getJoinedTableSpecificationGroups().get( 0 );
		assertThat( tableGroupJoin.getJoinType(), is( JoinType.INNER ) );

		final TableGroup joinedGroup = tableGroupJoin.getJoinedGroup();
		assertThat( joinedGroup, is( instanceOf( CollectionTableGroup.class ) ) );

		assertThat( joinedGroup.getTableJoins().size(), is( 0 ) );

		checkTableName( "PERSON_ADDRESS", joinedGroup );
	}

	@Test
	public void joinCollectionValuedFieldWithJoinColumnTest() {
		final TableSpace tableSpace = getTableSpace( "from Person p join p.pastRoles" );

		final TableGroup rootTableGroup = tableSpace.getRootTableGroup();
		assertThat( rootTableGroup.getTableJoins().size(), is( 0 ) );

		checkTableName( "PERSON", rootTableGroup );

		assertThat( tableSpace.getJoinedTableSpecificationGroups().size(), is( 1 ) );

		final TableGroupJoin tableGroupJoin =
				tableSpace.getJoinedTableSpecificationGroups().get( 0 );
		assertThat( tableGroupJoin.getJoinType(), is( JoinType.INNER ) );

		final TableGroup joinedGroup = tableGroupJoin.getJoinedGroup();
		assertThat( joinedGroup, is( instanceOf( CollectionTableGroup.class ) ) );

		assertThat( joinedGroup.getTableJoins().size(), is( 0 ) );

		checkTableName( "ROLE", joinedGroup );
	}

	@Test
	public void joinSingleValuedObjectFieldTest() {
		final TableSpace tableSpace = getTableSpace( "from Person p join p.actualRole" );

		final TableGroup rootTableGroup = tableSpace.getRootTableGroup();
		assertThat( rootTableGroup.getTableJoins().size(), is( 0 ) );

		checkTableName( "PERSON", rootTableGroup );

		assertThat( tableSpace.getJoinedTableSpecificationGroups().size(), is( 1 ) );

		final TableGroupJoin tableGroupJoin =
				tableSpace.getJoinedTableSpecificationGroups().get( 0 );
		assertThat( tableGroupJoin.getJoinType(), is( JoinType.INNER ) );

		final TableGroup joinedGroup = tableGroupJoin.getJoinedGroup();
		assertThat( joinedGroup, is( instanceOf( EntityTableGroup.class ) ) );

		assertThat( joinedGroup.getTableJoins().size(), is( 0 ) );

		checkTableName( "ROLE", joinedGroup );
	}

	@Test
	public void crossJoinTest() {
		final TableSpace tableSpace = getTableSpace( "from Person cross join Role" );

		final TableGroup rootTableGroup = tableSpace.getRootTableGroup();
		assertThat( rootTableGroup.getTableJoins().size(), is( 0 ) );

		checkTableName( "PERSON", rootTableGroup );

		assertThat( tableSpace.getJoinedTableSpecificationGroups().size(), is( 1 ) );

		final TableGroupJoin tableGroupJoin =
				tableSpace.getJoinedTableSpecificationGroups().get( 0 );
		assertThat( tableGroupJoin.getJoinType(), is( JoinType.CROSS ) );

		final TableGroup joinedGroup = tableGroupJoin.getJoinedGroup();
		assertThat( joinedGroup, is( instanceOf( EntityTableGroup.class ) ) );

		assertThat( joinedGroup.getTableJoins().size(), is( 0 ) );

		checkTableName( "ROLE", joinedGroup );
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

		@OneToMany
		Set<Address> addresses = new HashSet<Address>();

		@OneToMany
		@JoinColumn(name = "person_id")
		Set<Role> pastRoles = new HashSet<Role>();

		@OneToOne
		Role actualRole;
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
		final SelectStatement statement = (SelectStatement) interpret( query );

		final SelectStatementInterpreter interpreter = new SelectStatementInterpreter( queryOption(), callBack() );
		interpreter.interpret( statement );

		final SelectQuery selectQuery = interpreter.getSelectQuery();

		final List<TableSpace> tableSpaces = selectQuery.getQuerySpec().getFromClause().getTableSpaces();
		assertThat( tableSpaces.size(), is( 1 ) );

		return tableSpaces.get( 0 );
	}

	private Callback callBack() {
		return new Callback() {
		};
	}

	private QueryOptions queryOption() {
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

	protected Statement interpret(String query) {
		return SemanticQueryInterpreter.interpret( query, getConsumerContext() );
	}

	private void checkTableName(String expectedTableName, TableGroup tableGroup) {
		final Table table = tableGroup.getRootTable();
		assertThat( table.getTableReference(), is( instanceOf( PhysicalTable.class ) ) );
		assertThat( ((PhysicalTable) table.getTableReference() ).getTableName(), is( expectedTableName ) );
	}
}