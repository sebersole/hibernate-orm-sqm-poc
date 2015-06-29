/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.hql.parser.antlr.path;

import org.hibernate.hql.parser.JoinType;
import org.hibernate.hql.parser.ParsingContext;
import org.hibernate.hql.parser.SemanticException;
import org.hibernate.hql.parser.model.AttributeDescriptor;
import org.hibernate.hql.parser.semantic.from.FromElement;
import org.hibernate.hql.parser.semantic.from.JoinedFromElement;

/**
 * @author Steve Ebersole
 */
public abstract class AbstractAttributePathResolverImpl implements AttributePathResolver {
	protected abstract ParsingContext parsingContext();

	protected FromElement resolveAnyIntermediateAttributePathJoins(
			FromElement lhs,
			String[] pathParts,
			int start) {
		int i = start;

		// build joins for any intermediate path parts
		while ( i < pathParts.length-1 ) {
			lhs = buildIntermediateAttributeJoin( lhs, pathParts[i] );
			i++;
		}

		return lhs;
	}

	protected FromElement buildIntermediateAttributeJoin(FromElement lhs, String pathPart) {
		final AttributeDescriptor joinedAttributeDescriptor = resolveAttributeDescriptor( lhs, pathPart );
		validateIntermediateAttributeJoin( lhs, joinedAttributeDescriptor );
		return lhs.getContainingSpace().buildAttributeJoin(
				lhs,
				joinedAttributeDescriptor,
				null,
				getIntermediateJoinType(),
				areIntermediateJoinsFetched()
		);
	}

	protected void validateIntermediateAttributeJoin(FromElement lhs, AttributeDescriptor joinedAttributeDescriptor) {
	}

	protected JoinType getIntermediateJoinType() {
		return JoinType.LEFT;
	}

	protected boolean areIntermediateJoinsFetched() {
		return false;
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
}
