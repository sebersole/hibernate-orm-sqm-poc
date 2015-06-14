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
import java.util.Locale;

import org.hibernate.hql.JoinType;
import org.hibernate.hql.SemanticException;
import org.hibernate.hql.antlr.HqlParser;
import org.hibernate.hql.model.EntityTypeDescriptor;
import org.hibernate.hql.model.ModelMetadata;
import org.hibernate.hql.model.TypeDescriptor;

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
		final EntityTypeDescriptor entityTypeDescriptor = getModelMetadata().resolveEntityReference( entityName );
		if ( entityTypeDescriptor == null ) {
			throw new SemanticException( "Unresolved entity name : " + entityName );
		}

		String alias = null;
		if ( ctx.mainEntityPersisterReference().IDENTIFIER() != null ) {
			alias = ctx.mainEntityPersisterReference().IDENTIFIER().getText();
		}
		if ( alias == null ) {
			alias = fromClause.getParsingContext().getImplicitAliasGenerator().buildUniqueImplicitAlias();
		}

		root = new FromElementRootEntity( this, alias, entityTypeDescriptor );
		registerAlias( root );
		return root;
	}

	private ModelMetadata getModelMetadata() {
		return fromClause.getParsingContext().getModelMetadata();
	}

	private void registerAlias(FromElement fromElement) {
		fromClause.registerAlias( fromElement );
	}

	public FromElementJoined makeFromElement(HqlParser.CrossJoinContext ctx) {
		final String entityName = ctx.mainEntityPersisterReference().dotIdentifierPath().getText();
		final EntityTypeDescriptor entityTypeDescriptor = getModelMetadata().resolveEntityReference( entityName );
		if ( entityTypeDescriptor == null ) {
			throw new SemanticException( "Unresolved entity name : " + entityName );
		}

		String alias = null;
		if ( ctx.mainEntityPersisterReference().IDENTIFIER() != null ) {
			alias = ctx.mainEntityPersisterReference().IDENTIFIER().getText();
		}
		if ( alias == null ) {
			alias = fromClause.getParsingContext().getImplicitAliasGenerator().buildUniqueImplicitAlias();
		}

		return addJoin( new FromElementCrossJoinedImpl( this, alias, entityTypeDescriptor ) );
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
		// todo : handling of on/with clause

		// interpret join target as either an attribute reference or as an entityName.
		// some impl notes...
		// 		1) generally speaking the precedence for resolution is:
		//			1) qualified-attribute-path
		//			2) entity-name
		//			3) unqualified-attribute-path
		//		2) if the target has no dots, the target cannot be a qualified-attribute-path

		final String entityNameOrAttributePath = info.getJoinTarget();
		final String[] parts = entityNameOrAttributePath.split( "\\." );
		final String firstIdentifier = parts[0];

		// 1st level precedence : qualified-attribute-path
		if ( entityNameOrAttributePath.contains( "." ) ) {
			final FromElement lhs = fromClause.findFromElementByAlias( firstIdentifier );
			if ( lhs != null ) {
				// we have qualified-attribute-path join
				return addJoin(
						buildAttributePathJoin(
								lhs,
								parts,
								1,
								info
						)
				);
			}
		}

		// 2nd level precedence : entity-name
		EntityTypeDescriptor entityType = getModelMetadata().resolveEntityReference( entityNameOrAttributePath );
		if ( entityType != null ) {
			String alias = info.getAlias();
			if ( alias == null ) {
				alias = fromClause.getParsingContext().getImplicitAliasGenerator().buildUniqueImplicitAlias();
			}
			if ( info.isFetched() ) {
				throw new SemanticException( "Entity join cannot be fetched : " + entityNameOrAttributePath );
			}
			return addJoin(
					new FromElementQualifiedEntityJoinImpl(
							this,
							alias,
							entityType,
							info.getJoinType()
					)
			);
		}

		// 3rd level precedence : unqualified-attribute-path
		final FromElement lhs = fromClause.findFromElementWithAttribute( firstIdentifier );
		if ( lhs != null ) {
			return addJoin(
					buildAttributePathJoin(
							lhs,
							parts,
							0,
							info
					)
			);
		}

		// if we get here we had a problem interpreting the dot-ident path
		throw UnresolvedJoinTargetException.forJoinTarget( entityNameOrAttributePath );
	}

	private FromElementJoined buildAttributePathJoin(
			FromElement aliasedFromElement,
			String[] parts,
			int i,
			QualifiedJoinInfo info) {
		FromElement lhs = aliasedFromElement;

		// build joins for any intermediate path parts
		while ( i < parts.length-1 ) {
			final String partName = parts[i];
			final TypeDescriptor attributeType = lhs.getTypeDescriptor().getAttributeType( partName );
			lhs = new FromElementQualifiedAttributeJoinImpl(
					this,
					fromClause.getParsingContext().getImplicitAliasGenerator().buildUniqueImplicitAlias(),
					attributeType,
					info.getJoinType(),
					lhs.getAlias() + '.'  + partName,
					info.isFetched()
			);
			i++;
		}

		// and then build the "terminal" join
		String alias = info.getAlias();
		if ( alias == null ) {
			alias = fromClause.getParsingContext().getImplicitAliasGenerator().buildUniqueImplicitAlias();
		}
		final String terminalPartName = parts[i];
		final TypeDescriptor attributeType = lhs.getTypeDescriptor().getAttributeType( terminalPartName );
		return new FromElementQualifiedAttributeJoinImpl(
				this,
				alias,
				attributeType,
				info.getJoinType(),
				lhs.getAlias() + '.'  + terminalPartName,
				info.isFetched()
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
		if ( ctx.fullKeyword() != null || ctx.rightKeyword() != null ) {
			throw new UnsupportedJoinTypeException(
					String.format(
							Locale.ENGLISH,
							"Unsupported outer join type requested [%s], only LEFT outer joins are supported",
							ctx.fullKeyword() != null ? "FULL" : "RIGHT"
					)
			);
		}
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
