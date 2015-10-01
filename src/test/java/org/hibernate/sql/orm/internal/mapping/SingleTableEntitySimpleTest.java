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
import org.hibernate.sql.ast.from.EntityTableSpecificationGroup;
import org.hibernate.sql.ast.from.PhysicalTableSpecification;
import org.hibernate.sql.gen.BaseUnitTest;
import org.hibernate.sql.gen.internal.FromClauseIndex;
import org.hibernate.sql.gen.internal.SqlAliasBaseManager;
import org.hibernate.sql.orm.internal.sqm.model.EntityTypeDescriptorImpl;
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

		final EntityTypeDescriptorImpl entityTypeDescriptor =
				(EntityTypeDescriptorImpl) getConsumerContext().resolveEntityReference( "SingleTableEntity" );
		final ImprovedEntityPersister improvedEntityPersister = entityTypeDescriptor.getPersister();
		assertThat( improvedEntityPersister.getEntityPersister(), instanceOf( SingleTableEntityPersister.class ) );

		// interpreter set up
		final QuerySpec querySpec = new QuerySpec();
		final SqlAliasBaseManager aliasBaseManager = new SqlAliasBaseManager();
		final FromClauseIndex fromClauseIndex = new FromClauseIndex();

		final EntityTableSpecificationGroup result = improvedEntityPersister.getEntityTableSpecificationGroup(
				sqm.getQuerySpec().getFromClause().getFromElementSpaces().get( 0 ).getRoot(),
				querySpec.getFromClause().makeTableSpace(),
				aliasBaseManager,
				fromClauseIndex
		);
		assertThat( result, notNullValue() );
		assertThat( result.getAliasBase(), equalTo( "s1" ) );

		assertThat( result.getRootTableSpecification(), notNullValue() );
		assertThat( result.getTableSpecificationJoins().size(), equalTo( 0 ) );
		assertThat( result.getRootTableSpecification(), instanceOf( PhysicalTableSpecification.class ) );

		final PhysicalTableSpecification tableSpec = (PhysicalTableSpecification) result.getRootTableSpecification();
		assertThat( tableSpec.getTableName(), equalTo( "single_table_entity" ) );
		assertThat( tableSpec.getCorrelationName(), equalTo( "s1_0" ) );
	}
	@Test
	public void testTwoSpaces() {
		SelectStatement sqm = (SelectStatement) interpret( "from SingleTableEntity, SingleTableEntity" );

		final EntityTypeDescriptorImpl entityTypeDescriptor =
				(EntityTypeDescriptorImpl) getConsumerContext().resolveEntityReference( "SingleTableEntity" );
		final ImprovedEntityPersister improvedEntityPersister = entityTypeDescriptor.getPersister();
		assertThat( improvedEntityPersister.getEntityPersister(), instanceOf( SingleTableEntityPersister.class ) );

		assertThat( sqm.getQuerySpec().getFromClause().getFromElementSpaces().size(), equalTo( 2 ) );

		// interpreter set up
		final QuerySpec querySpec = new QuerySpec();
		final SqlAliasBaseManager aliasBaseManager = new SqlAliasBaseManager();
		final FromClauseIndex fromClauseIndex = new FromClauseIndex();

		// the first space
		final EntityTableSpecificationGroup firstSpace = improvedEntityPersister.getEntityTableSpecificationGroup(
				sqm.getQuerySpec().getFromClause().getFromElementSpaces().get( 0 ).getRoot(),
				querySpec.getFromClause().makeTableSpace(),
				aliasBaseManager,
				fromClauseIndex
		);
		assertThat( firstSpace, notNullValue() );
		assertThat( firstSpace.getAliasBase(), equalTo( "s1" ) );

		assertThat( firstSpace.getRootTableSpecification(), notNullValue() );
		assertThat( firstSpace.getTableSpecificationJoins().size(), equalTo( 0 ) );
		assertThat( firstSpace.getRootTableSpecification(), instanceOf( PhysicalTableSpecification.class ) );

		final PhysicalTableSpecification firstSpaceTableSpec = (PhysicalTableSpecification) firstSpace.getRootTableSpecification();
		assertThat( firstSpaceTableSpec.getTableName(), equalTo( "single_table_entity" ) );
		assertThat( firstSpaceTableSpec.getCorrelationName(), equalTo( "s1_0" ) );

		// the second space
		final EntityTableSpecificationGroup secondSpace = improvedEntityPersister.getEntityTableSpecificationGroup(
				sqm.getQuerySpec().getFromClause().getFromElementSpaces().get( 1 ).getRoot(),
				querySpec.getFromClause().makeTableSpace(),
				aliasBaseManager,
				fromClauseIndex
		);
		assertThat( secondSpace, notNullValue() );
		assertThat( secondSpace.getAliasBase(), equalTo( "s2" ) );

		assertThat( secondSpace.getRootTableSpecification(), notNullValue() );
		assertThat( secondSpace.getTableSpecificationJoins().size(), equalTo( 0 ) );
		assertThat( secondSpace.getRootTableSpecification(), instanceOf( PhysicalTableSpecification.class ) );

		final PhysicalTableSpecification secondSpaceTableSpec = (PhysicalTableSpecification) secondSpace.getRootTableSpecification();
		assertThat( secondSpaceTableSpec.getTableName(), equalTo( "single_table_entity" ) );
		assertThat( secondSpaceTableSpec.getCorrelationName(), equalTo( "s2_0" ) );
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
