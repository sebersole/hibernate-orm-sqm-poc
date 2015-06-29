/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.hql.parser.antlr;

import java.util.HashMap;
import java.util.Map;

import org.hibernate.hql.parser.JoinType;
import org.hibernate.hql.parser.ParsingContext;
import org.hibernate.hql.parser.ParsingException;
import org.hibernate.hql.parser.SemanticException;
import org.hibernate.hql.parser.antlr.path.AttributePathPart;
import org.hibernate.hql.parser.antlr.path.AttributePathResolver;
import org.hibernate.hql.parser.antlr.path.BasicAttributePathResolverImpl;
import org.hibernate.hql.parser.antlr.path.AttributePathResolverStack;
import org.hibernate.hql.parser.antlr.path.JoinPredicatePathResolverImpl;
import org.hibernate.hql.parser.model.AttributeDescriptor;
import org.hibernate.hql.parser.model.EntityTypeDescriptor;
import org.hibernate.hql.parser.semantic.Statement;
import org.hibernate.hql.parser.semantic.from.FromClause;
import org.hibernate.hql.parser.semantic.from.FromElement;
import org.hibernate.hql.parser.semantic.from.FromElementSpace;
import org.hibernate.hql.parser.semantic.from.JoinedFromElement;

import org.antlr.v4.runtime.tree.TerminalNode;

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
		currentFromElementSpace.makeRootEntityFromElement(
				resolveEntityReference( ctx.mainEntityPersisterReference().dotIdentifierSequence() ),
				interpretAlias( ctx.mainEntityPersisterReference().IDENTIFIER() )
		);
	}

	private EntityTypeDescriptor resolveEntityReference(HqlParser.DotIdentifierSequenceContext dotIdentifierSequenceContext) {
		final String entityName = dotIdentifierSequenceContext.getText();
		final EntityTypeDescriptor entityTypeDescriptor = parsingContext.getConsumerContext().resolveEntityReference( entityName );
		if ( entityTypeDescriptor == null ) {
			throw new SemanticException( "Unresolved entity name : " + entityName );
		}
		return entityTypeDescriptor;
	}

	private String interpretAlias(TerminalNode aliasNode) {
		if ( aliasNode == null ) {
			return parsingContext.getImplicitAliasGenerator().buildUniqueImplicitAlias();
		}
		assert aliasNode.getSymbol().getType() == HqlParser.IDENTIFIER;
		return aliasNode.getText();
	}

	@Override
	public void enterCrossJoin(HqlParser.CrossJoinContext ctx) {
		currentFromElementSpace.makeCrossJoinedFromElement(
				resolveEntityReference( ctx.mainEntityPersisterReference().dotIdentifierSequence() ),
				interpretAlias( ctx.mainEntityPersisterReference().IDENTIFIER() )
		);
	}

	@Override
	public void enterImplicitInnerJoin(final HqlParser.ImplicitInnerJoinContext ctx) {
		final QualifiedJoinTreeVisitor visitor = new QualifiedJoinTreeVisitor(
				parsingContext,
				currentFromElementSpace,
				JoinType.INNER,
				interpretAlias( ctx.qualifiedJoinRhs().IDENTIFIER() ),
				ctx.fetchKeyword() != null
		);

		JoinedFromElement joinedPath = (JoinedFromElement) ctx.qualifiedJoinRhs().path().accept( visitor );

		if ( joinedPath == null ) {
			throw new ParsingException( "Could not resolve join path : " + ctx.qualifiedJoinRhs().getText() );
		}

		// we'll handle on-clause predicates in a later pass
	}

	@Override
	public void enterExplicitInnerJoin(HqlParser.ExplicitInnerJoinContext ctx) {
		final QualifiedJoinTreeVisitor visitor = new QualifiedJoinTreeVisitor(
				parsingContext,
				currentFromElementSpace,
				JoinType.INNER,
				interpretAlias( ctx.qualifiedJoinRhs().IDENTIFIER() ),
				ctx.fetchKeyword() != null
		);

		JoinedFromElement joinedPath = (JoinedFromElement) ctx.qualifiedJoinRhs().path().accept( visitor );

		if ( joinedPath == null ) {
			throw new ParsingException( "Could not resolve join path : " + ctx.qualifiedJoinRhs().getText() );
		}

		// we'll handle on-clause predicates in a later pass
	}

	@Override
	public void enterExplicitOuterJoin(HqlParser.ExplicitOuterJoinContext ctx) {
		final QualifiedJoinTreeVisitor visitor = new QualifiedJoinTreeVisitor(
				parsingContext,
				currentFromElementSpace,
				// currently only left outer joins are supported
				JoinType.LEFT,
				interpretAlias( ctx.qualifiedJoinRhs().IDENTIFIER() ),
				ctx.fetchKeyword() != null
		);

		JoinedFromElement joinedPath = (JoinedFromElement) ctx.qualifiedJoinRhs().path().accept( visitor );

		if ( joinedPath == null ) {
			throw new ParsingException( "Could not resolve join path : " + ctx.qualifiedJoinRhs().getText() );
		}

		// we'll handle on-clause predicates in a later pass
	}

	private static class QualifiedJoinTreeVisitor extends AbstractHqlParseTreeVisitor {
		private final FromElementSpace fromElementSpace;

		private final AttributePathResolverStack resolverStack = new AttributePathResolverStack();

		public QualifiedJoinTreeVisitor(
				ParsingContext parsingContext,
				FromElementSpace fromElementSpace,
				JoinType joinType,
				String alias,
				boolean fetched) {
			super( parsingContext );

			this.fromElementSpace = fromElementSpace;

			this.resolverStack.push( new JoinAttributePathResolver( fromElementSpace, joinType, alias, fetched ) );
		}

		@Override
		public FromClause getCurrentFromClause() {
			return fromElementSpace.getFromClause();
		}

		@Override
		public AttributePathResolver getCurrentAttributePathResolver() {
			return resolverStack.getCurrent();
		}
	}

	private static class JoinAttributePathResolver extends BasicAttributePathResolverImpl {
		private final FromElementSpace fromElementSpace;
		private final JoinType joinType;
		private final String alias;
		private final boolean fetched;

		public JoinAttributePathResolver(
				FromElementSpace fromElementSpace,
				JoinType joinType,
				String alias,
				boolean fetched) {
			super( fromElementSpace.getFromClause() );
			this.fromElementSpace = fromElementSpace;
			this.joinType = joinType;
			this.alias = alias;
			this.fetched = fetched;
		}

		@Override
		protected JoinType getIntermediateJoinType() {
			return joinType;
		}

		protected boolean areIntermediateJoinsFetched() {
			return fetched;
		}

		@Override
		protected AttributePathPart resolveTerminalPathPart(FromElement lhs, String terminalName) {
			return fromElementSpace.buildAttributeJoin(
					lhs,
					resolveAttributeDescriptor( lhs, terminalName ),
					alias,
					joinType,
					fetched
			);
		}

		protected AttributeDescriptor resolveAttributeDescriptor(FromElement lhs, String attributeName) {
			final AttributeDescriptor attributeDescriptor = lhs.getTypeDescriptor().getAttributeDescriptor( attributeName );
			if ( attributeDescriptor == null ) {
				throw new SemanticException(
						"Name [" + attributeName + "] is not a valid attribute on from-element [" +
								lhs.getTypeDescriptor().getTypeName() + "]"
				);
			}

			return attributeDescriptor;
		}

		@Override
		protected AttributePathPart resolveFromElementAliasAsTerminal(FromElement aliasedFromElement) {
			return aliasedFromElement;
		}
	}

}
