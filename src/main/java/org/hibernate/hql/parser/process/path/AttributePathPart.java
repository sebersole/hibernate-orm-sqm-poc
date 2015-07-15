/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.hql.parser.process.path;

import org.hibernate.hql.parser.semantic.expression.Expression;
import org.hibernate.hql.parser.semantic.from.FromElement;

/**
 * @author Steve Ebersole
 */
public interface AttributePathPart extends Expression {
//	/**
//	 * Return the path which led to this source.
//	 *
//	 * @return The origination path.
//	 */
//	String getOriginationPathText();

	FromElement getUnderlyingFromElement();
}
