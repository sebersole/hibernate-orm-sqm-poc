/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.exec.results.process.internal;

import org.hibernate.sql.NotYetImplementedException;
import org.hibernate.sql.exec.results.process.spi2.FetchInitializer;
import org.hibernate.sql.exec.results.process.spi2.InitializerParent;
import org.hibernate.sql.exec.results.spi.ResolvedEntityReference;

/**
 * @author Steve Ebersole
 */
public class EntityFetchInitializerImpl extends AbstractEntityReferenceInitializer implements FetchInitializer {
	public EntityFetchInitializerImpl(
			InitializerParent parent,
			ResolvedEntityReference entityReference,
			boolean isShallow) {
		super( parent, entityReference, false, isShallow );
	}

	@Override
	public void link(Object fkValue) {
		throw new NotYetImplementedException(  );
	}
}
