/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.exec.results.process.spi;

import org.hibernate.engine.spi.EntityKey;
import org.hibernate.sql.exec.results.spi.ResolvedEntityReference;

/**
 * Holds all pieces of information known about an EntityReference in relation to
 * each row as we process the ResultSet - caches these values and makes it easy
 * to access while processing Fetches.
 *
 * @author Steve Ebersole
 */
public interface EntityReferenceProcessingState {
	/**
	 * The ResolvedEntityReference for which this is collecting process state
	 */
	ResolvedEntityReference getEntityReference();

	/**
	 * Register the fact that no identifier was found on attempt to hydrate it from ResultSet
	 */
	void registerMissingIdentifier();

	/**
	 *
	 * @return
	 */
	Boolean isMissingIdentifier();

	/**
	 * Register the hydrated form (raw Type-read ResultSet values) of the entity's identifier for the row
	 * currently being processed.
	 *
	 * @param hydratedForm The entity identifier hydrated state
	 */
	void registerIdentifierHydratedForm(Object hydratedForm);

	/**
	 * Obtain the hydrated form (the raw Type-read ResultSet values) of the entity's identifier
	 *
	 * @return The entity identifier hydrated state
	 */
	Object getIdentifierHydratedForm();

	/**
	 * Register the processed EntityKey for this Entity for the row currently being processed.
	 *
	 * @param entityKey The processed EntityKey for this EntityReference
	 */
	void registerEntityKey(EntityKey entityKey);

	/**
	 * Obtain the registered EntityKey for this EntityReference for the row currently being processed.
	 *
	 * @return The registered EntityKey for this EntityReference
	 */
	EntityKey getEntityKey();

	void registerHydratedState(Object[] hydratedState);
	Object[] getHydratedState();

	// usually uninitialized at this point
	void registerEntityInstance(Object instance);

	// may be uninitialized
	Object getEntityInstance();
}
