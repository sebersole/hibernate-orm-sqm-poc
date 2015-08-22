/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.query.parser;

import org.hibernate.hql.parser.antlr.HqlParser;
import org.hibernate.query.parser.internal.hql.phase2.SemanticQueryBuilder;
import org.hibernate.query.parser.internal.ParsingContext;
import org.hibernate.query.parser.internal.hql.HqlParseTreeBuilder;
import org.hibernate.query.parser.internal.hql.phase1.FromClauseProcessor;
import org.hibernate.sqm.query.Statement;

import org.antlr.v4.runtime.tree.ParseTreeWalker;

/**
 * Main entry point into the query parser.
 *
 * @author Steve Ebersole
 */
public class SemanticQueryInterpreter {
	public static Statement interpretQuery(String query, ConsumerContext consumerContext) {
		final ParsingContext parsingContext = new ParsingContext( consumerContext );

		// first, ask Antlr to build the parse tree
		final HqlParser parser = HqlParseTreeBuilder.INSTANCE.parseHql( query );

		// then we begin semantic analysis and building the semantic representation...

		// Phase 1
		FromClauseProcessor fromClauseProcessor = new FromClauseProcessor( parsingContext );
		ParseTreeWalker.DEFAULT.walk( fromClauseProcessor, parser.statement() );
		parser.reset();

		// Phase 2
		return new SemanticQueryBuilder( parsingContext, fromClauseProcessor ).visitStatement( parser.statement() );
	}
}
