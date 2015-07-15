/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.hql.parser.process;

import org.hibernate.hql.parser.NotYetImplementedException;
import org.hibernate.hql.parser.ParsingException;
import org.hibernate.hql.parser.antlr.HqlParser;
import org.hibernate.hql.parser.process.path.AttributePathPart;
import org.hibernate.hql.parser.process.path.BasicAttributePathResolverImpl;
import org.hibernate.hql.parser.semantic.QuerySpec;
import org.hibernate.hql.parser.semantic.Statement;
import org.hibernate.hql.parser.semantic.from.FromClause;

/**
 * @author Steve Ebersole
 */
public class SemanticQueryBuilder extends AbstractHqlParseTreeVisitor {
	private final ExplicitFromClauseIndexer fromClauseIndexer;

	private FromClause currentFromClause;

	public SemanticQueryBuilder(ParsingContext parsingContext, ExplicitFromClauseIndexer fromClauseIndexer) {
		super( parsingContext );
		this.fromClauseIndexer = fromClauseIndexer;

		if ( fromClauseIndexer.getStatementType() == Statement.Type.INSERT ) {
			throw new NotYetImplementedException();
			// set currentFromClause
		}
		else if ( fromClauseIndexer.getStatementType() == Statement.Type.UPDATE ) {
			throw new NotYetImplementedException();
			// set currentFromClause
		}
		else if ( fromClauseIndexer.getStatementType() == Statement.Type.DELETE ) {
			throw new NotYetImplementedException();
			// set currentFromClause
		}
	}

	@Override
	public FromClause getCurrentFromClause() {
		return currentFromClause;
	}

	@Override
	public Statement visitStatement(HqlParser.StatementContext ctx) {
		// for the moment, only selectStatements are valid...
		return visitSelectStatement( ctx.selectStatement() );
	}

	@Override
	public QuerySpec visitQuerySpec(HqlParser.QuerySpecContext ctx) {
		final FromClause fromClause = fromClauseIndexer.findFromClauseForQuerySpec( ctx );
		if ( fromClause == null ) {
			throw new ParsingException( "Could not resolve FromClause by QuerySpecContext" );
		}
		FromClause originalCurrentFromClause = currentFromClause;
		currentFromClause = fromClause;
		attributePathResolverStack.push( new BasicAttributePathResolverImpl( currentFromClause ) );
		try {
			return super.visitQuerySpec( ctx );
		}
		finally {
			attributePathResolverStack.pop();
			currentFromClause = originalCurrentFromClause;
		}
	}

	@Override
	public AttributePathPart visitIndexedPath(HqlParser.IndexedPathContext ctx) {
		return super.visitIndexedPath( ctx );
	}
}
