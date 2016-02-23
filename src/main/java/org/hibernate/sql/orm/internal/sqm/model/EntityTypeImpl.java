/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.sql.orm.internal.sqm.model;

import java.util.HashMap;
import java.util.Map;

import org.hibernate.persister.walking.spi.AttributeDefinition;
import org.hibernate.sql.gen.NotYetImplementedException;
import org.hibernate.sql.orm.internal.mapping.ImprovedEntityPersister;
import org.hibernate.sqm.domain.Attribute;
import org.hibernate.sqm.domain.EntityType;
import org.hibernate.sqm.domain.IdentifiableType;
import org.hibernate.sqm.domain.IdentifierDescriptor;
import org.hibernate.sqm.domain.ManagedType;
import org.hibernate.sqm.domain.SingularAttribute;
import org.hibernate.sqm.domain.Type;
import org.hibernate.type.CollectionType;

/**
 * @author Steve Ebersole
 */
public class EntityTypeImpl implements EntityType {
	private final DomainMetamodelImpl modelMetadata;
	private final ImprovedEntityPersister persister;

	private final Map<String,AbstractAttributeImpl> attributeDescriptorMap = new HashMap<String, AbstractAttributeImpl>();

	public EntityTypeImpl(DomainMetamodelImpl modelMetadata, ImprovedEntityPersister persister) {
		this.modelMetadata = modelMetadata;
		this.persister = persister;

		// todo : not sure this pulls in subclass attributes
		for ( AttributeDefinition attributeDefinition : persister.getEntityPersister().getAttributes() ) {
			final AbstractAttributeImpl attributeDescriptor;
			if ( attributeDefinition.getType().isCollectionType() ) {
				final CollectionType ormCollectionType = (CollectionType) attributeDefinition.getType();
				final Helper.CollectionMetadata collectionMetadata = Helper.interpretCollectionMetadata(
						modelMetadata.getSessionFactory(),
						ormCollectionType
				);
				attributeDescriptor = new PluralAttributeImpl(
						this,
						attributeDefinition.getName(),
						collectionMetadata.getCollectionClassification(),
						collectionMetadata.getElementClassification(),
						modelMetadata.toSqmType( collectionMetadata.getCollectionIdType() ),
						modelMetadata.toSqmType( collectionMetadata.getIndexType() ),
						modelMetadata.toSqmType( collectionMetadata.getElementType() )
				);
			}
			else {
				attributeDescriptor = new SingularAttributeImpl(
						this,
						attributeDefinition.getName(),
						Helper.interpretSingularAttributeClassification( attributeDefinition.getType() ),
						modelMetadata.toSqmType( attributeDefinition.getType() )
				);
			}
			attributeDescriptorMap.put( attributeDefinition.getName(), attributeDescriptor );
		}
	}

	@Override
	public String getTypeName() {
		return persister.getEntityPersister().getEntityName();
	}

	@Override
	public String getName() {
		return getTypeName();
	}

	@Override
	public Type getBoundType() {
		return this;
	}

	@Override
	public ManagedType asManagedType() {
		return this;
	}

	@Override
	public IdentifiableType getSuperType() {
		// todo : implement
		throw new NotYetImplementedException();
	}

	@Override
	public IdentifierDescriptor getIdentifierDescriptor() {
		// todo : implement
		throw new NotYetImplementedException();
	}

	@Override
	public SingularAttribute getVersionAttribute() {
		// todo : implement
		throw new NotYetImplementedException();
	}

	@Override
	public Attribute findAttribute(String attributeName) {
		if ( attributeDescriptorMap.containsKey( attributeName ) ) {
			return attributeDescriptorMap.get( attributeName );
		}

		if ( "id".equals( attributeName ) ) {
			return new PseudoIdAttributeImpl(
					this,
					modelMetadata.toSqmType( persister.getEntityPersister().getIdentifierType() ),
					Helper.interpretIdentifierClassification( persister.getEntityPersister().getIdentifierType() )
			);
		}

		return null;
	}

	@Override
	public Attribute findDeclaredAttribute(String name) {
		// todo : implement
		throw new NotYetImplementedException();
	}

	public ImprovedEntityPersister getPersister() {
		return persister;
	}

	public Map<String, AbstractAttributeImpl> getAttributeDescriptorMap() {
		return attributeDescriptorMap;
	}

}
