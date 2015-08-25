/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.sql.gen.sqm;

import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.sqm.domain.AttributeDescriptor;
import org.hibernate.sqm.domain.EntityTypeDescriptor;

/**
 * @author Steve Ebersole
 */
public class EntityTypeDescriptorImpl implements EntityTypeDescriptor {
	private final EntityPersister persister;

	public EntityTypeDescriptorImpl(EntityPersister persister) {
		this.persister = persister;
	}

	@Override
	public String getTypeName() {
		return persister.getEntityName();
	}

	@Override
	public AttributeDescriptor getAttributeDescriptor(String attributeName) {
		return null;
	}
}
