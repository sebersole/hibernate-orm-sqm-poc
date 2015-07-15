/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.hql.parser.semantic.from;

import org.hibernate.hql.parser.process.path.AttributePathPart;
import org.hibernate.hql.parser.model.TypeDescriptor;

/**
 * @author Steve Ebersole
 */
public interface FromElement extends AttributePathPart {
	FromElementSpace getContainingSpace();
	String getAlias();
	TypeDescriptor getTypeDescriptor();

	void addTreatedAs(TypeDescriptor typeDescriptor);
}
