/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.sql.orm.internal.sqm.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.sqm.domain.AttributeDescriptor;
import org.hibernate.sqm.domain.EntityTypeDescriptor;
import org.hibernate.sqm.domain.PolymorphicEntityTypeDescriptor;

/**
 * @author Steve Ebersole
 */
public class PolymorphicEntityTypeDescriptorImpl implements PolymorphicEntityTypeDescriptor {
	private final String name;
	private final List<EntityTypeDescriptor> implementors;
	private final Map<String,AttributeDescriptorImpl> attributeDescriptorMap = new HashMap<String, AttributeDescriptorImpl>();

	public PolymorphicEntityTypeDescriptorImpl(
			ModelMetadataImpl modelMetadata,
			String name,
			List<EntityTypeDescriptor> implementors) {
		this.name = name;
		this.implementors = implementors;

		EntityTypeDescriptorImpl firstImplementor = (EntityTypeDescriptorImpl) implementors.get( 0 );
		attr_loop: for ( AttributeDescriptorImpl attributeDescriptor : firstImplementor.getAttributeDescriptorMap().values() ) {
			for ( EntityTypeDescriptor implementor : implementors ) {
				if ( implementor.getAttributeDescriptor(  attributeDescriptor.getName() ) == null ) {
					break attr_loop;
				}
			}

			// if we get here, every implementor defined that attribute...
			attributeDescriptorMap.put( attributeDescriptor.getName(), attributeDescriptor );
		}

		if ( !attributeDescriptorMap.containsKey( "id" ) ) {
			attributeDescriptorMap.put(
					"id",
					new PseudoIdAttributeDescriptor(
							this,
							modelMetadata.toTypeDescriptor( firstImplementor.getPersister().getEntityPersister().getIdentifierType() )
					)
			);
		}
	}

	@Override
	public Collection<EntityTypeDescriptor> getImplementors() {
		return implementors;
	}

	@Override
	public String getTypeName() {
		return name;
	}

	@Override
	public AttributeDescriptor getAttributeDescriptor(String attributeName) {
		return attributeDescriptorMap.get( attributeName );
	}
}
