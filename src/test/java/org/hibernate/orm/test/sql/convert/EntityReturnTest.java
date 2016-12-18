/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.orm.test.sql.convert;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import org.hibernate.boot.MetadataSources;
import org.hibernate.sql.ast.SelectQuery;
import org.hibernate.sql.ast.select.Selection;
import org.hibernate.sql.convert.results.spi.ReturnEntity;
import org.hibernate.sql.convert.spi.SqmSelectInterpretation;
import org.hibernate.orm.test.sql.BaseUnitTest;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Steve Ebersole
 */
public class EntityReturnTest extends BaseUnitTest {

	@Test
	public void testSelectingIdentificationVariable() {
		final SqmSelectInterpretation interpretation = interpretSelectQuery( "select e from Employee e" );
		final SelectQuery sqlAstQuery = interpretation.getSqlSelectAst();

		assertThat(	sqlAstQuery.getQuerySpec().getSelectClause().getSelections().size(), is(1) );
		assertThat(	interpretation.getQueryReturns().size(), is(1) );

		// make sure the cast is valid
		@SuppressWarnings("unused") final ReturnEntity entityReturn = (ReturnEntity) interpretation.getQueryReturns().get( 0 );
	}

	@Test
	public void testSelectingManyToOneAttribute() {
		final SqmSelectInterpretation interpretation = interpretSelectQuery( "select e.manager from Employee e" );
		final SelectQuery sqlAstQuery = interpretation.getSqlSelectAst();

		assertThat(	sqlAstQuery.getQuerySpec().getSelectClause().getSelections().size(), is(1) );
		assertThat(	interpretation.getQueryReturns().size(), is(1) );

		final Selection selection = sqlAstQuery.getQuerySpec().getSelectClause().getSelections().get( 0 );
		final ReturnEntity entityReturn = (ReturnEntity) interpretation.getQueryReturns().get( 0 );

		assertThat( entityReturn.getSelectedExpression(), sameInstance( selection.getSelectExpression() ) );
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
