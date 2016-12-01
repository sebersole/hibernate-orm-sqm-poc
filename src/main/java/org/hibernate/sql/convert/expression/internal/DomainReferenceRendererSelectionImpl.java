/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.convert.expression.internal;

import org.hibernate.sql.convert.expression.spi.AbstractDomainReferenceRenderer;

/**
 * DomainReferenceRenderer implementation for rendering expressions used as selections
 * in the select-clause.  Rendering depends on whether we should do shallow rendering
 * ({@link org.hibernate.query.Query#iterate}) or not.
 *
 * @author Steve Ebersole
 */
public class DomainReferenceRendererSelectionImpl extends AbstractDomainReferenceRenderer {
	private final boolean isShallow;

	public DomainReferenceRendererSelectionImpl(RenderingContext renderingContext, boolean isShallow) {
		super( renderingContext );
		this.isShallow = isShallow;
	}
}
