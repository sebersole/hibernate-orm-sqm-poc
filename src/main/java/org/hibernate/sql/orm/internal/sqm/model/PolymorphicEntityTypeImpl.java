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

import org.hibernate.sql.gen.NotYetImplementedException;
import org.hibernate.sqm.domain.Attribute;
import org.hibernate.sqm.domain.EntityType;
import org.hibernate.sqm.domain.IdentifiableType;
import org.hibernate.sqm.domain.IdentifierDescriptor;
import org.hibernate.sqm.domain.ManagedType;
import org.hibernate.sqm.domain.PolymorphicEntityType;
import org.hibernate.sqm.domain.SingularAttribute;
import org.hibernate.sqm.domain.SingularAttribute.Classification;
import org.hibernate.sqm.domain.Type;

/**
 * @author Steve Ebersole
 */
public class PolymorphicEntityTypeImpl implements PolymorphicEntityType {
	private final String name;
	private final List<EntityType> implementors;

	private final Map<String,AbstractAttributeImpl> attributeDescriptorMap = new HashMap<String, AbstractAttributeImpl>();

	public PolymorphicEntityTypeImpl(
			DomainMetamodelImpl modelMetadata,
			String name,
			List<EntityType> implementors) {
		this.name = name;
		this.implementors = implementors;

		EntityTypeImpl firstImplementor = (EntityTypeImpl) implementors.get( 0 );
		attr_loop: for ( AbstractAttributeImpl attributeDescriptor : firstImplementor.getAttributeDescriptorMap().values() ) {
			for ( EntityType implementor : implementors ) {
				if ( implementor.findAttribute(  attributeDescriptor.getName() ) == null ) {
					break attr_loop;
				}
			}

			// if we get here, every implementor defined that attribute...
			attributeDescriptorMap.put( attributeDescriptor.getName(), attributeDescriptor );
		}

		if ( !attributeDescriptorMap.containsKey( "id" ) ) {
			final org.hibernate.type.Type ormIdType = firstImplementor.getPersister().getEntityPersister().getIdentifierType();
			final Classification idClassification = Helper.interpretIdentifierClassification( ormIdType );
			attributeDescriptorMap.put(
					"id",
					new PseudoIdAttributeImpl(
							this,
							modelMetadata.toSqmType( ormIdType ),
							idClassification
					)
			);
		}
	}

	@Override
	public Collection<EntityType> getImplementors() {
		return implementors;
	}

	@Override
	public String getTypeName() {
		return name;
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
		return attributeDescriptorMap.get( attributeName );
	}

	@Override
	public Attribute findDeclaredAttribute(String attributeName) {
		return findAttribute( attributeName );
	}
}
