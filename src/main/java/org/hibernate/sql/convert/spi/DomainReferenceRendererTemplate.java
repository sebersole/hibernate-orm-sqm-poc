/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.convert.spi;

import org.hibernate.sql.ast.expression.AttributeReference;
import org.hibernate.sql.ast.expression.DomainReference;
import org.hibernate.sql.ast.expression.EntityReference;
import org.hibernate.sql.ast.from.ColumnBinding;

/**
 * @author Steve Ebersole
 */
public abstract class DomainReferenceRendererTemplate implements DomainReferenceRenderer {
	private final RenderingContext renderingContext;

	public DomainReferenceRendererTemplate(RenderingContext renderingContext) {
		this.renderingContext = renderingContext;
	}

	protected void renderColumnBindings(ColumnBinding... columnBindings) {
		renderingContext.renderColumnBindings( columnBindings );
	}

	@Override
	public void render(DomainReference domainReference) {
		if ( domainReference instanceof EntityReference ) {
			renderEntityReference( (EntityReference) domainReference );
		}
		else if ( domainReference instanceof AttributeReference ) {
			renderAttributeReference( (AttributeReference) domainReference );
		}
	}

	protected abstract void renderEntityReference(EntityReference entityReference);

	protected abstract void renderAttributeReference(AttributeReference attributeReference);
}
