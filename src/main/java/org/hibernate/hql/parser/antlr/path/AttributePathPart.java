/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.hql.parser.antlr.path;

import org.hibernate.hql.parser.model.TypeDescriptor;
import org.hibernate.hql.parser.semantic.expression.AttributeReferenceExpression;

/**
 * @author Steve Ebersole
 */
public interface AttributePathPart {
	AttributePathPart getSource();

	String getLocalName();
	TypeDescriptor getTypeDescriptor();
}
