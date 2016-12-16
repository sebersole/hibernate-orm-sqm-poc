/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.convert;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.boot.MetadataSources;
import org.hibernate.persister.common.internal.PhysicalTable;
import org.hibernate.persister.entity.SingleTableEntityPersister;
import org.hibernate.persister.entity.spi.ImprovedEntityPersister;
import org.hibernate.sql.ast.QuerySpec;
import org.hibernate.sql.ast.from.EntityTableGroup;
import org.hibernate.sql.ast.from.TableBinding;
import org.hibernate.sql.convert.internal.FromClauseIndex;
import org.hibernate.sql.convert.internal.SqlAliasBaseManager;
import org.hibernate.sql.BaseUnitTest;
import org.hibernate.sqm.query.SqmSelectStatement;

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
		SqmSelectStatement sqm = (SqmSelectStatement) interpret( "from SingleTableEntity" );

		final ImprovedEntityPersister improvedEntityPersister =
				(ImprovedEntityPersister) getConsumerContext().getDomainMetamodel().resolveEntityReference( "SingleTableEntity" );
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
		assertThat( result.getTableJoins().size(), equalTo( 0 ) );
		assertThat( result.getRootTableBinding().getTable(), instanceOf( PhysicalTable.class ) );

		final TableBinding tableBindingSpec = result.getRootTableBinding();
		assertThat( tableBindingSpec.getTable().getTableExpression(), equalTo( "single_table_entity" ) );
		assertThat( tableBindingSpec.getIdentificationVariable(), equalTo( "s1" ) );
	}

	@Test
	public void testTwoSpaces() {
		SqmSelectStatement sqm = (SqmSelectStatement) interpret( "from SingleTableEntity, SingleTableEntity" );

		final ImprovedEntityPersister improvedEntityPersister =
				(ImprovedEntityPersister) getConsumerContext().getDomainMetamodel().resolveEntityReference( "SingleTableEntity" );
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
		assertThat( firstSpace.getTableJoins().size(), equalTo( 0 ) );
		assertThat( firstSpace.getRootTableBinding().getTable(), instanceOf( PhysicalTable.class ) );

		final TableBinding firstSpaceTableBindingSpec = firstSpace.getRootTableBinding();
		assertThat( firstSpaceTableBindingSpec.getTable().getTableExpression(), equalTo( "single_table_entity" ) );
		assertThat( firstSpaceTableBindingSpec.getIdentificationVariable(), equalTo( "s1" ) );

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
		assertThat( secondSpace.getTableJoins().size(), equalTo( 0 ) );
		assertThat( secondSpace.getRootTableBinding().getTable(), instanceOf( PhysicalTable.class ) );

		final TableBinding secondSpaceTableBindingSpec = secondSpace.getRootTableBinding();
		assertThat( secondSpaceTableBindingSpec.getTable().getTableExpression(), equalTo( "single_table_entity" ) );
		assertThat( secondSpaceTableBindingSpec.getIdentificationVariable(), equalTo( "s2" ) );
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
