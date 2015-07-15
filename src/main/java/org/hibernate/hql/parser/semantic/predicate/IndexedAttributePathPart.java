/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.hql.parser.semantic.predicate;

import org.hibernate.hql.parser.SemanticException;
import org.hibernate.hql.parser.process.path.AttributePathPart;
import org.hibernate.hql.parser.model.CollectionTypeDescriptor;
import org.hibernate.hql.parser.model.TypeDescriptor;
import org.hibernate.hql.parser.semantic.expression.Expression;
import org.hibernate.hql.parser.semantic.from.FromElement;

/**
 * @author Steve Ebersole
 */
public class IndexedAttributePathPart implements AttributePathPart {
	private final AttributePathPart source;
	private final Expression index;

	private final TypeDescriptor typeDescriptor;

	public IndexedAttributePathPart(AttributePathPart source, Expression index) {
		this.source = source;
		this.index = index;

		// the source TypeDescriptor needs to be an indexed collection for this to be valid...
		if ( !CollectionTypeDescriptor.class.isInstance( source.getTypeDescriptor() ) ) {
			throw new SemanticException( "Index operator only valid for indexed collections (maps, lists, arrays) : " + source );
		}

		final CollectionTypeDescriptor collectionTypeDescriptor = (CollectionTypeDescriptor) source.getTypeDescriptor();
		if ( collectionTypeDescriptor.getIndexTypeDescriptor() == null ) {
			throw new SemanticException( "Index operator only valid for indexed collections (maps, lists, arrays) : " + source );
		}

		// todo : would be nice to validate the index's type against the Collection-index's type
		// 		but that requires "compatible type checking" rather than TypeDescriptor sameness (long versus int, e.g)

		// Ultimately the TypeDescriptor for this part is the same as the elements of the collection...
		this.typeDescriptor = collectionTypeDescriptor.getElementTypeDescriptor();
	}

	public AttributePathPart getSource() {
		return source;
	}

	public Expression getIndex() {
		return index;
	}

	@Override
	public TypeDescriptor getTypeDescriptor() {
		return typeDescriptor;
	}

	@Override
	public FromElement getUnderlyingFromElement() {
		// todo : almost positive this is not accurate in most cases
		return source.getUnderlyingFromElement();
	}
}
