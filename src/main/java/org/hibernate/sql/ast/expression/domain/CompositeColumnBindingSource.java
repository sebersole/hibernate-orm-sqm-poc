/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.ast.expression.domain;

import java.util.Arrays;
import java.util.List;

import org.hibernate.sql.ast.from.ColumnBinding;

import org.jboss.logging.Logger;

/**
 * @author Steve Ebersole
 */
public class CompositeColumnBindingSource implements ColumnBindingSource {
	private static final Logger log = Logger.getLogger( CompositeColumnBindingSource.class );

	final List<ColumnBindingSource> components;

	public CompositeColumnBindingSource(ColumnBindingSource... components) {
		this.components = Arrays.asList( components );
	}

	@Override
	public List<ColumnBinding> resolveColumnBindings(
			DomainReferenceExpression domainReference,
			boolean shallow) {
		for ( ColumnBindingSource component : components ) {
			try {
				return component.resolveColumnBindings( domainReference, shallow );
			}
			catch (Exception e) {
				// go on to the next
				log.debugf(
						"Could not resolve column bindings from component source [%s], continuing search",
						component
				);
			}
		}

		throw new IllegalStateException( "Could not resolve column bindings : " + domainReference );
	}
}
