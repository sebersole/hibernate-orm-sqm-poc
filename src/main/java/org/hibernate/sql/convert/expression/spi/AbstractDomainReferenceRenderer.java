/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.convert.expression.spi;

import org.hibernate.sql.ast.expression.domain.DomainReferenceExpression;

/**
 * @author Steve Ebersole
 */
public abstract class AbstractDomainReferenceRenderer implements DomainReferenceRenderer {
	private final RenderingContext renderingContext;

	public AbstractDomainReferenceRenderer(RenderingContext renderingContext) {
		this.renderingContext = renderingContext;
	}

	@Override
	public void render(DomainReferenceExpression expression, boolean shallow) {
		// todo : invert this... pass the RenderingContext along to a DomainReferenceExpression#renderColumnBindings method
		//		^^ allows grouping of composite values for wrapping in parentheses in the SQL

		renderingContext.renderColumnBindings( expression.resolveColumnBindings( shallow ) );
	}
}
