/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.convert;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import org.hibernate.boot.MetadataSources;
import org.hibernate.sql.ast.SelectQuery;
import org.hibernate.sql.ast.from.TableSpace;
import org.hibernate.sql.ast.select.Selection;
import org.hibernate.sql.convert.results.spi.Fetch;
import org.hibernate.sql.convert.results.spi.FetchEntityAttribute;
import org.hibernate.sql.convert.results.spi.Return;
import org.hibernate.sql.convert.results.spi.ReturnEntity;
import org.hibernate.sql.gen.BaseUnitTest;

import org.hibernate.testing.FailureExpected;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Steve Ebersole
 */
public class EntityFetchesTest extends BaseUnitTest {
	@Test
	public void testBasicOneToOneFetching() {
		final SelectQuery sqlAstQuery = interpretSelectQuery( "select t from Trunk t join fetch t.tree" );

		assertThat(	sqlAstQuery.getQuerySpec().getSelectClause().getSelections().size(), is(1) );
		final Selection selection = sqlAstQuery.getQuerySpec().getSelectClause().getSelections().get( 0 );
		final Return queryReturn = selection.getQueryReturn();
		assertThat( queryReturn, instanceOf( ReturnEntity.class ) );
		final ReturnEntity entity = (ReturnEntity) queryReturn;
		assertThat( entity.getFetches().size(), is(1) );
	}
	@Test
	public void testNestedFetching() {
		final SelectQuery sqlAstQuery = interpretSelectQuery( "select t from Trunk t join fetch t.tree t2 join fetch t2.forest" );

		assertThat(	sqlAstQuery.getQuerySpec().getSelectClause().getSelections().size(), is(1) );
		final Selection selection = sqlAstQuery.getQuerySpec().getSelectClause().getSelections().get( 0 );
		final Return queryReturn = selection.getQueryReturn();
		assertThat( queryReturn, instanceOf( ReturnEntity.class ) );
		final ReturnEntity entity = (ReturnEntity) queryReturn;

		assertThat( entity.getFetches().size(), is(1) );
		FetchEntityAttribute treeFetch = (FetchEntityAttribute) entity.getFetches().get( 0 );

		assertThat( treeFetch.getFetches().size(), is(1) );
		FetchEntityAttribute forestFetch = (FetchEntityAttribute) entity.getFetches().get( 0 );
	}

	@Test
	@FailureExpected( jiraKey = "n/a" )
	public void testBasicOneToOneInverseFetching() {
		final SelectQuery sqlAstQuery = interpretSelectQuery( "select t from Tree t join fetch t.trunk" );

		assertThat(	sqlAstQuery.getQuerySpec().getSelectClause().getSelections().size(), is(1) );
		final Selection selection = sqlAstQuery.getQuerySpec().getSelectClause().getSelections().get( 0 );
		final Return queryReturn = selection.getQueryReturn();
		assertThat( queryReturn, instanceOf( ReturnEntity.class ) );
		final ReturnEntity entity = (ReturnEntity) queryReturn;
		assertThat( entity.getFetches().size(), is(1) );
	}

	@Test
	public void testBasicManyToOneFetching() {
		final SelectQuery sqlAstQuery = interpretSelectQuery( "select t from Tree t join fetch t.forest" );

		assertThat(	sqlAstQuery.getQuerySpec().getSelectClause().getSelections().size(), is(1) );
		final Selection selection = sqlAstQuery.getQuerySpec().getSelectClause().getSelections().get( 0 );
		final Return queryReturn = selection.getQueryReturn();
		assertThat( queryReturn, instanceOf( ReturnEntity.class ) );
		final ReturnEntity entity = (ReturnEntity) queryReturn;
		assertThat( entity.getFetches().size(), is(1) );
	}

	@Override
	protected void applyMetadataSources(MetadataSources metadataSources) {
		super.applyMetadataSources( metadataSources );
		metadataSources.addAnnotatedClass( Tree.class );
		metadataSources.addAnnotatedClass( Forest.class );
		metadataSources.addAnnotatedClass( Trunk.class );
	}

	@Entity(name = "Tree")
	public static class Tree {
		@Id
		private Integer id;
		@ManyToOne
		private Forest forest;
		@OneToOne( mappedBy = "tree" )
		private Trunk trunk;
	}

	@Entity(name = "Trunk")
	public static class Trunk {
		@Id
		private Integer id;
		@OneToOne( orphanRemoval = true )
		private Tree tree;
	}

	@Entity(name = "Forest")
	public static class Forest {
		@Id
		private Integer id;
		private String name;
	}
}
