/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.ast.select;

import org.hibernate.sql.ast.expression.Expression;
import org.hibernate.sql.convert.results.spi.Return;

/**
 * @author Steve Ebersole
 */
public class Selection {
	private final Return queryReturn;

	public Selection(Return queryReturn) {
		this.queryReturn = queryReturn;
	}

	public Return getQueryReturn() {
		return queryReturn;
	}

	public Expression getSelectExpression() {
		return getQueryReturn().getSelectExpression();
	}

	public String getResultVariable() {
		return getQueryReturn().getResultVariableName();
	}
}
