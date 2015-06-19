/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.hql.parser.antlr;

import java.util.HashMap;
import java.util.Map;

import org.hibernate.hql.parser.ParsingContext;
import org.hibernate.hql.parser.semantic.Statement;
import org.hibernate.hql.parser.semantic.from.FromClause;
import org.hibernate.hql.parser.semantic.from.FromElementSpace;

/**
 * @author Steve Ebersole
 */
public class ExplicitFromClauseIndexer extends HqlParserBaseListener {
	private final ParsingContext parsingContext;
	private final FromClause rootFromClause;

	private Statement.Type statementType;

	// Using HqlParser.QuerySpecContext direct did not work in my experience, each walk
	// seems to build new instances.  So use the context text as key :(
	private final Map<String, FromClause> fromClauseMap = new HashMap<String, FromClause>();

	public ExplicitFromClauseIndexer(ParsingContext parsingContext) {
		this.parsingContext = parsingContext;
		this.rootFromClause = new FromClause( parsingContext );
	}

	public Statement.Type getStatementType() {
		return statementType;
	}

	public FromClause getRootFromClause() {
		return rootFromClause;
	}

	public FromClause findFromClauseForQuerySpec(HqlParser.QuerySpecContext ctx) {
		return fromClauseMap.get( ctx.getText() );
	}

	@Override
	public void enterSelectStatement(HqlParser.SelectStatementContext ctx) {
		statementType = Statement.Type.SELECT;
	}

	@Override
	public void enterInsertStatement(HqlParser.InsertStatementContext ctx) {
		statementType = Statement.Type.INSERT;
	}

	@Override
	public void enterUpdateStatement(HqlParser.UpdateStatementContext ctx) {
		statementType = Statement.Type.UPDATE;
	}

	@Override
	public void enterDeleteStatement(HqlParser.DeleteStatementContext ctx) {
		statementType = Statement.Type.DELETE;
	}

	private FromClause currentFromClause;

	@Override
	public void enterFromClause(HqlParser.FromClauseContext ctx) {
		super.enterFromClause( ctx );

		if ( currentFromClause == null ) {
			currentFromClause = rootFromClause;
		}
		else {
			currentFromClause = currentFromClause.makeChildFromClause();
		}
	}

	@Override
	public void exitQuerySpec(HqlParser.QuerySpecContext ctx) {
		fromClauseMap.put( ctx.getText(), currentFromClause );

		if ( currentFromClause == null ) {
			throw new IllegalStateException( "Mismatch currentFromClause handling" );
		}
		currentFromClause = currentFromClause.getParentFromClause();
	}

//	@Override
//	public void exitFromClause(HqlParser.FromClauseContext ctx) {
//		if ( currentFromClause == null ) {
//			throw new IllegalStateException( "Mismatch currentFromClause handling" );
//		}
//		currentFromClause = currentFromClause.getParentFromClause();
//	}

	private FromElementSpace currentFromElementSpace;

	@Override
	public void enterFromElementSpace(HqlParser.FromElementSpaceContext ctx) {
		currentFromElementSpace = currentFromClause.makeFromElementSpace();
	}

	@Override
	public void exitFromElementSpace(HqlParser.FromElementSpaceContext ctx) {
		currentFromElementSpace.complete();
		currentFromElementSpace = null;
	}

	@Override
	public void enterRootEntityReference(HqlParser.RootEntityReferenceContext ctx) {
		currentFromElementSpace.makeFromElement( ctx );
	}

	@Override
	public void enterCrossJoin(HqlParser.CrossJoinContext ctx) {
		currentFromElementSpace.makeFromElement( ctx );
	}

	@Override
	public void enterImplicitInnerJoin(HqlParser.ImplicitInnerJoinContext ctx) {
		currentFromElementSpace.makeFromElement( ctx );
	}

	@Override
	public void enterExplicitInnerJoin(HqlParser.ExplicitInnerJoinContext ctx) {
		currentFromElementSpace.makeFromElement( ctx );
	}

	@Override
	public void enterExplicitOuterJoin(HqlParser.ExplicitOuterJoinContext ctx) {
		currentFromElementSpace.makeFromElement( ctx );
	}
}
