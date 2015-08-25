/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.sql.gen.sqm;

import java.util.Collection;
import java.util.List;

import org.hibernate.sqm.domain.AttributeDescriptor;
import org.hibernate.sqm.domain.EntityTypeDescriptor;
import org.hibernate.sqm.domain.PolymorphicEntityTypeDescriptor;

/**
 * @author Steve Ebersole
 */
public class PolymorphicEntityTypeDescriptorImpl implements PolymorphicEntityTypeDescriptor {
	private final String name;
	private final List<EntityTypeDescriptor> descriptors;

	public PolymorphicEntityTypeDescriptorImpl(
			String name,
			List<EntityTypeDescriptor> descriptors) {
		this.name = name;
		this.descriptors = descriptors;
	}

	@Override
	public Collection<EntityTypeDescriptor> getImplementors() {
		return descriptors;
	}

	@Override
	public String getTypeName() {
		return name;
	}

	@Override
	public AttributeDescriptor getAttributeDescriptor(String attributeName) {
		return null;
	}
}
