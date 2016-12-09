/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.exec.results.process.spi;

import org.hibernate.engine.spi.EntityKey;
import org.hibernate.loader.plan.spi.EntityFetch;
import org.hibernate.loader.plan.spi.Fetch;
import org.hibernate.persister.entity.spi.EntityReference;
import org.hibernate.sql.ast.expression.domain.DomainReferenceExpression;
import org.hibernate.sql.exec.results.spi.ResolvedEntityReference;

/**
 * State pertaining to the processing of a single row of a JdbcValuesSource
 *
 * @author Steve Ebersole
 */
public interface RowProcessingState {
	JdbcValuesSourceProcessingState getJdbcValuesSourceProcessingState();

//	boolean next() throws SQLException;
	Object[] getJdbcValues();

	void registerNonExists(EntityFetch fetch);
	void registerHydratedEntity(EntityReference entityReference, EntityKey entityKey, Object entityInstance);

	EntityReferenceProcessingState getProcessingState(ResolvedEntityReference resolvedEntityReference);

	EntityReferenceProcessingState getProcessingState(DomainReferenceExpression expression);
	EntityReferenceProcessingState getOwnerProcessingState(Fetch fetch);

	void finishRowProcessing();
}
