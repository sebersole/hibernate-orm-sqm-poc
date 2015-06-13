/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.hql.antlr.normalization;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.hibernate.hql.JoinType;
import org.hibernate.hql.antlr.HqlParser;

/**
 * @author Steve Ebersole
 */
public class FromElementSpace {
	private final FromClause fromClause;

	private FromElementRootEntity root;
	private List<FromElementJoined> joins;


	public FromElementSpace(FromClause fromClause) {
		this.fromClause = fromClause;
	}

	public FromElementRootEntity getRoot() {
		return root;
	}

	public List<FromElementJoined> getJoins() {
		return joins == null ? Collections.<FromElementJoined>emptyList() : joins;
	}

	public FromElementRootEntity makeFromElement(HqlParser.RootEntityReferenceContext ctx) {
		final String entityName = ctx.mainEntityPersisterReference().dotIdentifierPath().getText();

		String alias = null;
		if ( ctx.mainEntityPersisterReference().IDENTIFIER() != null ) {
			alias = ctx.mainEntityPersisterReference().IDENTIFIER().getText();
		}
		if ( alias == null ) {
			alias = fromClause.getNormalizationContext().getImplicitAliasGenerator().buildUniqueImplicitAlias();
		}

		root = new FromElementRootEntity( this, alias, entityName );
		registerAlias( root );
		return root;
	}

	private void registerAlias(FromElement fromElement) {
		fromClause.registerAlias( fromElement );
	}

	public FromElementJoined makeFromElement(HqlParser.CrossJoinContext ctx) {
		final String entityName = ctx.mainEntityPersisterReference().dotIdentifierPath().getText();

		String alias = null;
		if ( ctx.mainEntityPersisterReference().IDENTIFIER() != null ) {
			alias = ctx.mainEntityPersisterReference().IDENTIFIER().getText();
		}
		if ( alias == null ) {
			alias = fromClause.getNormalizationContext().getImplicitAliasGenerator().buildUniqueImplicitAlias();
		}

		return addJoin( new FromElementCrossJoinedImpl( this, alias, entityName ) );
	}

	private FromElementJoined addJoin(FromElementJoined fromElement) {
		if ( joins == null ) {
			joins = new ArrayList<FromElementJoined>();
		}
		joins.add( fromElement );
		registerAlias( fromElement );
		return fromElement;
	}

	public FromElementJoined makeFromElement(final HqlParser.ImplicitInnerJoinContext ctx) {
		return makeQualifiedJoin(
				new QualifiedJoinInfo() {
					@Override
					public JoinType getJoinType() {
						return JoinType.INNER;
					}

					@Override
					public String getJoinTarget() {
						return ctx.qualifiedJoinRhs().dotIdentifierPath().getText();
					}

					@Override
					public String getAlias() {
						return ctx.qualifiedJoinRhs().IDENTIFIER() == null
								? null
								: ctx.qualifiedJoinRhs().IDENTIFIER().getText();
					}

					@Override
					public boolean isFetched() {
						return ctx.fetchKeyword() != null;
					}

					@Override
					public HqlParser.LogicalExpressionContext getRestrictions() {
						return ctx.qualifiedJoinRhs().logicalExpression();
					}
				}
		);
	}

	private static interface QualifiedJoinInfo {
		JoinType getJoinType();
		String getJoinTarget();
		String getAlias();
		boolean isFetched();
		HqlParser.LogicalExpressionContext getRestrictions();
	}

	private FromElementJoined makeQualifiedJoin(QualifiedJoinInfo info) {
		final String entityNameOrAttributePath = info.getJoinTarget();

		String alias = info.getAlias();
		if ( alias == null ) {
			alias = fromClause.getNormalizationContext().getImplicitAliasGenerator().buildUniqueImplicitAlias();
		}

		// todo : interpretation of entityNameOrAttributePath
		// todo : interpretation of on/with clause

		// for now, assume an "attribute join" with no restrictions
		return addJoin(
				new FromElementQualifiedAttributeJoinImpl(
						this,
						alias,
						info.getJoinType(),
						entityNameOrAttributePath,
						info.isFetched()
				)
		);
	}

	public FromElementJoined makeFromElement(final HqlParser.ExplicitInnerJoinContext ctx) {
		return makeQualifiedJoin(
				new QualifiedJoinInfo() {
					@Override
					public JoinType getJoinType() {
						return JoinType.INNER;
					}

					@Override
					public String getJoinTarget() {
						return ctx.qualifiedJoinRhs().dotIdentifierPath().getText();
					}

					@Override
					public String getAlias() {
						return ctx.qualifiedJoinRhs().IDENTIFIER() == null
								? null
								: ctx.qualifiedJoinRhs().IDENTIFIER().getText();
					}

					@Override
					public boolean isFetched() {
						return ctx.fetchKeyword() != null;
					}

					@Override
					public HqlParser.LogicalExpressionContext getRestrictions() {
						return ctx.qualifiedJoinRhs().logicalExpression();
					}
				}
		);
	}

	public FromElementJoined makeFromElement(final HqlParser.ExplicitOuterJoinContext ctx) {
		return makeQualifiedJoin(
				new QualifiedJoinInfo() {
					@Override
					public JoinType getJoinType() {
						// currently only left outer joins are supported
						return JoinType.LEFT;
					}

					@Override
					public String getJoinTarget() {
						return ctx.qualifiedJoinRhs().dotIdentifierPath().getText();
					}

					@Override
					public String getAlias() {
						return ctx.qualifiedJoinRhs().IDENTIFIER() == null
								? null
								: ctx.qualifiedJoinRhs().IDENTIFIER().getText();
					}

					@Override
					public boolean isFetched() {
						return ctx.fetchKeyword() != null;
					}

					@Override
					public HqlParser.LogicalExpressionContext getRestrictions() {
						return ctx.qualifiedJoinRhs().logicalExpression();
					}
				}
		);
	}

	public void complete() {

	}
}
