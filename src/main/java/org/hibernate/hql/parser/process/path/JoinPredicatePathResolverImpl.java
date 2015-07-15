/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.hql.parser.process.path;

import org.hibernate.hql.parser.SemanticException;
import org.hibernate.hql.parser.model.AttributeDescriptor;
import org.hibernate.hql.parser.model.CollectionTypeDescriptor;
import org.hibernate.hql.parser.model.EntityTypeDescriptor;
import org.hibernate.hql.parser.semantic.from.FromClause;
import org.hibernate.hql.parser.semantic.from.FromElement;
import org.hibernate.hql.parser.semantic.from.QualifiedJoinedFromElement;

/**
 * @author Steve Ebersole
 */
public class JoinPredicatePathResolverImpl extends BasicAttributePathResolverImpl {
	private final QualifiedJoinedFromElement joinRhs;
	private FromElement joinLhs;

	public JoinPredicatePathResolverImpl(FromClause fromClause, QualifiedJoinedFromElement joinRhs) {
		super( fromClause );
		this.joinRhs = joinRhs;
	}

	@Override
	@SuppressWarnings("StatementWithEmptyBody")
	protected void validatePathRoot(FromElement root) {
		if ( root == joinRhs ) {
			// nothing to do
		}
		else if ( joinLhs == null ) {
			// assume root is LHS
			joinLhs = root;
		}
		else {
			if ( joinLhs != root ) {
				throw new SemanticException( "Qualified join predicate referred to more than 2 FromElements" );
			}
		}
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

		super.validateIntermediateAttributeJoin( lhs, joinedAttributeDescriptor );
	}
}
