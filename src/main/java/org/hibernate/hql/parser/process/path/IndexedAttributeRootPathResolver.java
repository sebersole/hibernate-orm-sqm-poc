/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.hql.parser.process.path;

import org.hibernate.hql.parser.process.ParsingContext;
import org.hibernate.hql.parser.antlr.HqlParser;
import org.hibernate.hql.parser.semantic.expression.AttributeReferenceExpression;
import org.hibernate.hql.parser.semantic.from.FromClause;
import org.hibernate.hql.parser.semantic.from.FromElement;
import org.hibernate.hql.parser.semantic.predicate.IndexedAttributePathPart;

import org.jboss.logging.Logger;

/**
 * @author Steve Ebersole
 */
public class IndexedAttributeRootPathResolver extends AbstractAttributePathResolverImpl {
	private static final Logger log = Logger.getLogger( IndexedAttributeRootPathResolver.class );

	private final FromClause fromClause;
	private final IndexedAttributePathPart source;

	public IndexedAttributeRootPathResolver(
			FromClause fromClause,
			IndexedAttributePathPart source) {
		this.fromClause = fromClause;
		this.source = source;
	}

	@Override
	protected ParsingContext parsingContext() {
		return fromClause.getParsingContext();
	}

	@Override
	public AttributePathPart resolvePath(HqlParser.DotIdentifierSequenceContext path) {
		final String pathText = path.getText();
		log.debugf( "Starting resolution of dot-ident sequence (relative to index-path part [%s]) : %s", source, pathText );

		final String[] parts = pathText.split( "\\." );

//		final String rootPart = parts[0];
//		final AttributeDescriptor initialAttributeReference = source.getTypeDescriptor().getAttributeDescriptor( rootPart );
//		if ( initialAttributeReference == null ) {
//			throw new SemanticException(
//					String.format(
//							Locale.ENGLISH,
//							"Could not resolve path reference [%s] against source type [%s] from indexed collection reference [%s]",
//							rootPart,
//							source.getTypeDescriptor().getTypeName(),
//							source
//					)
//			);
//		}

		final FromElement lhs = resolveAnyIntermediateAttributePathJoins( source.getUnderlyingFromElement(), parts, 0 );
		return new AttributeReferenceExpression( lhs, parts[parts.length-1] );
	}
}
