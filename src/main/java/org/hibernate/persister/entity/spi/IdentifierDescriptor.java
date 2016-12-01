/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.persister.entity.spi;

import org.hibernate.persister.common.spi.Column;
import org.hibernate.persister.common.spi.SingularAttributeDescriptor;
import org.hibernate.type.Type;

/**
 * @author Steve Ebersole
 */
public interface IdentifierDescriptor extends SingularAttributeDescriptor {
	Type getIdType();

	/**
	 * Is this identifier defined by a single attribute on the entity?
	 * <p/>
	 * The only time this is false is in the case of a non-aggregated composite identifier.
	 *
	 * @return {@code false} indicates we have a non-aggregated composite identifier.
	 */
	boolean hasSingleIdAttribute();

	/**
	 * Get a SingularAttributeImplementor representation of the identifier.
	 * <p/>
	 * Note that for the case of a non-aggregated composite identifier this returns a
	 * "virtual" attribute mapping
	 *
	 * @return
	 */
	SingularAttributeDescriptor getIdAttribute();

	Column[] getColumns();
}
