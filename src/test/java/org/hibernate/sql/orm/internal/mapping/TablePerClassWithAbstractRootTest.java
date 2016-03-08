/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.sql.orm.internal.mapping;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import java.util.List;

import org.hibernate.boot.MetadataSources;
import org.hibernate.sql.ast.QuerySpec;
import org.hibernate.sql.ast.SelectQuery;
import org.hibernate.sql.ast.from.FromClause;
import org.hibernate.sql.ast.from.TableBinding;
import org.hibernate.sql.ast.from.TableSpace;
import org.hibernate.sql.ast.select.Selection;
import org.hibernate.sql.gen.BaseUnitTest;
import org.hibernate.sql.gen.internal.SelectStatementInterpreter;
import org.hibernate.sqm.query.SelectStatement;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.hamcrest.core.IsNull.notNullValue;

/**
 * @author Andrea Boriero
 */
public class TablePerClassWithAbstractRootTest extends BaseUnitTest {

	@Test
	public void selectRootEntity() {
		final QuerySpec querySpec = getQuerySpec( "from RootEntity" );

		final List<Selection> selections = querySpec.getSelectClause().getSelections();
		assertThat( selections.size(), is( 5 ) );
		final FromClause fromClause =
				querySpec.getFromClause();
		final List<TableSpace> tableSpaces = fromClause.getTableSpaces();
		assertThat( tableSpaces.size(), is( 1 ) );
		final TableSpace tableSpace = tableSpaces.get( 0 );

		assertThat( tableSpace.getJoinedTableGroups().size(), is( 0 ) );

		final TableBinding rootTableBinding = tableSpace.getRootTableGroup().getRootTableBinding();
		final Table table = rootTableBinding.getTable();
		assertThat( table, instanceOf( DerivedTable.class ) );
		assertThat(
				table.getTableExpression(),
				is( "( select id, description, name, child2, null as child1, 1 as clazz_ " +
							"from second_child " +
							"union all " +
							"select id, description, name, null as child2, child1, 2 as clazz_ " +
							"from first_child )" )
		);

		assertThat( tableSpace.getRootTableGroup().getTableJoins().size(), is( 0 ) );
	}

	private QuerySpec getQuerySpec(String hql) {
		final SelectStatement statement = (SelectStatement) interpret( hql );
		final SelectStatementInterpreter interpreter = new SelectStatementInterpreter( queryOption(), callBack() );
		interpreter.interpret( statement );
		final SelectQuery selectQuery = interpreter.getSelectQuery();
		assertThat( selectQuery, notNullValue() );
		return selectQuery.getQuerySpec();
	}


	@Override
	protected void applyMetadataSources(MetadataSources metadataSources) {
		metadataSources.addAnnotatedClass( RootEntity.class );
		metadataSources.addAnnotatedClass( FirstChild.class );
		metadataSources.addAnnotatedClass( SecondChild.class );
	}

	@Entity(name = "RootEntity")
	@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
	public abstract static class RootEntity {
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

}
