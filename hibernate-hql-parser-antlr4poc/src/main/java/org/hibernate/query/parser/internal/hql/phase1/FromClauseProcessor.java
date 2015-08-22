/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.query.parser.internal.hql.phase1;

import java.util.HashMap;
import java.util.Map;

import org.hibernate.hql.parser.antlr.HqlParser;
import org.hibernate.hql.parser.antlr.HqlParserBaseListener;
import org.hibernate.query.parser.ParsingException;
import org.hibernate.query.parser.SemanticException;
import org.hibernate.query.parser.internal.FromClauseIndex;
import org.hibernate.query.parser.internal.FromElementBuilder;
import org.hibernate.query.parser.internal.ParsingContext;
import org.hibernate.query.parser.internal.hql.AbstractHqlParseTreeVisitor;
import org.hibernate.query.parser.internal.hql.path.AttributePathResolver;
import org.hibernate.query.parser.internal.hql.path.BasicAttributePathResolverImpl;
import org.hibernate.query.parser.internal.hql.path.JoinPredicatePathResolverImpl;
import org.hibernate.sqm.domain.AttributeDescriptor;
import org.hibernate.sqm.domain.EntityTypeDescriptor;
import org.hibernate.sqm.path.AttributePathPart;
import org.hibernate.sqm.query.JoinType;
import org.hibernate.sqm.query.Statement;
import org.hibernate.sqm.query.from.CrossJoinedFromElement;
import org.hibernate.sqm.query.from.FromClause;
import org.hibernate.sqm.query.from.FromElement;
import org.hibernate.sqm.query.from.FromElementSpace;
import org.hibernate.sqm.query.from.QualifiedJoinedFromElement;
import org.hibernate.sqm.query.from.RootEntityFromElement;
import org.hibernate.sqm.query.predicate.Predicate;

import org.antlr.v4.runtime.tree.TerminalNode;

/**
 * The main piece of Phase 1 processing of an HQL/JPQL statement responsible for processing the from clauses
 * present in the query and building some in-flight indexes (symbol tables) to be used later.
 * <p/>
 * This is needed because, just like SQL, the from clause defines the namespace for the query.  We need to
 * know that namespace before we can start processing the other clauses which work within that namespace.
 * <p/>
 * E.g., in the HQL {@code select p.name from Person p} we cannot effectively process the {@code p.name}
 * reference in the select clause until after we have processed the from clause and can then recognize that
 * {@code p} is a (forward) reference to the alias {@code p} defined in the from clause.
 *
 * @author Steve Ebersole
 */
public class FromClauseProcessor  extends HqlParserBaseListener {
	private final ParsingContext parsingContext;
	private final FromClause rootFromClause;
	private final FromClauseIndex fromClauseIndex;
	private final FromElementBuilder fromElementBuilder;

	private Statement.Type statementType;

	// Using HqlParser.QuerySpecContext references directly did not work in my experience, as each walk
	// seems to build new instances.  So here use the context text as key.
	private final Map<String, FromClause> fromClauseMap = new HashMap<String, FromClause>();
	private final Map<String, FromElement> fromElementMap = new HashMap<String, FromElement>();

	public FromClauseProcessor(ParsingContext parsingContext) {
		this.parsingContext = parsingContext;
		this.rootFromClause = new FromClause();
		this.fromClauseIndex = new FromClauseIndex();
		this.fromElementBuilder = new FromElementBuilder( parsingContext, fromClauseIndex );
	}

	public Statement.Type getStatementType() {
		return statementType;
	}

	public FromClause getRootFromClause() {
		return rootFromClause;
	}

	public FromClauseIndex getFromClauseIndex() {
		return fromClauseIndex;
	}

	public FromElementBuilder getFromElementBuilder() {
		return fromElementBuilder;
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
			throw new ParsingException( "Mismatch currentFromClause handling" );
		}
		currentFromClause = currentFromClause.getParentFromClause();
	}


	private FromElementSpace currentFromElementSpace;

	@Override
	public void enterFromElementSpace(HqlParser.FromElementSpaceContext ctx) {
		currentFromElementSpace = currentFromClause.makeFromElementSpace();
	}

	@Override
	public void exitFromElementSpace(HqlParser.FromElementSpaceContext ctx) {
		currentFromElementSpace = null;
	}

	@Override
	public void enterFromElementSpaceRoot(HqlParser.FromElementSpaceRootContext ctx) {
		final RootEntityFromElement rootEntityFromElement = fromElementBuilder.makeRootEntityFromElement(
				currentFromElementSpace,
				resolveEntityReference( ctx.mainEntityPersisterReference().dotIdentifierSequence() ),
				interpretAlias( ctx.mainEntityPersisterReference().IDENTIFIER() )
		);
		fromElementMap.put( ctx.getText(), rootEntityFromElement );
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
		final CrossJoinedFromElement join = fromElementBuilder.makeCrossJoinedFromElement(
				currentFromElementSpace,
				resolveEntityReference( ctx.mainEntityPersisterReference().dotIdentifierSequence() ),
				interpretAlias( ctx.mainEntityPersisterReference().IDENTIFIER() )
		);
		fromElementMap.put( ctx.getText(), join );
	}

	@Override
	public void enterJpaCollectionJoin(HqlParser.JpaCollectionJoinContext ctx) {
		final QualifiedJoinTreeVisitor visitor = new QualifiedJoinTreeVisitor(
				fromElementBuilder,
				fromClauseIndex,
				parsingContext,
				currentFromElementSpace,
				JoinType.INNER,
				interpretAlias( ctx.IDENTIFIER() ),
				false
		);

		QualifiedJoinedFromElement joinedPath = (QualifiedJoinedFromElement) ctx.path().accept(
				visitor
		);

		if ( joinedPath == null ) {
			throw new ParsingException( "Could not resolve JPA collection join path : " + ctx.getText() );
		}

		fromElementMap.put( ctx.getText(), joinedPath );
	}

	@Override
	public void enterQualifiedJoin(HqlParser.QualifiedJoinContext ctx) {
		final JoinType joinType;
		if ( ctx.outerKeyword() != null ) {
			// for outer joins, only left outer joins are currently supported
			joinType = JoinType.LEFT;
		}
		else {
			joinType = JoinType.INNER;
		}

		final QualifiedJoinTreeVisitor visitor = new QualifiedJoinTreeVisitor(
				fromElementBuilder,
				fromClauseIndex,
				parsingContext,
				currentFromElementSpace,
				joinType,
				interpretAlias( ctx.qualifiedJoinRhs().IDENTIFIER() ),
				ctx.fetchKeyword() != null
		);

		QualifiedJoinedFromElement joinedPath = (QualifiedJoinedFromElement) ctx.qualifiedJoinRhs().path().accept(
				visitor
		);

		if ( joinedPath == null ) {
			throw new ParsingException( "Could not resolve join path : " + ctx.qualifiedJoinRhs().getText() );
		}

		if ( ctx.qualifiedJoinPredicate() != null ) {
			visitor.setCurrentJoinRhs( joinedPath );
			joinedPath.setOnClausePredicate( (Predicate) ctx.qualifiedJoinPredicate().accept( visitor ) );
		}

		fromElementMap.put( ctx.getText(), joinedPath );
	}

	private static class QualifiedJoinTreeVisitor extends AbstractHqlParseTreeVisitor {
		private final FromElementBuilder fromElementBuilder;
		private final FromClauseIndex fromClauseIndex;
		private final ParsingContext parsingContext;
		private final FromElementSpace fromElementSpace;

		private QualifiedJoinedFromElement currentJoinRhs;

		public QualifiedJoinTreeVisitor(
				FromElementBuilder fromElementBuilder,
				FromClauseIndex fromClauseIndex,
				ParsingContext parsingContext,
				FromElementSpace fromElementSpace,
				JoinType joinType,
				String alias,
				boolean fetched) {
			super( parsingContext, fromElementBuilder, fromClauseIndex );
			this.fromElementBuilder = fromElementBuilder;
			this.fromClauseIndex = fromClauseIndex;
			this.parsingContext = parsingContext;
			this.fromElementSpace = fromElementSpace;
			this.attributePathResolverStack.push(
					new JoinAttributePathResolver(
							fromElementBuilder,
							fromClauseIndex,
							parsingContext,
							fromElementSpace,
							joinType,
							alias,
							fetched
					)
			);
		}

		@Override
		public FromClause getCurrentFromClause() {
			return fromElementSpace.getFromClause();
		}

		@Override
		public AttributePathResolver getCurrentAttributePathResolver() {
			return attributePathResolverStack.getCurrent();
		}

		public void setCurrentJoinRhs(QualifiedJoinedFromElement currentJoinRhs) {
			this.currentJoinRhs = currentJoinRhs;
		}

		@Override
		public Predicate visitQualifiedJoinPredicate(HqlParser.QualifiedJoinPredicateContext ctx) {
			if ( currentJoinRhs == null ) {
				throw new ParsingException( "Expecting join RHS to be set" );
			}

			attributePathResolverStack.push(
					new JoinPredicatePathResolverImpl(
							fromElementBuilder,
							fromClauseIndex,
							parsingContext,
							getCurrentFromClause(),
							currentJoinRhs
					)
			);
			try {
				return super.visitQualifiedJoinPredicate( ctx );
			}
			finally {
				attributePathResolverStack.pop();
			}
		}
	}

	private static class JoinAttributePathResolver extends BasicAttributePathResolverImpl {
		private final FromElementBuilder fromElementBuilder;
		private final FromElementSpace fromElementSpace;
		private final JoinType joinType;
		private final String alias;
		private final boolean fetched;

		public JoinAttributePathResolver(
				FromElementBuilder fromElementBuilder,
				FromClauseIndex fromClauseIndex,
				ParsingContext parsingContext,
				FromElementSpace fromElementSpace,
				JoinType joinType,
				String alias,
				boolean fetched) {
			super( fromElementBuilder, fromClauseIndex, parsingContext, fromElementSpace.getFromClause() );
			this.fromElementBuilder = fromElementBuilder;
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
			return fromElementBuilder.buildAttributeJoin(
					fromElementSpace,
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
