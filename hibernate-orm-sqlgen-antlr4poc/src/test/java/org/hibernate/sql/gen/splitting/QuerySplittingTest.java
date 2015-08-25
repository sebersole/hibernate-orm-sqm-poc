/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.sql.gen.splitting;

import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.query.parser.SemanticQueryInterpreter;
import org.hibernate.sql.gen.QuerySplitter;
import org.hibernate.sql.gen.sqm.ConsumerContextImpl;
import org.hibernate.sqm.query.SelectStatement;
import org.hibernate.sqm.query.Statement;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

/**
 * @author Steve Ebersole
 */
public class QuerySplittingTest {
	private SessionFactoryImplementor sessionFactory;

	@Before
	public void prepare() {
		sessionFactory = (SessionFactoryImplementor) new MetadataSources()
				.addAnnotatedClass( Account.class )
				.addAnnotatedClass( Fund.class )
				.buildMetadata()
				.buildSessionFactory();
	}

	@After
	public void release() {
		if ( sessionFactory == null ) {
			return;
		}

		sessionFactory.close();
	}

	@Test
	public void testQuerySplitting() {
		ConsumerContextImpl consumerContext = new ConsumerContextImpl( sessionFactory );

		// first try directly with the 2 mapped classes
		SelectStatement statement = (SelectStatement) SemanticQueryInterpreter.interpret( "from Account", consumerContext );
		Statement[] split = QuerySplitter.split( statement );
		assertEquals( 1, split.length );
		assertSame( statement, split[0] );

		statement = (SelectStatement) SemanticQueryInterpreter.interpret( "from Fund", consumerContext );
		split = QuerySplitter.split( statement );
		assertEquals( 1, split.length );
		assertSame( statement, split[0] );

		// Now try with an unmapped reference
		statement = (SelectStatement) SemanticQueryInterpreter.interpret( "from org.hibernate.sql.gen.splitting.Auditable", consumerContext );
		split = QuerySplitter.split( statement );
		assertEquals( 2, split.length );
	}
}
