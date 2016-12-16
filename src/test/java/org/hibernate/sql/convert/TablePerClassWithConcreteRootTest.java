/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.convert;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import java.util.List;

import org.hibernate.boot.MetadataSources;
import org.hibernate.persister.common.internal.DerivedTable;
import org.hibernate.persister.common.internal.PhysicalTable;
import org.hibernate.persister.common.spi.Table;
import org.hibernate.sql.ast.QuerySpec;
import org.hibernate.sql.ast.SelectQuery;
import org.hibernate.sql.ast.from.FromClause;
import org.hibernate.sql.ast.from.TableBinding;
import org.hibernate.sql.ast.from.TableSpace;
import org.hibernate.sql.BaseUnitTest;

import org.hibernate.testing.FailureExpected;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.hamcrest.core.IsNull.notNullValue;

/**
 * @author Andrea Boriero
 */
public class TablePerClassWithConcreteRootTest extends BaseUnitTest {

	@Test
	@FailureExpected( jiraKey = "none" )
	public void selectRootEntity() {
		final QuerySpec querySpec = getQuerySpec( "from RootEntity" );

		final FromClause fromClause =
				querySpec.getFromClause();
		final List<TableSpace> tableSpaces = fromClause.getTableSpaces();
		assertThat( tableSpaces.size(), is( 1 ) );
		final TableSpace tableSpace = tableSpaces.get( 0 );

		assertThat( tableSpace.getJoinedTableGroups().size(), is( 0 ) );

		final TableBinding rootTableBinding = tableSpace.getRootTableGroup().getRootTableBinding();
		final Table table = rootTableBinding.getTable();
		assertThat( table, instanceOf( DerivedTable.class ) );
		assertThat( table.getTableExpression(), containsString( "RootEntity union all" ) );
		assertThat( table.getTableExpression(), containsString( "from second_child" ) );
		assertThat( table.getTableExpression(), containsString( "from first_child" ) );

		assertThat( tableSpace.getRootTableGroup().getTableJoins().size(), is( 0 ) );
	}

	@Test
	@FailureExpected( jiraKey = "none" )
	public void selectChild() {
		final QuerySpec querySpec = getQuerySpec( "from FirstChild" );

		final FromClause fromClause =
				querySpec.getFromClause();
		final List<TableSpace> tableSpaces = fromClause.getTableSpaces();
		assertThat( tableSpaces.size(), is( 1 ) );
		final TableSpace tableSpace = tableSpaces.get( 0 );

		assertThat( tableSpace.getJoinedTableGroups().size(), is( 0 ) );

		final TableBinding rootTableBinding = tableSpace.getRootTableGroup().getRootTableBinding();
		final Table table = rootTableBinding.getTable();
		assertThat( table, instanceOf( PhysicalTable.class ) );

		assertThat( tableSpace.getRootTableGroup().getTableJoins().size(), is( 0 ) );
	}

	@Override
	protected void applyMetadataSources(MetadataSources metadataSources) {
		metadataSources.addAnnotatedClass( RootEntity.class );
		metadataSources.addAnnotatedClass( FirstChild.class );
		metadataSources.addAnnotatedClass( SecondChild.class );
	}

	@Entity(name = "RootEntity")
	@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
	public static class RootEntity {
		@Id
		public Integer id;
		public String name;
		public String description;
	}

	@Entity(name = "FirstChild")
	@javax.persistence.Table(name = "first_child")
	public static class FirstChild extends RootEntity {
		public String child1;
	}

	@Entity(name = "SecondChild")
	@javax.persistence.Table(name = "second_child")
	public static class SecondChild extends RootEntity {
		public String child2;
	}

	private QuerySpec getQuerySpec(String hql) {
		final SelectQuery selectQuery = interpretSelectQuery(hql).getSqlSelectAst();
		assertThat( selectQuery, notNullValue() );
		return selectQuery.getQuerySpec();
	}
}
