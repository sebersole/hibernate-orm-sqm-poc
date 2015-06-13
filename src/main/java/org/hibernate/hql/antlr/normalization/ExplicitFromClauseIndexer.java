/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.hql.antlr.normalization;

import org.hibernate.hql.ImplicitAliasGenerator;
import org.hibernate.hql.antlr.HqlParser;
import org.hibernate.hql.antlr.HqlParserBaseListener;

/**
 * @author Steve Ebersole
 */
public class ExplicitFromClauseIndexer extends HqlParserBaseListener implements NormalizationContext {
	private final ImplicitAliasGenerator aliasGenerator;

	public ExplicitFromClauseIndexer(ImplicitAliasGenerator aliasGenerator) {
		this.aliasGenerator = aliasGenerator;
	}

	@Override
	public ImplicitAliasGenerator getImplicitAliasGenerator() {
		return aliasGenerator;
	}

	public FromClause getRootFromClause() {
		return rootFromClause;
	}

	private FromClause rootFromClause = new FromClause( this );
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
	public void exitFromClause(HqlParser.FromClauseContext ctx) {
		if ( currentFromClause == null ) {
			throw new IllegalStateException( "Mismatch currentFromClause handling" );
		}
		currentFromClause = currentFromClause.getParentFromClause();
		super.exitFromClause( ctx );
	}

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
