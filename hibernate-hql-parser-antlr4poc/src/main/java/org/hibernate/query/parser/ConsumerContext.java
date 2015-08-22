/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.query.parser;

import org.hibernate.sqm.domain.ModelMetadata;

/**
 * Contextual information related to the consumer/caller of the parser - a callback API.
 *
 * @author Steve Ebersole
 */
public interface ConsumerContext extends ModelMetadata {
	/**
	 * Resolve any (potential) non-entity class reference encountered in the query.
	 *
	 * @param name The name of the class to locate
	 *
	 * @return The Class reference
	 *
	 * @throws ClassNotFoundException
	 */
	Class classByName(String name) throws ClassNotFoundException;
}
