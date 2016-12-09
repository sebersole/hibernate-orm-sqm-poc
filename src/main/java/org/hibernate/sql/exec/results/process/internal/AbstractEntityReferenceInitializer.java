/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.exec.results.process.internal;

import java.io.Serializable;
import java.util.List;

import org.hibernate.MappingException;
import org.hibernate.engine.spi.EntityKey;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.persister.entity.spi.ImprovedEntityPersister;
import org.hibernate.sql.NotYetImplementedException;
import org.hibernate.sql.ast.select.SqlSelectionDescriptor;
import org.hibernate.sql.exec.results.process.spi.EntityReferenceProcessingState;
import org.hibernate.sql.exec.results.process.spi.RowProcessingState;
import org.hibernate.sql.exec.results.process.spi2.EntityReferenceInitializer;
import org.hibernate.sql.exec.results.process.spi2.InitializerParent;
import org.hibernate.sql.exec.results.spi.ResolvedEntityReference;

/**
 * @author Steve Ebersole
 */
public abstract class AbstractEntityReferenceInitializer
		extends AbstractFetchParentInitializer
		implements EntityReferenceInitializer {

	// todo : it might be better to simply keep this information here on the initializer.
	//		especially now that initializers are hierarchical.  i believe only the ResolvedEntityReference
	//		instance and its child initializers would need this information, and they can get that via
	//		their parent initializer.

	private final ResolvedEntityReference entityReference;
	private final boolean isEntityReturn;
	private final List<SqlSelectionDescriptor> sqlSelectionDescriptors;
	private final boolean isShallow;

	public AbstractEntityReferenceInitializer(
			InitializerParent parent,
			ResolvedEntityReference entityReference,
			boolean isEntityReturn,
			List<SqlSelectionDescriptor> sqlSelectionDescriptors,
			boolean isShallow) {
		super( parent );
		this.entityReference = entityReference;
		this.isEntityReturn = isEntityReturn;
		this.sqlSelectionDescriptors = sqlSelectionDescriptors;
		this.isShallow = isShallow;
	}

	@Override
	public ResolvedEntityReference getEntityReference() {
		return entityReference;
	}

	@Override
	public void hydrateIdentifier(RowProcessingState rowProcessingState) {
		final EntityReferenceProcessingState entityProcessingState = rowProcessingState.getProcessingState( entityReference );

		// get any previously registered identifier hydrated-state
		Object identifierHydratedForm = entityProcessingState.getIdentifierHydratedForm();
		if ( identifierHydratedForm == null ) {
			// if there is none, read it from the result set
			identifierHydratedForm = buildIdentifierHydratedForm( rowProcessingState, entityProcessingState );

			// broadcast the fact that a hydrated identifier value just became associated with
			// this entity reference
			entityProcessingState.registerIdentifierHydratedForm( identifierHydratedForm );
		}
	}

	private Object buildIdentifierHydratedForm(
			RowProcessingState rowProcessingState,
			EntityReferenceProcessingState entityProcessingState) {
		// todo : we need to decide whether this should be a slice of the current JDBC row values (an Object[]) or the actual id representation (composite, etc)

		// for now assume the JDBC row values slice approach
		//		see how many selections the identifier consumes
		final int selectionsConsumed = entityReference.getEntityPersister().getIdentifierDescriptor().getColumnCount(
				isShallow,
				rowProcessingState.getJdbcValuesSourceProcessingState().getPersistenceContext().getFactory()
		);
		if ( selectionsConsumed == 1 ) {
			return rowProcessingState.getJdbcValues()[ sqlSelectionDescriptors.get( 0 ).getValuesArrayPosition() ];
		}
		else {
			final Object[] value = new Object[selectionsConsumed];
			for ( int i = 0; i < selectionsConsumed; i++ ){
				value[i] = rowProcessingState.getJdbcValues()[ sqlSelectionDescriptors.get( i ).getValuesArrayPosition() ];
			}
			return value;
		}
	}

	@Override
	public void resolveEntityKey(RowProcessingState rowProcessingState) {
		final EntityReferenceProcessingState entityProcessingState = rowProcessingState.getProcessingState( entityReference );

		if ( entityProcessingState.getEntityKey() != null ) {
			return;
		}

		// based on assumption as stated in buildIdentifierHydratedForm, here we'd need to:
		//		1) resolve the value(s) into its identifier representation
		//		2) build and register an EntityKey

		// Step 1
		// todo : need some way on Type to resolve an Object[] into an instance of its Java type.
		//		kind of similar to Type#assemble, although taking the Object[] from cache
		final ImprovedEntityPersister persister = getEntityReference().getEntityPersister();
		final SharedSessionContractImplementor persistenceContext = rowProcessingState.getJdbcValuesSourceProcessingState().getPersistenceContext();

		final Object id = persister.getEntityPersister().getIdentifierType().assemble(
				(Serializable) entityProcessingState.getIdentifierHydratedForm(),
				persistenceContext,
				null
		);

		// Step 2
		final EntityKey entityKey = new EntityKey( (Serializable) id, persister.getEntityPersister() );
		entityProcessingState.registerEntityKey( entityKey );

		scheduleBatchLoadIfNeeded( persister, entityKey, persistenceContext );
	}

	private void scheduleBatchLoadIfNeeded(
			ImprovedEntityPersister entityPersister,
			EntityKey entityKey,
			SharedSessionContractImplementor session) throws MappingException {
		if ( shouldBatchFetch() && entityPersister.getEntityPersister().isBatchLoadable() ) {
			if ( !session.getPersistenceContext().containsEntity( entityKey ) ) {
				session.getPersistenceContext().getBatchFetchQueue().addBatchLoadableEntityKey( entityKey );
			}
		}
	}

	/**
	 * Should we consider this entity reference batchable?
	 */
	protected boolean shouldBatchFetch() {
		return true;
	}

	@Override
	public void hydrateEntityState(RowProcessingState rowProcessingState) {
		throw new NotYetImplementedException(  );
	}

	@Override
	public void finishUpRow(RowProcessingState rowProcessingState) {
		throw new NotYetImplementedException(  );
	}
}
