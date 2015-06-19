/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.hql.parser.semantic.select;

import org.hibernate.hql.parser.ParsingContext;
import org.hibernate.hql.parser.semantic.expression.Expression;

/**
 * @author Steve Ebersole
 */
public class SelectListItem {
	private final ParsingContext parsingContext;
	private final Expression selectExpression;
	private final String alias;

	public SelectListItem(ParsingContext parsingContext, Expression selectExpression, String alias) {
		this.parsingContext = parsingContext;
		this.selectExpression = selectExpression;
		this.alias = alias;
	}

	public SelectListItem(ParsingContext parsingContext, Expression selectExpression) {
		this( parsingContext, selectExpression, null );
	}

	public Expression getSelectExpression() {
		return selectExpression;
	}

	public String getAlias() {
		return alias;
	}
}
