/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.hql.parser.antlr.path;

import org.hibernate.hql.parser.ParsingContext;
import org.hibernate.hql.parser.antlr.HqlParser;
import org.hibernate.hql.parser.model.AttributeDescriptor;
import org.hibernate.hql.parser.semantic.expression.AttributeReferenceExpression;
import org.hibernate.hql.parser.semantic.expression.FromElementReferenceExpression;
import org.hibernate.hql.parser.semantic.from.FromClause;
import org.hibernate.hql.parser.semantic.from.FromElement;

import org.jboss.logging.Logger;

/**
 * @author Steve Ebersole
 */
public class BasicAttributePathResolverImpl extends AbstractAttributePathResolverImpl {
	private static final Logger log = Logger.getLogger( BasicAttributePathResolverImpl.class );

	private final FromClause fromClause;

	public BasicAttributePathResolverImpl(FromClause fromClause) {
		this.fromClause = fromClause;
	}

	@Override
	protected ParsingContext parsingContext() {
		return fromClause.getParsingContext();
	}

	@Override
	public AttributePathPart resolvePath(HqlParser.DotIdentifierSequenceContext path) {
		final String pathText = path.getText();
		log.debugf( "Starting resolution of dot-ident sequence : %s", pathText );

		final String[] parts = pathText.split( "\\." );

		final String rootPart = parts[0];

		// 1st level precedence : qualified-attribute-path
		if ( pathText.contains( "." ) ) {
			final FromElement aliasedFromElement = findFromElementByAlias( rootPart );
			if ( aliasedFromElement != null ) {
				final FromElement lhs = resolveAnyIntermediateAttributePathJoins( aliasedFromElement, parts, 1 );
				return resolveTerminalPathPart( lhs, parts[parts.length-1] );
			}
		}

		// 2nd level precedence : from-element alias
		if ( !pathText.contains( "." ) ) {
			final FromElement aliasedFromElement = findFromElementByAlias( rootPart );
			if ( aliasedFromElement != null ) {
				return resolveFromElementAliasAsTerminal( aliasedFromElement );
			}
		}

		// 3rd level precedence : unqualified-attribute-path
		final FromElement root = findFromElementWithAttribute( rootPart );
		if ( root != null ) {
			final FromElement lhs = resolveAnyIntermediateAttributePathJoins( root, parts, 0 );
			return resolveTerminalPathPart( lhs, parts[parts.length-1] );
		}

		return null;
	}

	protected FromElement findFromElementByAlias(String alias) {
		return fromClause.findFromElementByAlias( alias );
	}

	protected FromElement findFromElementWithAttribute(String attributeName) {
		return fromClause.findFromElementWithAttribute( attributeName );
	}

	protected AttributePathPart resolveTerminalPathPart(FromElement lhs, String terminalName) {
		final AttributeDescriptor attributeDescriptor = lhs.getTypeDescriptor().getAttributeDescriptor( terminalName );
		final AttributeReferenceExpression expr = new AttributeReferenceExpression( lhs, attributeDescriptor );
		log.debugf( "Resolved terminal path-part [%s] : %s", terminalName, expr );
		return expr;
	}

	protected AttributePathPart resolveFromElementAliasAsTerminal(FromElement aliasedFromElement) {
		log.debugf( "Resolved from-element alias as terminal : %s", aliasedFromElement.getAlias() );
		return new FromElementReferenceExpression( aliasedFromElement );
	}
}
