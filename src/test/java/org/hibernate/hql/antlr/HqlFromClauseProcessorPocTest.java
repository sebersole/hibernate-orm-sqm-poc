/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.hql.antlr;

import org.hibernate.hql.ImplicitAliasGenerator;
import org.hibernate.hql.JoinType;
import org.hibernate.hql.model.ModelMetadata;
import org.hibernate.hql.ParsingContext;
import org.hibernate.hql.antlr.normalization.ExplicitFromClauseIndexer;
import org.hibernate.hql.antlr.normalization.FromClause;
import org.hibernate.hql.antlr.normalization.FromElement;
import org.hibernate.hql.antlr.normalization.FromElementQualifiedAttributeJoinImpl;
import org.hibernate.hql.antlr.normalization.FromElementSpace;

import org.junit.Test;

import org.antlr.v4.runtime.tree.ParseTreeWalker;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 * Initial work on a "from clause processor"
 *
 * @author Steve Ebersole
 */
public class HqlFromClauseProcessorPocTest {
	@Test
	public void testSimpleFrom() throws Exception {
		final HqlParser parser = HqlParseTreeBuilder.INSTANCE.parseHql( "select a.b from Something a" );
		final ExplicitFromClauseIndexer explicitFromClauseIndexer = processFromClause( parser );
		final FromClause fromClause1 = explicitFromClauseIndexer.getRootFromClause();
		assertNotNull( fromClause1 );
		assertEquals( 0, fromClause1.getChildFromClauses().size() );
		assertEquals( 1, fromClause1.getFromElementSpaces().size() );
		FromElementSpace space1 = fromClause1.getFromElementSpaces().get( 0 );
		assertNotNull( space1 );
		assertNotNull( space1.getRoot() );
		assertEquals( 0, space1.getJoins().size() );
		FromElement fromElement = fromClause1.findFromElementByAlias( "a" );
		assertNotNull( fromElement );
		assertSame( fromElement, space1.getRoot() );
	}

	private ExplicitFromClauseIndexer processFromClause(HqlParser parser) {
		final ExplicitFromClauseIndexer explicitFromClauseIndexer = new ExplicitFromClauseIndexer( new ParsingContextImpl() );
		ParseTreeWalker.DEFAULT.walk( explicitFromClauseIndexer, parser.statement() );
		return explicitFromClauseIndexer;
	}

	@Test
	public void testMultipleSpaces() throws Exception {
		final HqlParser parser = HqlParseTreeBuilder.INSTANCE.parseHql( "select a.b from Something a, SomethingElse b" );
		final ExplicitFromClauseIndexer explicitFromClauseIndexer = processFromClause( parser );
		final FromClause fromClause1 = explicitFromClauseIndexer.getRootFromClause();
		assertNotNull( fromClause1 );
		assertEquals( 0, fromClause1.getChildFromClauses().size() );
		assertEquals( 2, fromClause1.getFromElementSpaces().size() );
		FromElementSpace space1 = fromClause1.getFromElementSpaces().get( 0 );
		FromElementSpace space2 = fromClause1.getFromElementSpaces().get( 1 );
		assertNotNull( space1.getRoot() );
		assertEquals( 0, space1.getJoins().size() );
		assertNotNull( space2.getRoot() );
		assertEquals( 0, space2.getJoins().size() );
		FromElement fromElementA = fromClause1.findFromElementByAlias( "a" );
		assertNotNull( fromElementA );
		FromElement fromElementB = fromClause1.findFromElementByAlias( "b" );
		assertNotNull( fromElementB );
		assertNotEquals( fromElementA, fromElementB );
	}

	@Test
	public void testImplicitAlias() throws Exception {
		final HqlParser parser = HqlParseTreeBuilder.INSTANCE.parseHql( "select b from Something" );
		final ExplicitFromClauseIndexer explicitFromClauseIndexer = processFromClause( parser );
		final FromClause fromClause1 = explicitFromClauseIndexer.getRootFromClause();
		assertNotNull( fromClause1 );
		assertEquals( 0, fromClause1.getChildFromClauses().size() );
		assertEquals( 1, fromClause1.getFromElementSpaces().size() );
		FromElementSpace space1 = fromClause1.getFromElementSpaces().get( 0 );
		assertNotNull( space1 );
		assertNotNull( space1.getRoot() );
		assertEquals( 0, space1.getJoins().size() );
		assertTrue( ImplicitAliasGenerator.isImplicitAlias( space1.getRoot().getAlias() ) );
		FromElement fromElement = fromClause1.findFromElementByAlias( space1.getRoot().getAlias() );
		assertSame( space1.getRoot(), fromElement );
	}

	@Test
	public void testCrossJoin() throws Exception {
		final HqlParser parser = HqlParseTreeBuilder.INSTANCE.parseHql( "select a.b from Something a cross join SomethingElse b" );
		final ExplicitFromClauseIndexer explicitFromClauseIndexer = processFromClause( parser );
		final FromClause fromClause1 = explicitFromClauseIndexer.getRootFromClause();
		assertNotNull( fromClause1 );
		assertEquals( 0, fromClause1.getChildFromClauses().size() );
		assertEquals( 1, fromClause1.getFromElementSpaces().size() );
		FromElementSpace space1 = fromClause1.getFromElementSpaces().get( 0 );
		assertNotNull( space1 );
		assertNotNull( space1.getRoot() );
		assertEquals( 1, space1.getJoins().size() );

		FromElement fromElementA = fromClause1.findFromElementByAlias( "a" );
		assertNotNull( fromElementA );
		assertSame( space1.getRoot(), fromElementA );

		FromElement fromElementB = fromClause1.findFromElementByAlias( "b" );
		assertNotNull( fromElementB );
		assertSame( space1.getJoins().get( 0 ), fromElementB );
	}

	@Test
	public void testSimpleImplicitInnerJoin() throws Exception {
		simpleJoinAssertions(
				HqlParseTreeBuilder.INSTANCE.parseHql( "select a.b from Something a join a.c c" ),
				JoinType.INNER
		);
	}

	private void simpleJoinAssertions(HqlParser parser, JoinType joinType) {
		final ExplicitFromClauseIndexer explicitFromClauseIndexer = processFromClause( parser );
		final FromClause fromClause1 = explicitFromClauseIndexer.getRootFromClause();
		assertNotNull( fromClause1 );
		assertEquals( 0, fromClause1.getChildFromClauses().size() );
		assertEquals( 1, fromClause1.getFromElementSpaces().size() );
		FromElementSpace space1 = fromClause1.getFromElementSpaces().get( 0 );
		assertNotNull( space1 );
		assertNotNull( space1.getRoot() );
		assertEquals( 1, space1.getJoins().size() );

		FromElement fromElementA = fromClause1.findFromElementByAlias( "a" );
		assertNotNull( fromElementA );
		assertSame( space1.getRoot(), fromElementA );

		FromElement fromElementC = fromClause1.findFromElementByAlias( "c" );
		assertNotNull( fromElementC );
		assertSame( space1.getJoins().get( 0 ), fromElementC );
		FromElementQualifiedAttributeJoinImpl join = (FromElementQualifiedAttributeJoinImpl) fromElementC;
		assertEquals( joinType, join.getJoinType() );
		assertEquals( "c", join.getAlias() );
		assertEquals( "a.c", join.getJoinedAttribute() );
	}

	@Test
	public void testSimpleExplicitInnerJoin() throws Exception {
		simpleJoinAssertions(
				HqlParseTreeBuilder.INSTANCE.parseHql( "select a.b from Something a inner join a.c c" ),
				JoinType.INNER
		);
	}

	@Test
	public void testSimpleExplicitOuterJoin() throws Exception {
		simpleJoinAssertions(
				HqlParseTreeBuilder.INSTANCE.parseHql( "select a.b from Something a outer join a.c c" ),
				JoinType.LEFT
		);
	}

	@Test
	public void testSimpleExplicitLeftOuterJoin() throws Exception {
		simpleJoinAssertions(
				HqlParseTreeBuilder.INSTANCE.parseHql( "select a.b from Something a left outer join a.c c" ),
				JoinType.LEFT
		);
	}

	private static class ParsingContextImpl implements ParsingContext {
		private final ImplicitAliasGenerator implicitAliasGenerator = new ImplicitAliasGenerator();

		@Override
		public ModelMetadata getModelMetadata() {
			return null;
		}

		@Override
		public ImplicitAliasGenerator getImplicitAliasGenerator() {
			return implicitAliasGenerator;
		}
	}
}
