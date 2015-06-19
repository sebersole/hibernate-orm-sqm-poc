/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.hql.parser.semantic.select;

import org.hibernate.hql.parser.semantic.expression.Expression;

/**
 * @author Steve Ebersole
 */
public class AliasedDynamicInstantiationArgument {
	private final Expression argument;
	private final String alias;

	public AliasedDynamicInstantiationArgument(Expression argument, String alias) {
		this.argument = argument;
		this.alias = alias;
	}

	public Expression getArgument() {
		return argument;
	}

	public String getAlias() {
		return alias;
	}
}
