package org.hibernate.sql.gen.internal;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.LockOptions;
import org.hibernate.ScrollMode;
import org.hibernate.boot.MetadataSources;
import org.hibernate.engine.spi.RowSelection;
import org.hibernate.sql.ast.SelectQuery;
import org.hibernate.sql.ast.from.CollectionTableSpecificationGroup;
import org.hibernate.sql.ast.from.EntityTableSpecificationGroup;
import org.hibernate.sql.ast.from.PhysicalTableSpecification;
import org.hibernate.sql.ast.from.TableSpace;
import org.hibernate.sql.ast.from.TableSpecification;
import org.hibernate.sql.ast.from.TableSpecificationGroup;
import org.hibernate.sql.ast.from.TableSpecificationGroupJoin;
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
		SelectStatement statement = (SelectStatement) interpret( "from Person p join p.addresses" );

		SelectStatementInterpreter interpreter = new SelectStatementInterpreter( queryOption(), callBack() );
		interpreter.interpret( statement );

		SelectQuery selectQuery = interpreter.getSelectQuery();

		final List<TableSpace> tableSpaces = selectQuery.getQuerySpec().getFromClause().getTableSpaces();
		assertThat( tableSpaces.size(), is( 1 ) );

		final TableSpace tableSpace = tableSpaces.get( 0 );
		assertThat( tableSpace.getJoinedTableSpecificationGroups().size(), is( 1 ) );

		final TableSpecificationGroupJoin tableSpecificationGroupJoin = tableSpace.getJoinedTableSpecificationGroups()
				.get( 0 );
		assertThat( tableSpecificationGroupJoin.getJoinType(), is( JoinType.INNER ) );

		final TableSpecificationGroup joinedGroup = tableSpecificationGroupJoin.getJoinedGroup();
		assertThat( joinedGroup, is( instanceOf( CollectionTableSpecificationGroup.class ) ) );

		assertThat( joinedGroup.getTableSpecificationJoins().size(), is( 0 ) )
		;
		final TableSpecification rootTableSpecification = joinedGroup.getRootTableSpecification();
		assertThat( rootTableSpecification, is( instanceOf( PhysicalTableSpecification.class ) ) );

		assertThat( ((PhysicalTableSpecification) rootTableSpecification).getTableName(), is( "PERSON_ADDRESS" ) );
	}

	@Test
	public void joinCollectionValuedFieldWithJoinColumnTest() {
		SelectStatement statement = (SelectStatement) interpret( "from Person p join p.pastRoles" );

		SelectStatementInterpreter interpreter = new SelectStatementInterpreter( queryOption(), callBack() );
		interpreter.interpret( statement );

		SelectQuery selectQuery = interpreter.getSelectQuery();

		final List<TableSpace> tableSpaces = selectQuery.getQuerySpec().getFromClause().getTableSpaces();
		assertThat( tableSpaces.size(), is( 1 ) );

		final TableSpace tableSpace = tableSpaces.get( 0 );
		assertThat( tableSpace.getJoinedTableSpecificationGroups().size(), is( 1 ) );

		final TableSpecificationGroupJoin tableSpecificationGroupJoin = tableSpace.getJoinedTableSpecificationGroups()
				.get( 0 );
		assertThat( tableSpecificationGroupJoin.getJoinType(), is( JoinType.INNER ) );

		final TableSpecificationGroup joinedGroup = tableSpecificationGroupJoin.getJoinedGroup();
		assertThat( joinedGroup, is( instanceOf( CollectionTableSpecificationGroup.class ) ) );

		assertThat( joinedGroup.getTableSpecificationJoins().size(), is( 0 ) )
		;
		final TableSpecification rootTableSpecification = joinedGroup.getRootTableSpecification();
		assertThat( rootTableSpecification, is( instanceOf( PhysicalTableSpecification.class ) ) );

		assertThat( ((PhysicalTableSpecification) rootTableSpecification).getTableName(), is( "ROLE" ) );
	}

	@Test
	public void joinSingleValuedObjectFieldTest() {
		SelectStatement statement = (SelectStatement) interpret( "from Person p join p.actualRole" );

		SelectStatementInterpreter interpreter = new SelectStatementInterpreter( queryOption(), callBack() );
		interpreter.interpret( statement );

		SelectQuery selectQuery = interpreter.getSelectQuery();

		final List<TableSpace> tableSpaces = selectQuery.getQuerySpec().getFromClause().getTableSpaces();
		assertThat( tableSpaces.size(), is( 1 ) );

		final TableSpace tableSpace = tableSpaces.get( 0 );
		assertThat( tableSpace.getJoinedTableSpecificationGroups().size(), is( 1 ) );

		final TableSpecificationGroupJoin tableSpecificationGroupJoin = tableSpace.getJoinedTableSpecificationGroups()
				.get( 0 );
		assertThat( tableSpecificationGroupJoin.getJoinType(), is( JoinType.INNER ) );

		final TableSpecificationGroup joinedGroup = tableSpecificationGroupJoin.getJoinedGroup();
		assertThat( joinedGroup, is( instanceOf( EntityTableSpecificationGroup.class ) ) );

		assertThat( joinedGroup.getTableSpecificationJoins().size(), is( 0 ) )
		;
		final TableSpecification rootTableSpecification = joinedGroup.getRootTableSpecification();
		assertThat( rootTableSpecification, is( instanceOf( PhysicalTableSpecification.class ) ) );

		assertThat( ((PhysicalTableSpecification) rootTableSpecification).getTableName(), is( "ROLE" ) );
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

	@Override
	protected void applyMetadataSources(MetadataSources metadataSources) {
		metadataSources.addAnnotatedClass( Person.class );
		metadataSources.addAnnotatedClass( Address.class );
		metadataSources.addAnnotatedClass( Role.class );
	}

	@Entity(name = "Person")
	@Table(name = "PERSON")
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
	@Table(name = "ADDRESS")
	public static class Address {
		@Id
		@GeneratedValue
		long id;
	}

	@Entity(name = "Role")
	@Table(name = "ROLE")
	public static class Role {
		@Id
		@GeneratedValue
		long id;
	}
}