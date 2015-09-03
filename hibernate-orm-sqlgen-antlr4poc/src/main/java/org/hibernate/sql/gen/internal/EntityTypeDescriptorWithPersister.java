/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: Apache License, Version 2.0
 * See the LICENSE file in the root directory or visit http://www.apache.org/licenses/LICENSE-2.0
 */
package org.hibernate.sql.gen.internal;

import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.sqm.domain.EntityTypeDescriptor;

/**
 * @author Steve Ebersole
 */
public interface EntityTypeDescriptorWithPersister extends EntityTypeDescriptor {
	EntityPersister getPersister();
}
