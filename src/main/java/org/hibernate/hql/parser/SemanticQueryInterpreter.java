/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.hql.parser;

import org.hibernate.hql.parser.process.ExplicitFromClauseIndexer;
import org.hibernate.hql.parser.process.HqlParseTreeBuilder;
import org.hibernate.hql.parser.antlr.HqlParser;
import org.hibernate.hql.parser.process.ImplicitAliasGenerator;
import org.hibernate.hql.parser.process.ParsingContext;
import org.hibernate.hql.parser.process.SemanticQueryBuilder;
import org.hibernate.hql.parser.semantic.Statement;

import org.antlr.v4.runtime.tree.ParseTreeWalker;

/**
 * Main entry point into the HQL query parser.  Pass in the HQL query string and a "context",
 * and get back a semantic representation of the query statement.
 *
 * @author Steve Ebersole
 */
public class SemanticQueryInterpreter {
	public static Statement interpretQuery(String query, ConsumerContext consumerContext) {
		// first, ask Antlr to build the parse tree
		final HqlParser parser = HqlParseTreeBuilder.INSTANCE.parseHql( query );

		// then we begin semantic analysis and building the semantic representation
		final ParsingContextImpl parsingContext = new ParsingContextImpl( consumerContext );

		final ExplicitFromClauseIndexer explicitFromClauseIndexer = new ExplicitFromClauseIndexer( parsingContext );
		ParseTreeWalker.DEFAULT.walk( explicitFromClauseIndexer, parser.statement() );
		parser.reset();

		return new SemanticQueryBuilder( parsingContext, explicitFromClauseIndexer ).visitStatement( parser.statement() );
	}

	private static class ParsingContextImpl implements ParsingContext {
		private final ConsumerContext consumerContext;
		private final ImplicitAliasGenerator aliasGenerator = new ImplicitAliasGenerator();

		public ParsingContextImpl(ConsumerContext consumerContext) {
			this.consumerContext = consumerContext;
		}

		@Override
		public ConsumerContext getConsumerContext() {
			return consumerContext;
		}

		@Override
		public ImplicitAliasGenerator getImplicitAliasGenerator() {
			return aliasGenerator;
		}
	}
}
