/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.sql.ast.from;

import org.hibernate.persister.entity.EntityPersister;

/**
 * A TableSpecificationGroup for an entity reference
 *
 * @author Steve Ebersole
 */
public class EntityTableSpecificationGroup extends AbstractTableSpecificationGroup {
	private final EntityPersister persister;

	public EntityTableSpecificationGroup(TableSpace tableSpace, String aliasBase, EntityPersister persister) {
		super( tableSpace, aliasBase );
		this.persister = persister;
	}

	public EntityPersister getPersister() {
		return persister;
	}
}
