/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.sql.orm.internal.mapping;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.SecondaryTable;
import javax.persistence.Table;

import org.hibernate.boot.MetadataSources;
import org.hibernate.persister.entity.SingleTableEntityPersister;
import org.hibernate.sql.ast.QuerySpec;
import org.hibernate.sql.ast.from.EntityTableGroup;
import org.hibernate.sql.ast.from.TableBinding;
import org.hibernate.sql.gen.BaseUnitTest;
import org.hibernate.sql.gen.internal.FromClauseIndex;
import org.hibernate.sql.gen.internal.SqlAliasBaseManager;
import org.hibernate.sqm.query.SelectStatement;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Test of a ImprovedEntityPersister over a simple (no secondary tables)
 * {@link SingleTableEntityPersister}
 *
 * @author Steve Ebersole
 */
public class SingleTableEntityWithSecondaryTableTest extends BaseUnitTest {
	@Test
	public void testSingleSpace() {
		SelectStatement sqm = (SelectStatement) interpret( "from SingleTableWithSecondaryTableEntity" );

		final ImprovedEntityPersister improvedEntityPersister =
				(ImprovedEntityPersister) getConsumerContext().getDomainMetamodel().resolveEntityType( "SingleTableWithSecondaryTableEntity" );
		assertThat( improvedEntityPersister.getEntityPersister(), instanceOf( SingleTableEntityPersister.class ) );

		// interpreter set up
		final QuerySpec querySpec = new QuerySpec();
		final SqlAliasBaseManager aliasBaseManager = new SqlAliasBaseManager();
		final FromClauseIndex fromClauseIndex = new FromClauseIndex();

		final EntityTableGroup result = improvedEntityPersister.buildTableGroup(
				sqm.getQuerySpec().getFromClause().getFromElementSpaces().get( 0 ).getRoot(),
				querySpec.getFromClause().makeTableSpace(),
				aliasBaseManager,
				fromClauseIndex
		);
		assertThat( result, notNullValue() );
		assertThat( result.getAliasBase(), equalTo( "s1" ) );

		assertThat( result.getRootTableBinding(), notNullValue() );
		assertThat( result.getRootTableBinding().getTable(), instanceOf( PhysicalTable.class ) );
		final TableBinding tableBindingSpec = result.getRootTableBinding();
		assertThat( tableBindingSpec.getTable().getTableExpression(), equalTo( "stwst_primary" ) );
		assertThat( tableBindingSpec.getIdentificationVariable(), equalTo( "s1" ) );

		assertThat( result.getTableJoins().size(), equalTo( 1 ) );
		assertThat(
				result.getTableJoins().get( 0 ).getJoinedTableBinding().getTable(),
				instanceOf( PhysicalTable.class )
		);
		final TableBinding secondaryTableBinding = result.getTableJoins().get( 0 ).getJoinedTableBinding();
		assertThat( secondaryTableBinding.getTable().getTableExpression(), equalTo( "stwst_secondary" ) );
		assertThat( secondaryTableBinding.getIdentificationVariable(), equalTo( "s1_0" ) );

	}
	@Test
	public void testTwoSpaces() {
		SelectStatement sqm = (SelectStatement) interpret( "from SingleTableWithSecondaryTableEntity, SingleTableWithSecondaryTableEntity" );

		final ImprovedEntityPersister improvedEntityPersister =
				(ImprovedEntityPersister) getConsumerContext().getDomainMetamodel().resolveEntityType( "SingleTableWithSecondaryTableEntity" );
		assertThat( improvedEntityPersister.getEntityPersister(), instanceOf( SingleTableEntityPersister.class ) );

		assertThat( sqm.getQuerySpec().getFromClause().getFromElementSpaces().size(), equalTo( 2 ) );

		// interpreter set up
		final QuerySpec querySpec = new QuerySpec();
		final SqlAliasBaseManager aliasBaseManager = new SqlAliasBaseManager();
		final FromClauseIndex fromClauseIndex = new FromClauseIndex();

		// the first space
		final EntityTableGroup firstSpace = improvedEntityPersister.buildTableGroup(
				sqm.getQuerySpec().getFromClause().getFromElementSpaces().get( 0 ).getRoot(),
				querySpec.getFromClause().makeTableSpace(),
				aliasBaseManager,
				fromClauseIndex
		);
		assertThat( firstSpace, notNullValue() );
		assertThat( firstSpace.getAliasBase(), equalTo( "s1" ) );

		assertThat( firstSpace.getRootTableBinding(), notNullValue() );
		assertThat( firstSpace.getRootTableBinding().getTable(), instanceOf( PhysicalTable.class ) );
		final TableBinding firstSpaceTableBindingSpec = firstSpace.getRootTableBinding();
		assertThat( firstSpaceTableBindingSpec.getTable().getTableExpression(), equalTo( "stwst_primary" ) );
		assertThat( firstSpaceTableBindingSpec.getIdentificationVariable(), equalTo( "s1" ) );

		assertThat( firstSpace.getTableJoins().size(), equalTo( 1 ) );
		assertThat(
				firstSpace.getTableJoins().get( 0 ).getJoinedTableBinding().getTable(),
				instanceOf( PhysicalTable.class )
		);
		final TableBinding firstSpaceSecondaryTableBinding = firstSpace.getTableJoins().get( 0 ).getJoinedTableBinding();
		assertThat( firstSpaceSecondaryTableBinding.getTable().getTableExpression(), equalTo( "stwst_secondary" ) );
		assertThat( firstSpaceSecondaryTableBinding.getIdentificationVariable(), equalTo( "s1_0" ) );

		// the second space
		final EntityTableGroup secondSpace = improvedEntityPersister.buildTableGroup(
				sqm.getQuerySpec().getFromClause().getFromElementSpaces().get( 1 ).getRoot(),
				querySpec.getFromClause().makeTableSpace(),
				aliasBaseManager,
				fromClauseIndex
		);
		assertThat( secondSpace, notNullValue() );
		assertThat( secondSpace.getAliasBase(), equalTo( "s2" ) );

		assertThat( secondSpace.getRootTableBinding(), notNullValue() );
		assertThat( secondSpace.getRootTableBinding().getTable(), instanceOf( PhysicalTable.class ) );
		final TableBinding secondSpaceTableBindingSpec = secondSpace.getRootTableBinding();
		assertThat( secondSpaceTableBindingSpec.getTable().getTableExpression(), equalTo( "stwst_primary" ) );
		assertThat( secondSpaceTableBindingSpec.getIdentificationVariable(), equalTo( "s2" ) );

		assertThat( secondSpace.getTableJoins().size(), equalTo( 1 ) );
		assertThat(
				secondSpace.getTableJoins().get( 0 ).getJoinedTableBinding().getTable(),
				instanceOf( PhysicalTable.class )
		);
		final TableBinding secondSpaceSecondaryTableBinding = secondSpace.getTableJoins().get( 0 ).getJoinedTableBinding();
		assertThat( secondSpaceSecondaryTableBinding.getTable().getTableExpression(), equalTo( "stwst_secondary" ) );
		assertThat( secondSpaceSecondaryTableBinding.getIdentificationVariable(), equalTo( "s2_0" ) );
	}

	@Override
	protected void applyMetadataSources(MetadataSources metadataSources) {
		metadataSources.addAnnotatedClass( SingleTableWithSecondaryTableEntity.class );
	}

	@Entity( name = "SingleTableWithSecondaryTableEntity" )
	@Table( name = "stwst_primary" )
	@SecondaryTable( name = "stwst_secondary")
	public static class SingleTableWithSecondaryTableEntity {
		@Id
		public Integer id;
		public String name;
		@Column( table = "stwst_secondary" )
		public String description;
	}
}
