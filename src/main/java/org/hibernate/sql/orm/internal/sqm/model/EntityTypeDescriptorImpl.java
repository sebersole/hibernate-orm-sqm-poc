/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.sql.orm.internal.sqm.model;

import java.util.HashMap;
import java.util.Map;

import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.persister.walking.spi.AttributeDefinition;
import org.hibernate.sql.orm.internal.mapping.ImprovedEntityPersister;
import org.hibernate.sqm.domain.AttributeDescriptor;
import org.hibernate.sqm.domain.EntityTypeDescriptor;

/**
 * @author Steve Ebersole
 */
public class EntityTypeDescriptorImpl implements EntityTypeDescriptor {
	private final ModelMetadataImpl modelMetadata;
	private final ImprovedEntityPersister persister;
	private final Map<String,AttributeDescriptorImpl> attributeDescriptorMap = new HashMap<String, AttributeDescriptorImpl>();

	public EntityTypeDescriptorImpl(ModelMetadataImpl modelMetadata, ImprovedEntityPersister persister) {
		this.modelMetadata = modelMetadata;
		this.persister = persister;

		// todo : not sure this pulls in subclass attributes
		for ( AttributeDefinition attributeDefinition : persister.getEntityPersister().getAttributes() ) {
			final AttributeDescriptorImpl attributeDescriptor = new AttributeDescriptorImpl(
					this,
					attributeDefinition.getName(),
					modelMetadata.toTypeDescriptor( attributeDefinition.getType() )
			);
			attributeDescriptorMap.put( attributeDefinition.getName(), attributeDescriptor );
		}
	}

	@Override
	public String getTypeName() {
		return persister.getEntityPersister().getEntityName();
	}

	@Override
	public AttributeDescriptor getAttributeDescriptor(String attributeName) {
		if ( attributeDescriptorMap.containsKey( attributeName ) ) {
			return attributeDescriptorMap.get( attributeName );
		}

		if ( "id".equals( attributeName ) ) {
			return new PseudoIdAttributeDescriptor(
					this,
					modelMetadata.toTypeDescriptor( persister.getEntityPersister().getIdentifierType() )
			);
		}

		return null;
	}

	public ImprovedEntityPersister getPersister() {
		return persister;
	}

	public Map<String, AttributeDescriptorImpl> getAttributeDescriptorMap() {
		return attributeDescriptorMap;
	}

}
