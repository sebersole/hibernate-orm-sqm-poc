/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.sql.orm.internal;

import org.hibernate.sql.orm.QueryParameter;
import org.hibernate.type.Type;

/**
 * @author Steve Ebersole
 */
public class PositionalQueryParameter extends AbstractQueryParameter implements QueryParameter {
	private final int position;

	public PositionalQueryParameter(int position, Type expectedType) {
		super( expectedType );
		this.position = position;
	}

	public int getPosition() {
		return position;
	}
}
