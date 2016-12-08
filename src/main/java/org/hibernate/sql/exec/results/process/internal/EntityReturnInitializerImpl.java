/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.exec.results.process.internal;

import org.hibernate.sql.exec.results.process.spi2.EntityReferenceInitializer;
import org.hibernate.sql.exec.results.spi.ResolvedEntityReference;

/**
 * @author Steve Ebersole
 */
public class EntityReturnInitializerImpl
		extends AbstractEntityReferenceInitializer
		implements EntityReferenceInitializer {
	public EntityReturnInitializerImpl(
			ResolvedEntityReference entityReference,
			boolean isShallow) {
		super( null, entityReference, true, isShallow );
	}

	@Override
	public void link(Object fkValue) {
		// nothing to do here
	}
}
