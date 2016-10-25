/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.convert.internal;

import org.hibernate.sql.ast.expression.AttributeReference;
import org.hibernate.sql.ast.expression.EntityReference;
import org.hibernate.sql.convert.spi.DomainReferenceRendererTemplate;

/**
 * @author Steve Ebersole
 */
public class DomainReferenceRendererStandardImpl extends DomainReferenceRendererTemplate {
	public DomainReferenceRendererStandardImpl(RenderingContext renderingContext) {
		super( renderingContext );
	}

	@Override
	protected void renderEntityReference(EntityReference entityReference) {
		renderColumnBindings(
				entityReference.getImprovedEntityPersister().resolveColumnBindings( entityReference.getTableBinding(), true )
		);
	}

	@Override
	protected void renderAttributeReference(AttributeReference attributeReference) {
		renderColumnBindings( attributeReference.getColumnBindings() );
	}
}
