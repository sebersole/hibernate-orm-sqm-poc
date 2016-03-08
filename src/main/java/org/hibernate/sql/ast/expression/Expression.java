/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.sql.ast.expression;

import org.hibernate.sql.gen.SqlTreeWalker;
import org.hibernate.type.Type;

/**
 * @author Steve Ebersole
 */
public interface Expression {
	Type getType();

	void accept(SqlTreeWalker sqlTreeWalker);
}
