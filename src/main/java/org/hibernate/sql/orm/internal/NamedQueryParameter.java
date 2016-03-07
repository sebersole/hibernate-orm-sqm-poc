/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.sql.orm.internal;

import org.hibernate.sql.orm.QueryParameter;

/**
 * @author Steve Ebersole
 */
public class NamedQueryParameter implements QueryParameter {
	private final String name;

	public NamedQueryParameter(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

}
