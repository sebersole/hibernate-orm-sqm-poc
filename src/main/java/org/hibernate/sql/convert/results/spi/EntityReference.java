/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.convert.results.spi;

import org.hibernate.persister.entity.spi.ImprovedEntityPersister;

/**
 * Represents a reference to an entity either as a return, fetch, or collection element or index.
 *
 * @author Steve Ebersole
 */
public interface EntityReference extends FetchParent {
	/**
	 * Retrieves the entity persister describing the entity associated with this Return.
	 *
	 * @return The EntityPersister.
	 */
	ImprovedEntityPersister getEntityPersister();

	/**
	 * Obtain the unique-identifier of the TableGroup (specifically a
	 * {@link org.hibernate.sql.ast.from.EntityTableGroup}) that this
	 * EntityReference refers to.
	 *
	 * @return The unique-identifier
	 */
	String getTableGroupUniqueIdentifier();

	/**
	 * Get the description of the entity's identifier, specific to this query
	 *
	 * @return The identifier description.
	 */
	EntityIdentifierReference getIdentifierReference();
}
