/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.exec.results.process.internal;

import java.sql.SQLException;

import org.hibernate.sql.NotYetImplementedException;
import org.hibernate.sql.exec.results.process.spi.RowProcessingState;
import org.hibernate.sql.exec.results.process.spi2.EntityReferenceInitializer;
import org.hibernate.sql.exec.results.process.spi2.FetchInitializer;
import org.hibernate.sql.exec.results.process.spi2.InitializerParent;
import org.hibernate.sql.exec.results.spi.ResolvedEntityReference;

/**
 * @author Steve Ebersole
 */
public abstract class AbstractEntityReferenceInitializer
		extends AbstractFetchParentInitializer
		implements EntityReferenceInitializer, FetchInitializer {
	private final ResolvedEntityReference entityReference;
	private final boolean isEntityReturn;
	private final boolean isShallow;

	public AbstractEntityReferenceInitializer(
			InitializerParent parent,
			ResolvedEntityReference entityReference,
			boolean isEntityReturn,
			boolean isShallow) {
		super( parent );
		this.entityReference = entityReference;
		this.isEntityReturn = isEntityReturn;
		this.isShallow = isShallow;
	}

	@Override
	public ResolvedEntityReference getEntityReference() {
		return entityReference;
	}

	@Override
	public void hydrateIdentifier(RowProcessingState rowProcessingState) throws SQLException {
		throw new NotYetImplementedException(  );
	}

	@Override
	public void resolveEntityKey(RowProcessingState rowProcessingState) throws SQLException {
		throw new NotYetImplementedException(  );
	}

	@Override
	public void hydrateEntityState(RowProcessingState rowProcessingState) throws SQLException {
		throw new NotYetImplementedException(  );
	}

	@Override
	public void finishUpRow(RowProcessingState rowProcessingState) throws SQLException {
		throw new NotYetImplementedException(  );
	}
}
