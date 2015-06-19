/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.hql.parser.semantic;

import org.hibernate.hql.parser.ParsingContext;
import org.hibernate.hql.parser.ParsingException;
import org.hibernate.hql.parser.semantic.order.OrderByClause;

/**
 * @author Steve Ebersole
 */
public class SelectStatement implements Statement {
	private final ParsingContext parsingContext;

	private QuerySpec querySpec;
	private OrderByClause orderByClause;

	public SelectStatement(ParsingContext parsingContext) {
		this.parsingContext = parsingContext;
		this.orderByClause = new OrderByClause( parsingContext );
	}

	public QuerySpec getQuerySpec() {
		return querySpec;
	}

	public OrderByClause getOrderByClause() {
		return orderByClause;
	}

	public void applyQuerySpec(QuerySpec querySpec) {
		if ( this.querySpec != null ) {
			throw new ParsingException( "QuerySpec was already defined for select-statement" );
		}
		this.querySpec = querySpec;
	}

	public void applyOrderByClause(OrderByClause orderByClause) {
		if ( this.orderByClause != null ) {
			throw new ParsingException( "OrderByClause was already defined for select-statement" );
		}
		this.orderByClause = orderByClause;
	}

	@Override
	public Type getType() {
		return Type.SELECT;
	}
}
