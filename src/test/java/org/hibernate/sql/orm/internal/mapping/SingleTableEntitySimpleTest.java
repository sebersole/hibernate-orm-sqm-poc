/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.sql.orm.internal.mapping;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.boot.MetadataSources;
import org.hibernate.persister.entity.SingleTableEntityPersister;
import org.hibernate.sql.ast.QuerySpec;
import org.hibernate.sql.ast.from.EntityTableGroup;
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
public class SingleTableEntitySimpleTest extends BaseUnitTest {
	@Test
	public void testSingleSpace() {
		SelectStatement sqm = (SelectStatement) interpret( "from SingleTableEntity" );

		final EntityTypeImpl entityTypeDescriptor =
				(EntityTypeImpl) getConsumerContext().getDomainMetamodel().resolveEntityType( "SingleTableEntity" );
		final ImprovedEntityPersister improvedEntityPersister = entityTypeDescriptor.getPersister();
		assertThat( improvedEntityPersister.getEntityPersister(), instanceOf( SingleTableEntityPersister.class ) );

		// interpreter set up
		final QuerySpec querySpec = new QuerySpec();
		final SqlAliasBaseManager aliasBaseManager = new SqlAliasBaseManager();
		final FromClauseIndex fromClauseIndex = new FromClauseIndex();

		final EntityTableGroup result = improvedEntityPersister.getEntityTableGroup(
				sqm.getQuerySpec().getFromClause().getFromElementSpaces().get( 0 ).getRoot(),
				querySpec.getFromClause().makeTableSpace(),
				aliasBaseManager,
				fromClauseIndex
		);
		assertThat( result, notNullValue() );
		assertThat( result.getAliasBase(), equalTo( "s1" ) );

		assertThat( result.getRootTable(), notNullValue() );
		assertThat( result.getTableJoins().size(), equalTo( 0 ) );
		assertThat( result.getRootTable().getTableReference(), instanceOf( PhysicalTable.class ) );

		final org.hibernate.sql.ast.from.Table tableSpec = result.getRootTable();
		assertThat( tableSpec.getTableReference().getTableExpression(), equalTo( "single_table_entity" ) );
		assertThat( tableSpec.getIdentificationVariable(), equalTo( "s1_0" ) );
	}
	@Test
	public void testTwoSpaces() {
		SelectStatement sqm = (SelectStatement) interpret( "from SingleTableEntity, SingleTableEntity" );

		final EntityTypeImpl entityTypeDescriptor =
				(EntityTypeImpl) getConsumerContext().getDomainMetamodel().resolveEntityType( "SingleTableEntity" );
		final ImprovedEntityPersister improvedEntityPersister = entityTypeDescriptor.getPersister();
		assertThat( improvedEntityPersister.getEntityPersister(), instanceOf( SingleTableEntityPersister.class ) );

		assertThat( sqm.getQuerySpec().getFromClause().getFromElementSpaces().size(), equalTo( 2 ) );

		// interpreter set up
		final QuerySpec querySpec = new QuerySpec();
		final SqlAliasBaseManager aliasBaseManager = new SqlAliasBaseManager();
		final FromClauseIndex fromClauseIndex = new FromClauseIndex();

		// the first space
		final EntityTableGroup firstSpace = improvedEntityPersister.getEntityTableGroup(
				sqm.getQuerySpec().getFromClause().getFromElementSpaces().get( 0 ).getRoot(),
				querySpec.getFromClause().makeTableSpace(),
				aliasBaseManager,
				fromClauseIndex
		);
		assertThat( firstSpace, notNullValue() );
		assertThat( firstSpace.getAliasBase(), equalTo( "s1" ) );

		assertThat( firstSpace.getRootTable(), notNullValue() );
		assertThat( firstSpace.getTableJoins().size(), equalTo( 0 ) );
		assertThat( firstSpace.getRootTable().getTableReference(), instanceOf( PhysicalTable.class ) );

		final org.hibernate.sql.ast.from.Table firstSpaceTableSpec = firstSpace.getRootTable();
		assertThat( firstSpaceTableSpec.getTableReference().getTableExpression(), equalTo( "single_table_entity" ) );
		assertThat( firstSpaceTableSpec.getIdentificationVariable(), equalTo( "s1_0" ) );

		// the second space
		final EntityTableGroup secondSpace = improvedEntityPersister.getEntityTableGroup(
				sqm.getQuerySpec().getFromClause().getFromElementSpaces().get( 1 ).getRoot(),
				querySpec.getFromClause().makeTableSpace(),
				aliasBaseManager,
				fromClauseIndex
		);
		assertThat( secondSpace, notNullValue() );
		assertThat( secondSpace.getAliasBase(), equalTo( "s2" ) );

		assertThat( secondSpace.getRootTable(), notNullValue() );
		assertThat( secondSpace.getTableJoins().size(), equalTo( 0 ) );
		assertThat( secondSpace.getRootTable().getTableReference(), instanceOf( PhysicalTable.class ) );

		final org.hibernate.sql.ast.from.Table secondSpaceTableSpec = secondSpace.getRootTable();
		assertThat( secondSpaceTableSpec.getTableReference().getTableExpression(), equalTo( "single_table_entity" ) );
		assertThat( secondSpaceTableSpec.getIdentificationVariable(), equalTo( "s2_0" ) );
	}

	@Override
	protected void applyMetadataSources(MetadataSources metadataSources) {
		metadataSources.addAnnotatedClass( SingleTableEntity.class );
	}

	@Entity( name = "SingleTableEntity" )
	@Table( name = "single_table_entity" )
	public static class SingleTableEntity {
		@Id
		public Integer id;
		public String name;
	}
}
