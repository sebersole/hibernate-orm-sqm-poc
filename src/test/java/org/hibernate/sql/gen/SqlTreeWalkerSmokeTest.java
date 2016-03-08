/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.gen;

import javax.persistence.Entity;
import javax.persistence.Id;

import org.hibernate.boot.MetadataSources;
import org.hibernate.engine.jdbc.internal.FormatStyle;
import org.hibernate.engine.jdbc.internal.Formatter;
import org.hibernate.sql.ast.SelectQuery;
import org.hibernate.sql.gen.internal.SelectStatementInterpreter;
import org.hibernate.sqm.SemanticQueryInterpreter;
import org.hibernate.sqm.query.SelectStatement;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * @author Steve Ebersole
 */
public class SqlTreeWalkerSmokeTest extends BaseUnitTest {
	@Override
	protected void applyMetadataSources(MetadataSources metadataSources) {
		super.applyMetadataSources( metadataSources );
		metadataSources.addAnnotatedClass( Person.class );
	}

	@Test
	public void testSqlTreeWalking() {
		SelectQuery sqlTree = interpretSelectQuery( "select p.name from Person p" );
		SqlTreeWalker sqlTreeWalker = new SqlTreeWalker( getSessionFactory() );
		sqlTreeWalker.visitSelectQuery( sqlTree );

		System.out.println( FormatStyle.BASIC.getFormatter().format( sqlTreeWalker.getSql() ) );

		assertThat( sqlTreeWalker.getSql(), notNullValue() );
	}

	@Entity(name="Person")
	public static class Person {
		@Id
		Integer id;
		String name;
	}
}
