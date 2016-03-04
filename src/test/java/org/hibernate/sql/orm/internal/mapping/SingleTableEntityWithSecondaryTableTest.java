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
import org.hibernate.sql.ast.from.PhysicalTable;
import org.hibernate.sql.gen.BaseUnitTest;
import org.hibernate.sql.gen.internal.FromClauseIndex;
import org.hibernate.sql.gen.internal.SqlAliasBaseManager;
import org.hibernate.sql.orm.internal.sqm.model.EntityTypeImpl;
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

		final EntityTypeImpl entityTypeDescriptor =
				(EntityTypeImpl) getConsumerContext().getDomainMetamodel().resolveEntityType( "SingleTableWithSecondaryTableEntity" );
		final ImprovedEntityPersister improvedEntityPersister = entityTypeDescriptor.getPersister();
		assertThat( improvedEntityPersister.getEntityPersister(), instanceOf( SingleTableEntityPersister.class ) );

		// interpreter set up
		final QuerySpec querySpec = new QuerySpec();
		final SqlAliasBaseManager aliasBaseManager = new SqlAliasBaseManager();
		final FromClauseIndex fromClauseIndex = new FromClauseIndex();

		final EntityTableGroup result = improvedEntityPersister.getEntityTableSpecificationGroup(
				sqm.getQuerySpec().getFromClause().getFromElementSpaces().get( 0 ).getRoot(),
				querySpec.getFromClause().makeTableSpace(),
				aliasBaseManager,
				fromClauseIndex
		);
		assertThat( result, notNullValue() );
		assertThat( result.getAliasBase(), equalTo( "s1" ) );

		assertThat( result.getRootTable(), notNullValue() );
		assertThat( result.getRootTable(), instanceOf( PhysicalTable.class ) );
		final PhysicalTable tableSpec = (PhysicalTable) result.getRootTable();
		assertThat( tableSpec.getTableName(), equalTo( "stwst_primary" ) );
		assertThat( tableSpec.getCorrelationName(), equalTo( "s1_0" ) );

		assertThat( result.getTableJoins().size(), equalTo( 1 ) );
		assertThat(
				result.getTableJoins().get( 0 ).getJoinedTable(),
				instanceOf( PhysicalTable.class )
		);
		final PhysicalTable secondaryTable = (PhysicalTable) result.getTableJoins().get( 0 ).getJoinedTable();
		assertThat( secondaryTable.getTableName(), equalTo( "stwst_secondary" ) );
		assertThat( secondaryTable.getCorrelationName(), equalTo( "s1_1" ) );

	}
	@Test
	public void testTwoSpaces() {
		SelectStatement sqm = (SelectStatement) interpret( "from SingleTableWithSecondaryTableEntity, SingleTableWithSecondaryTableEntity" );

		final EntityTypeImpl entityTypeDescriptor =
				(EntityTypeImpl) getConsumerContext().getDomainMetamodel().resolveEntityType( "SingleTableWithSecondaryTableEntity" );
		final ImprovedEntityPersister improvedEntityPersister = entityTypeDescriptor.getPersister();
		assertThat( improvedEntityPersister.getEntityPersister(), instanceOf( SingleTableEntityPersister.class ) );

		assertThat( sqm.getQuerySpec().getFromClause().getFromElementSpaces().size(), equalTo( 2 ) );

		// interpreter set up
		final QuerySpec querySpec = new QuerySpec();
		final SqlAliasBaseManager aliasBaseManager = new SqlAliasBaseManager();
		final FromClauseIndex fromClauseIndex = new FromClauseIndex();

		// the first space
		final EntityTableGroup firstSpace = improvedEntityPersister.getEntityTableSpecificationGroup(
				sqm.getQuerySpec().getFromClause().getFromElementSpaces().get( 0 ).getRoot(),
				querySpec.getFromClause().makeTableSpace(),
				aliasBaseManager,
				fromClauseIndex
		);
		assertThat( firstSpace, notNullValue() );
		assertThat( firstSpace.getAliasBase(), equalTo( "s1" ) );

		assertThat( firstSpace.getRootTable(), notNullValue() );
		assertThat( firstSpace.getRootTable(), instanceOf( PhysicalTable.class ) );
		final PhysicalTable firstSpaceTableSpec = (PhysicalTable) firstSpace.getRootTable();
		assertThat( firstSpaceTableSpec.getTableName(), equalTo( "stwst_primary" ) );
		assertThat( firstSpaceTableSpec.getCorrelationName(), equalTo( "s1_0" ) );

		assertThat( firstSpace.getTableJoins().size(), equalTo( 1 ) );
		assertThat(
				firstSpace.getTableJoins().get( 0 ).getJoinedTable(),
				instanceOf( PhysicalTable.class )
		);
		final PhysicalTable firstSpaceSecondaryTable = (PhysicalTable) firstSpace.getTableJoins().get( 0 ).getJoinedTable();
		assertThat( firstSpaceSecondaryTable.getTableName(), equalTo( "stwst_secondary" ) );
		assertThat( firstSpaceSecondaryTable.getCorrelationName(), equalTo( "s1_1" ) );

		// the second space
		final EntityTableGroup secondSpace = improvedEntityPersister.getEntityTableSpecificationGroup(
				sqm.getQuerySpec().getFromClause().getFromElementSpaces().get( 1 ).getRoot(),
				querySpec.getFromClause().makeTableSpace(),
				aliasBaseManager,
				fromClauseIndex
		);
		assertThat( secondSpace, notNullValue() );
		assertThat( secondSpace.getAliasBase(), equalTo( "s2" ) );

		assertThat( secondSpace.getRootTable(), notNullValue() );
		assertThat( secondSpace.getRootTable(), instanceOf( PhysicalTable.class ) );
		final PhysicalTable secondSpaceTableSpec = (PhysicalTable) secondSpace.getRootTable();
		assertThat( secondSpaceTableSpec.getTableName(), equalTo( "stwst_primary" ) );
		assertThat( secondSpaceTableSpec.getCorrelationName(), equalTo( "s2_0" ) );

		assertThat( secondSpace.getTableJoins().size(), equalTo( 1 ) );
		assertThat(
				secondSpace.getTableJoins().get( 0 ).getJoinedTable(),
				instanceOf( PhysicalTable.class )
		);
		final PhysicalTable secondSpaceSecondaryTable = (PhysicalTable) secondSpace.getTableJoins().get( 0 ).getJoinedTable();
		assertThat( secondSpaceSecondaryTable.getTableName(), equalTo( "stwst_secondary" ) );
		assertThat( secondSpaceSecondaryTable.getCorrelationName(), equalTo( "s2_1" ) );
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
