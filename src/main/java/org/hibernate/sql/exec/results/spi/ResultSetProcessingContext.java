/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.exec.results.spi;

import org.hibernate.engine.spi.EntityKey;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.loader.plan.spi.EntityFetch;
import org.hibernate.loader.plan.spi.EntityReference;
import org.hibernate.loader.plan.spi.Fetch;

/**
 * Provides a context for processing a ResultSet.  Holds in-flight state
 * and provides access to environmental information needed to perform the
 * processing.
 *
 * @author Steve Ebersole
 */
public interface ResultSetProcessingContext {
	SessionImplementor getSession();

	void registerNonExists(EntityFetch fetch);
	void registerHydratedEntity(EntityReference entityReference, EntityKey entityKey, Object entityInstance);

	EntityReferenceProcessingState getProcessingState(EntityReference entityReference);
	EntityReferenceProcessingState getOwnerProcessingState(Fetch fetch);

}
