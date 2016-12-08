/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.exec.results.spi;

import org.hibernate.persister.entity.spi.ImprovedEntityPersister;

/**
 * Represents a reference to an entity either as a return, fetch, or collection element or index.
 *
 * @author Steve Ebersole
 */
public interface ResolvedEntityReference extends ResolvedFetchParent {
	/**
	 * Retrieves the entity persister describing the entity associated with this Return.
	 */
	ImprovedEntityPersister getEntityPersister();

	/**
	 * Retrieve the Resolved form of an entity's identifier information, which is really
	 * just static information from the referenced {@link ImprovedEntityPersister}.  This
	 * reference is generally used as a unified access to an entity's identifier information,
	 * whether that by any of the simple ids, aggregated-composite ids or
	 * non-aggregated-composite ids.
	 */
	ResolvedEntityIdentifierReference getIdentifierReference();
}
