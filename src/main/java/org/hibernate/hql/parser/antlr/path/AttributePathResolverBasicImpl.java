/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.hql.parser.antlr.path;

import org.hibernate.hql.parser.JoinType;
import org.hibernate.hql.parser.NotYetImplementedException;
import org.hibernate.hql.parser.ParsingContext;
import org.hibernate.hql.parser.SemanticException;
import org.hibernate.hql.parser.antlr.HqlParser;
import org.hibernate.hql.parser.model.AttributeDescriptor;
import org.hibernate.hql.parser.model.EntityTypeDescriptor;
import org.hibernate.hql.parser.model.TypeDescriptor;
import org.hibernate.hql.parser.semantic.expression.AttributeReferenceExpression;
import org.hibernate.hql.parser.semantic.expression.EntityTypeExpression;
import org.hibernate.hql.parser.semantic.expression.FromElementReferenceExpression;
import org.hibernate.hql.parser.semantic.from.FromClause;
import org.hibernate.hql.parser.semantic.from.FromElement;
import org.hibernate.hql.parser.semantic.from.JoinedFromElement;
import org.hibernate.hql.parser.semantic.from.TreatedFromElement;
import org.hibernate.hql.parser.semantic.from.TreatedJoinedFromElement;

import org.jboss.logging.Logger;

/**
 * @author Steve Ebersole
 */
public class AttributePathResolverBasicImpl extends AbstractAttributePathResolverImpl {
	private static final Logger log = Logger.getLogger( AttributePathResolverBasicImpl.class );

	private final FromClause fromClause;

	public AttributePathResolverBasicImpl(FromClause fromClause) {
		this.fromClause = fromClause;
	}

	@Override
	protected ParsingContext parsingContext() {
		return fromClause.getParsingContext();
	}

	@Override
	protected Object resolveSimplePathContext(HqlParser.SimplePathContext pathContext) {
		final String pathText = pathContext.dotIdentifierSequence().getText();
		final String[] parts = pathText.split( "\\." );

		final String rootPart = parts[0];

		// 1st level precedence : qualified-attribute-path
		if ( pathText.contains( "." ) ) {
			final FromElement aliasedFromElement = fromClause.findFromElementByAlias( rootPart );
			if ( aliasedFromElement != null ) {
				final FromElement lhs = resolveAnyIntermediateAttributePathJoins(
						aliasedFromElement,
						parts,
						1,
						JoinType.LEFT,
						false
				);
				return new AttributeReferenceExpression( lhs, parts[parts.length-1] );
			}
		}

		// 2nd level precedence : from-element alias
		if ( !pathText.contains( "." ) ) {
			final FromElement aliasedFromElement = fromClause.findFromElementByAlias( rootPart );
			if ( aliasedFromElement != null ) {
				return new FromElementReferenceExpression( aliasedFromElement );
			}
		}

		// 3rd level precedence : unqualified-attribute-path
		final FromElement root = fromClause.findFromElementWithAttribute( rootPart );
		if ( root != null ) {
			final FromElement lhs = resolveAnyIntermediateAttributePathJoins(
					root,
					parts,
					0,
					JoinType.LEFT,
					false
			);
			return new AttributeReferenceExpression( lhs, parts[parts.length-1] );
		}

		// 4th level precedence : entity-name
		EntityTypeDescriptor entityType = parsingContext().getConsumerContext().resolveEntityReference( pathText );
		if ( entityType != null ) {
			return new EntityTypeExpression( entityType );
		}

		// 5th level precedence : constant reference
		try {
			return resolveConstantExpression( pathText );
		}
		catch (SemanticException e) {
			log.debug( e.getMessage() );
		}

		// if we get here we had a problem interpreting the dot-ident sequence
		throw new SemanticException( "Could not interpret token : " + pathText );
	}

	@Override
	protected Object resolveIndexedPathContext(HqlParser.IndexedPathContext pathContext) {
		throw new NotYetImplementedException();
	}

	@Override
	protected Object resolveTreatedPathContext(HqlParser.TreatedPathContext pathContext) {
		final FromElement fromElement = resolveTreatedBase( pathContext.dotIdentifierSequence().get( 0 ).getText() );
		final String treatAsName = pathContext.dotIdentifierSequence().get( 1 ).getText();

		final TypeDescriptor treatAsTypeDescriptor = parsingContext().getConsumerContext().resolveEntityReference( treatAsName );
		if ( treatAsTypeDescriptor == null ) {
			throw new SemanticException( "TREAT-AS target type [" + treatAsName + "] did not reference an entity" );
		}

		fromElement.addTreatedAs( treatAsTypeDescriptor );

		if ( fromElement instanceof JoinedFromElement ) {
			return new TreatedJoinedFromElement( (JoinedFromElement) fromElement, treatAsTypeDescriptor );
		}
		else {
			return new TreatedFromElement( fromElement, treatAsTypeDescriptor );
		}
	}

	private FromElement resolveTreatedBase(String pathText) {
		final String[] parts = pathText.split( "\\." );

		final String rootPart = parts[0];

		// 1st level precedence : qualified-attribute-path
		if ( pathText.contains( "." ) ) {
			final FromElement aliasedFromElement = fromClause.findFromElementByAlias( rootPart );
			if ( aliasedFromElement != null ) {
				final FromElement lhs = resolveAnyIntermediateAttributePathJoins(
						aliasedFromElement,
						parts,
						1,
						JoinType.LEFT,
						false
				);

				final String terminalName = parts[parts.length-1];
				final AttributeDescriptor attributeDescriptor = lhs.getTypeDescriptor().getAttributeDescriptor( terminalName );
				final TypeDescriptor terminalTypeDescriptor = attributeDescriptor.getType();
				if ( terminalTypeDescriptor == null ) {
					throw new SemanticException( "Could not resolve path [" + pathText + "] for TREAT-AS" );
				}
				if ( !EntityTypeDescriptor.class.isInstance( terminalTypeDescriptor ) ) {
					throw new SemanticException( "Path [" + pathText + "] for TREAT-AS did not resolve to entity" );
				}
				// todo : this does not always have to resolve to a Join, but modeling this requires a 'Path' contract
				return buildAttributeJoin(
						lhs,
						terminalName,
						JoinType.LEFT,
						null,
						false
				);
			}
		}

		// 2nd level precedence : from-element alias
		if ( !pathText.contains( "." ) ) {
			final FromElement aliasedFromElement = fromClause.findFromElementByAlias( rootPart );
			if ( aliasedFromElement != null ) {
				return aliasedFromElement;
			}
		}

		// 3rd level precedence : unqualified-attribute-path
		final FromElement root = fromClause.findFromElementWithAttribute( rootPart );
		if ( root != null ) {
			final FromElement lhs = resolveAnyIntermediateAttributePathJoins(
					root,
					parts,
					0,
					JoinType.LEFT,
					false
			);

			final String terminalName = parts[parts.length-1];
			final AttributeDescriptor attributeDescriptor = lhs.getTypeDescriptor().getAttributeDescriptor( terminalName );
			final TypeDescriptor terminalTypeDescriptor = attributeDescriptor.getType();
			if ( terminalTypeDescriptor == null ) {
				throw new SemanticException( "Could not resolve path [" + pathText + "] for TREAT-AS" );
			}
			if ( !EntityTypeDescriptor.class.isInstance( terminalTypeDescriptor ) ) {
				throw new SemanticException( "Path [" + pathText + "] for TREAT-AS did not resolve to entity" );
			}
			// todo : this does not always have to resolve to a Join, but modeling this requires a 'Path' contract
			return buildAttributeJoin(
					lhs,
					terminalName,
					JoinType.LEFT,
					null,
					false
			);
		}

		throw new SemanticException( "Could not interpret TREAT-AS path token : " + pathText );
	}
}
