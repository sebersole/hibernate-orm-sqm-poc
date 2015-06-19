/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.hql.parser.semantic;

import org.hibernate.hql.parser.semantic.from.FromClause;

/**
 * @author Steve Ebersole
 */
public interface Statement {
	enum Type {
		SELECT,
		INSERT,
		UPDATE,
		DELETE
	}

	Type getType();
}
