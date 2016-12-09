/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.exec.results.process.internal;

import org.hibernate.engine.spi.EntityKey;
import org.hibernate.loader.plan.spi.EntityFetch;
import org.hibernate.sql.exec.results.process.spi.EntityReferenceProcessingState;
import org.hibernate.sql.exec.results.process.spi.RowProcessingState;
import org.hibernate.sql.exec.results.spi.ResolvedEntityReference;

/**
 * @author Steve Ebersole
 */
public class EntityReferenceProcessingStateImpl implements EntityReferenceProcessingState {
	private final RowProcessingState rowProcessingState;
	private final ResolvedEntityReference entityReference;

	private Boolean wasMissingIdentifier;
	private Object identifierHydratedForm;
	private EntityKey entityKey;
	private Object[] hydratedState;
	private Object entityInstance;

	public EntityReferenceProcessingStateImpl(
			RowProcessingState rowProcessingState,
			ResolvedEntityReference entityReference) {
		this.rowProcessingState = rowProcessingState;
		this.entityReference = entityReference;
	}

	@Override
	public ResolvedEntityReference getEntityReference() {
		return entityReference;
	}

	@Override
	public void registerMissingIdentifier() {
		if ( !EntityFetch.class.isInstance( entityReference ) ) {
			throw new IllegalStateException( "Missing return row identifier" );
		}
		rowProcessingState.registerNonExists( (EntityFetch) entityReference );
		wasMissingIdentifier = true;
	}

	@Override
	public Boolean isMissingIdentifier() {
		return wasMissingIdentifier;
	}

	@Override
	public void registerIdentifierHydratedForm(Object identifierHydratedForm) {
		this.identifierHydratedForm = identifierHydratedForm;
	}

	@Override
	public Object getIdentifierHydratedForm() {
		return identifierHydratedForm;
	}

	@Override
	public void registerEntityKey(EntityKey entityKey) {
		this.entityKey = entityKey;
	}

	@Override
	public EntityKey getEntityKey() {
		return entityKey;
	}

	@Override
	public void registerHydratedState(Object[] hydratedState) {
		this.hydratedState = hydratedState;
	}

	@Override
	public Object[] getHydratedState() {
		return hydratedState;
	}

	@Override
	public void registerEntityInstance(Object entityInstance) {
		this.entityInstance = entityInstance;
	}

	@Override
	public Object getEntityInstance() {
		return entityInstance;
	}
}
