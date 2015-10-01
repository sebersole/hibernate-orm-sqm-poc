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
import javax.persistence.Table;

import org.hibernate.boot.MetadataSources;
import org.hibernate.persister.entity.JoinedSubclassEntityPersister;
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
 * @author Steve Ebersole
 */
public class JoinedEntitySimpleTest extends BaseUnitTest {
	@Test
	public void testSingleSpaceBase() {
		SelectStatement sqm = (SelectStatement) interpret( "from JoinedEntityBase" );

		final EntityTypeDescriptorImpl entityTypeDescriptor =
				(EntityTypeDescriptorImpl) getConsumerContext().resolveEntityReference( "JoinedEntityBase" );
		final ImprovedEntityPersister improvedEntityPersister = entityTypeDescriptor.getPersister();
		assertThat( improvedEntityPersister.getEntityPersister(), instanceOf( JoinedSubclassEntityPersister.class ) );

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
		assertThat( result.getAliasBase(), equalTo( "j1" ) );

		assertThat( result.getRootTableSpecification(), notNullValue() );
		assertThat( result.getRootTableSpecification(), instanceOf( PhysicalTableSpecification.class ) );
		final PhysicalTableSpecification tableSpec = (PhysicalTableSpecification) result.getRootTableSpecification();
		assertThat( tableSpec.getTableName(), equalTo( "joined_entity_base" ) );
		assertThat( tableSpec.getCorrelationName(), equalTo( "j1_0" ) );

		assertThat( result.getTableSpecificationJoins().size(), equalTo( 2 ) );

		assertThat(
				result.getTableSpecificationJoins().get( 0 ).getJoinedTable(),
				instanceOf( PhysicalTableSpecification.class )
		);
		final PhysicalTableSpecification firstSubclassTable = (PhysicalTableSpecification) result.getTableSpecificationJoins().get( 0 ).getJoinedTable();
		assertThat( firstSubclassTable.getTableName(), equalTo( "joined_entity_branch" ) );
		assertThat( firstSubclassTable.getCorrelationName(), equalTo( "j1_1" ) );

		assertThat(
				result.getTableSpecificationJoins().get( 1 ).getJoinedTable(),
				instanceOf( PhysicalTableSpecification.class )
		);
		final PhysicalTableSpecification secondSubclassTable = (PhysicalTableSpecification) result.getTableSpecificationJoins().get( 1 ).getJoinedTable();
		assertThat( secondSubclassTable.getTableName(), equalTo( "joined_entity_leaf" ) );
		assertThat( secondSubclassTable.getCorrelationName(), equalTo( "j1_2" ) );
	}

	@Test
	public void testSingleSpaceBranch() {
		SelectStatement sqm = (SelectStatement) interpret( "from JoinedEntityBranch" );

		final EntityTypeDescriptorImpl entityTypeDescriptor =
				(EntityTypeDescriptorImpl) getConsumerContext().resolveEntityReference( "JoinedEntityBranch" );
		final ImprovedEntityPersister improvedEntityPersister = entityTypeDescriptor.getPersister();
		assertThat( improvedEntityPersister.getEntityPersister(), instanceOf( JoinedSubclassEntityPersister.class ) );

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
		assertThat( result.getAliasBase(), equalTo( "j1" ) );

		assertThat( result.getRootTableSpecification(), notNullValue() );
		assertThat( result.getRootTableSpecification(), instanceOf( PhysicalTableSpecification.class ) );
		final PhysicalTableSpecification tableSpec = (PhysicalTableSpecification) result.getRootTableSpecification();
		assertThat( tableSpec.getTableName(), equalTo( "joined_entity_branch" ) );
		assertThat( tableSpec.getCorrelationName(), equalTo( "j1_0" ) );

		assertThat( result.getTableSpecificationJoins().size(), equalTo( 2 ) );

		assertThat(
				result.getTableSpecificationJoins().get( 0 ).getJoinedTable(),
				instanceOf( PhysicalTableSpecification.class )
		);
		final PhysicalTableSpecification firstSubclassTable = (PhysicalTableSpecification) result.getTableSpecificationJoins().get( 0 ).getJoinedTable();
		assertThat( firstSubclassTable.getTableName(), equalTo( "joined_entity_base" ) );
		assertThat( firstSubclassTable.getCorrelationName(), equalTo( "j1_1" ) );

		assertThat(
				result.getTableSpecificationJoins().get( 1 ).getJoinedTable(),
				instanceOf( PhysicalTableSpecification.class )
		);
		final PhysicalTableSpecification secondSubclassTable = (PhysicalTableSpecification) result.getTableSpecificationJoins().get( 1 ).getJoinedTable();
		assertThat( secondSubclassTable.getTableName(), equalTo( "joined_entity_leaf" ) );
		assertThat( secondSubclassTable.getCorrelationName(), equalTo( "j1_2" ) );
	}

	@Test
	public void testSingleSpaceLeaf() {
		SelectStatement sqm = (SelectStatement) interpret( "from JoinedEntityLeaf" );

		final EntityTypeDescriptorImpl entityTypeDescriptor =
				(EntityTypeDescriptorImpl) getConsumerContext().resolveEntityReference( "JoinedEntityLeaf" );
		final ImprovedEntityPersister improvedEntityPersister = entityTypeDescriptor.getPersister();
		assertThat( improvedEntityPersister.getEntityPersister(), instanceOf( JoinedSubclassEntityPersister.class ) );

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
		assertThat( result.getAliasBase(), equalTo( "j1" ) );

		assertThat( result.getRootTableSpecification(), notNullValue() );
		assertThat( result.getRootTableSpecification(), instanceOf( PhysicalTableSpecification.class ) );
		final PhysicalTableSpecification tableSpec = (PhysicalTableSpecification) result.getRootTableSpecification();
		assertThat( tableSpec.getTableName(), equalTo( "joined_entity_leaf" ) );
		assertThat( tableSpec.getCorrelationName(), equalTo( "j1_0" ) );

		assertThat( result.getTableSpecificationJoins().size(), equalTo( 2 ) );

		assertThat(
				result.getTableSpecificationJoins().get( 0 ).getJoinedTable(),
				instanceOf( PhysicalTableSpecification.class )
		);
		final PhysicalTableSpecification firstSubclassTable = (PhysicalTableSpecification) result.getTableSpecificationJoins().get( 0 ).getJoinedTable();
		assertThat( firstSubclassTable.getTableName(), equalTo( "joined_entity_branch" ) );
		assertThat( firstSubclassTable.getCorrelationName(), equalTo( "j1_1" ) );

		assertThat(
				result.getTableSpecificationJoins().get( 1 ).getJoinedTable(),
				instanceOf( PhysicalTableSpecification.class )
		);
		final PhysicalTableSpecification secondSubclassTable = (PhysicalTableSpecification) result.getTableSpecificationJoins().get( 1 ).getJoinedTable();
		assertThat( secondSubclassTable.getTableName(), equalTo( "joined_entity_base" ) );
		assertThat( secondSubclassTable.getCorrelationName(), equalTo( "j1_2" ) );
	}


	@Override
	protected void applyMetadataSources(MetadataSources metadataSources) {
		metadataSources.addAnnotatedClass( JoinedEntityBase.class );
		metadataSources.addAnnotatedClass( JoinedEntityBranch.class );
		metadataSources.addAnnotatedClass( JoinedEntityLeaf.class );
	}

	@Entity( name = "JoinedEntityBase" )
	@Table( name = "joined_entity_base" )
	@Inheritance( strategy = InheritanceType.JOINED )
	public static class JoinedEntityBase {
		@Id
		public Integer id;
		public String name;
		public String description;
	}

	@Entity( name = "JoinedEntityBranch" )
	@Table( name = "joined_entity_branch" )
	public static class JoinedEntityBranch extends JoinedEntityBase {
		public String branchSpecificState;
	}

	@Entity( name = "JoinedEntityLeaf" )
	@Table( name = "joined_entity_leaf" )
	public static class JoinedEntityLeaf extends JoinedEntityBranch {
		public String leafSpecificState;
	}
}
