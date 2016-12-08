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

import org.hibernate.boot.MetadataSources;
import org.hibernate.sql.ast.SelectQuery;
import org.hibernate.sql.ast.select.Selection;
import org.hibernate.sql.convert.results.spi.Return;
import org.hibernate.sql.convert.results.spi.ReturnEntity;
import org.hibernate.sql.gen.BaseUnitTest;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Steve Ebersole
 */
public class EntityReturnTest extends BaseUnitTest {

	@Test
	public void testSelectingIdentificationVariable() {
		final SelectQuery sqlAstQuery = interpretSelectQuery( "select e from Employee e" );

		assertThat(	sqlAstQuery.getQuerySpec().getSelectClause().getSelections().size(), is(1) );
		final Selection selection = sqlAstQuery.getQuerySpec().getSelectClause().getSelections().get( 0 );
		final Return queryReturn = selection.getQueryReturn();
		assertThat( queryReturn, instanceOf( ReturnEntity.class ) );
		final ReturnEntity entity = (ReturnEntity) queryReturn;
	}

	@Test
	public void testSelectingManyToOneAttribute() {
		final SelectQuery sqlAstQuery = interpretSelectQuery( "select e.manager from Employee e" );

		assertThat(	sqlAstQuery.getQuerySpec().getSelectClause().getSelections().size(), is(1) );
		final Selection selection = sqlAstQuery.getQuerySpec().getSelectClause().getSelections().get( 0 );
		final Return queryReturn = selection.getQueryReturn();
		assertThat( queryReturn, instanceOf( ReturnEntity.class ) );
		// purpose of this test is to ensure we get a ReturnEntity
		final ReturnEntity entity = (ReturnEntity) queryReturn;
	}

	@Override
	protected void applyMetadataSources(MetadataSources metadataSources) {
		super.applyMetadataSources( metadataSources );
		metadataSources.addAnnotatedClass( Employee.class );
	}

	@Entity(name = "Employee")
	public static class Employee {
		@Id
		private Integer id;

		@ManyToOne
		private Employee manager;
	}
}
