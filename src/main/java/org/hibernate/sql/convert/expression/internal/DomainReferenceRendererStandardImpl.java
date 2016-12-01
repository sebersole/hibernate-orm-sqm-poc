/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.convert.expression.internal;

import org.hibernate.sql.convert.expression.spi.AbstractDomainReferenceRenderer;

/**
 * DomainReferenceRenderer implementation for normal (shallow) rendering.
 *
 * @author Steve Ebersole
 */
public class DomainReferenceRendererStandardImpl extends AbstractDomainReferenceRenderer {
	public DomainReferenceRendererStandardImpl(RenderingContext renderingContext) {
		super( renderingContext );
	}

}
