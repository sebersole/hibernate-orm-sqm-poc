/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.sql.ast.from;

import org.hibernate.persister.collection.CollectionPersister;

/**
 * A TableSpecificationGroup for a collection reference
 *
 * @author Steve Ebersole
 */
public class CollectionTableSpecificationGroup extends AbstractTableSpecificationGroup {
	private final CollectionPersister persister;

	public CollectionTableSpecificationGroup(
			TableSpace tableSpace,
			String aliasBase,
			CollectionPersister persister) {
		super( tableSpace, aliasBase );
		this.persister = persister;
	}

	public CollectionPersister getPersister() {
		return persister;
	}
}
