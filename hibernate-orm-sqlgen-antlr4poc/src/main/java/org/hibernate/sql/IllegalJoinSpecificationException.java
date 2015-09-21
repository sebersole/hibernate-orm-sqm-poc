/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.sql;

import org.hibernate.sql.ast.from.TableSpecificationJoin;

/**
 * Indicates a problem in the definition of a {@link TableSpecificationJoin}
 *
 * @author Steve Ebersole
 */
public class IllegalJoinSpecificationException extends SqlTreeException {
	public IllegalJoinSpecificationException(String message) {
		super( message );
	}
}
