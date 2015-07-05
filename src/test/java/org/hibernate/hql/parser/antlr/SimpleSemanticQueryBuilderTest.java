/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.hql.parser.antlr;

import java.util.Collection;

import org.hibernate.hql.parser.semantic.QuerySpec;
import org.hibernate.hql.parser.semantic.SelectStatement;
import org.hibernate.hql.parser.semantic.expression.LiteralIntegerExpression;
import org.hibernate.hql.parser.semantic.expression.LiteralLongExpression;

import org.junit.Test;

import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.xpath.XPath;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Steve Ebersole
 */
public class SimpleSemanticQueryBuilderTest {
	@Test
	public void simpleIntegerLiteralsTest() {
		final ParsingContextTestingImpl parsingContext = new ParsingContextTestingImpl();

		final HqlParser parser = HqlParseTreeBuilder.INSTANCE.parseHql( "select a.basic from Something a where 1=2" );

		final ExplicitFromClauseIndexer explicitFromClauseIndexer = new ExplicitFromClauseIndexer( new ParsingContextTestingImpl() );
		ParseTreeWalker.DEFAULT.walk( explicitFromClauseIndexer, parser.statement() );

		parser.reset();

		Collection<ParseTree> logicalExpressions = XPath.findAll( parser.statement(), "//predicate", parser );
		assertEquals( 1, logicalExpressions.size() );
		ParseTree logicalExpression = logicalExpressions.iterator().next();
		// 3 -> the 2 expressions, plus the operand (=)
		assertEquals( 3, logicalExpression.getChildCount() );

		SemanticQueryBuilder semanticQueryBuilder = new SemanticQueryBuilder( parsingContext, explicitFromClauseIndexer );
		Object lhs = logicalExpression.getChild( 0 ).accept( semanticQueryBuilder );
		assertNotNull( lhs );
		assertTrue( lhs instanceof LiteralIntegerExpression );
		assertEquals( 1, ( (LiteralIntegerExpression) lhs ).getLiteralValue().intValue() );

		Object rhs = logicalExpression.getChild( 2 ).accept( semanticQueryBuilder );
		assertNotNull( rhs );
		assertTrue( rhs instanceof LiteralIntegerExpression );
		assertEquals( 2, ( (LiteralIntegerExpression) rhs ).getLiteralValue().intValue() );

		parser.reset();

		semanticQueryBuilder = new SemanticQueryBuilder( parsingContext, explicitFromClauseIndexer );
		SelectStatement selectStatement = semanticQueryBuilder.visitSelectStatement( parser.selectStatement() );
		selectStatement.getQuerySpec();
	}

	@Test
	public void simpleLongLiteralsTest() {
		final ParsingContextTestingImpl parsingContext = new ParsingContextTestingImpl();

		final HqlParser parser = HqlParseTreeBuilder.INSTANCE.parseHql( "select a.basic from Something a where 1L=2L" );

		final ExplicitFromClauseIndexer explicitFromClauseIndexer = new ExplicitFromClauseIndexer( new ParsingContextTestingImpl() );
		ParseTreeWalker.DEFAULT.walk( explicitFromClauseIndexer, parser.statement() );

		parser.reset();

		Collection<ParseTree> logicalExpressions = XPath.findAll( parser.statement(), "//predicate", parser );
		assertEquals( 1, logicalExpressions.size() );
		ParseTree logicalExpression = logicalExpressions.iterator().next();
		// 3 -> the 2 expressions, plus the operand (=)
		assertEquals( 3, logicalExpression.getChildCount() );

		SemanticQueryBuilder semanticQueryBuilder = new SemanticQueryBuilder( parsingContext, explicitFromClauseIndexer );
		Object lhs = logicalExpression.getChild( 0 ).accept( semanticQueryBuilder );
		assertNotNull( lhs );
		assertTrue( lhs instanceof LiteralLongExpression );
		assertEquals( 1L, ( (LiteralLongExpression) lhs ).getLiteralValue().longValue() );

		Object rhs = logicalExpression.getChild( 2 ).accept( semanticQueryBuilder );
		assertNotNull( rhs );
		assertTrue( rhs instanceof LiteralLongExpression );
		assertEquals( 2L, ( (LiteralLongExpression) rhs ).getLiteralValue().longValue() );

	}


	@Test
	public void testAttributeJoinWithOnClause() throws Exception {
		final ParsingContextTestingImpl parsingContext = new ParsingContextTestingImpl();

		final HqlParser parser = HqlParseTreeBuilder.INSTANCE.parseHql( "select a from Something a left outer join a.entity c on c.basic1 > 5 and c.basic2 < 20 " );

		final ExplicitFromClauseIndexer explicitFromClauseIndexer = new ExplicitFromClauseIndexer( new ParsingContextTestingImpl() );
		ParseTreeWalker.DEFAULT.walk( explicitFromClauseIndexer, parser.statement() );

		parser.reset();

		SemanticQueryBuilder semanticQueryBuilder = new SemanticQueryBuilder( parsingContext, explicitFromClauseIndexer );
		SelectStatement selectStatement = semanticQueryBuilder.visitSelectStatement( parser.selectStatement() );
		QuerySpec querySpec = selectStatement.getQuerySpec();
		assertNotNull( querySpec );
	}


	@Test
	public void testSimpleDynamicInstantiation() throws Exception {
		final ParsingContextTestingImpl parsingContext = new ParsingContextTestingImpl();

		final HqlParser parser = HqlParseTreeBuilder.INSTANCE.parseHql( "select new org.hibernate.hql.parser.antlr.SimpleSemanticQueryBuilderTest$DTO(a.basic1 as id, a.basic2 as name) from Something a" );

		final ExplicitFromClauseIndexer explicitFromClauseIndexer = new ExplicitFromClauseIndexer( new ParsingContextTestingImpl() );
		ParseTreeWalker.DEFAULT.walk( explicitFromClauseIndexer, parser.statement() );

		parser.reset();

		SemanticQueryBuilder semanticQueryBuilder = new SemanticQueryBuilder( parsingContext, explicitFromClauseIndexer );
		SelectStatement selectStatement = semanticQueryBuilder.visitSelectStatement( parser.selectStatement() );
		QuerySpec querySpec = selectStatement.getQuerySpec();
		assertNotNull( querySpec );
	}

	private static class DTO {
	}
}
