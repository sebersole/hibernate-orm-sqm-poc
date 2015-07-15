/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.hql.parser;

import org.hibernate.hql.parser.model.EntityTypeDescriptor;

/**
 * Contextual information related to the consumer/caller of the parser - a callback API.
 *
 * @author Steve Ebersole
 */
public interface ConsumerContext {
	EntityTypeDescriptor resolveEntityReference(String reference);

	Class classByName(String name) throws ClassNotFoundException;
}
