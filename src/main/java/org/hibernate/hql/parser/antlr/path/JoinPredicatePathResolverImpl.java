/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.hql.parser.antlr.path;

import org.hibernate.hql.parser.SemanticException;
import org.hibernate.hql.parser.model.AttributeDescriptor;
import org.hibernate.hql.parser.model.CollectionTypeDescriptor;
import org.hibernate.hql.parser.model.EntityTypeDescriptor;
import org.hibernate.hql.parser.semantic.from.FromClause;
import org.hibernate.hql.parser.semantic.from.FromElement;

/**
 * @author Steve Ebersole
 */
public class JoinPredicatePathResolverImpl extends BasicAttributePathResolverImpl {
	public JoinPredicatePathResolverImpl(FromClause fromClause) {
		super( fromClause );
	}

	@Override
	protected void validateIntermediateAttributeJoin(
			FromElement lhs,
			AttributeDescriptor joinedAttributeDescriptor) {
		if ( joinedAttributeDescriptor.getType() instanceof EntityTypeDescriptor ) {
			throw new SemanticException(
					"On-clause predicate of a qualified join cannot contain implicit entity joins : " +
							joinedAttributeDescriptor.getName()
			);
		}
		else if ( joinedAttributeDescriptor.getType() instanceof CollectionTypeDescriptor ) {
			throw new SemanticException(
					"On-clause predicate of a qualified join cannot contain implicit collection joins : " +
							joinedAttributeDescriptor.getName()
			);
		}

		// todo : we'd really want to validate that all predicate expressions refer to just the lhs and rhs from-elements

		super.validateIntermediateAttributeJoin( lhs, joinedAttributeDescriptor );
	}
}
