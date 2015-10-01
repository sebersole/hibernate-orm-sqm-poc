/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.sql.orm.internal.sqm.model;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.persister.collection.CollectionPersister;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.sql.gen.NotYetImplementedException;
import org.hibernate.sql.orm.internal.mapping.ImprovedEntityPersisterImpl;
import org.hibernate.sqm.domain.BasicTypeDescriptor;
import org.hibernate.sqm.domain.EntityTypeDescriptor;
import org.hibernate.sqm.domain.ModelMetadata;
import org.hibernate.sqm.domain.StandardBasicTypeDescriptors;
import org.hibernate.sqm.domain.TypeDescriptor;
import org.hibernate.type.AnyType;
import org.hibernate.type.CollectionType;
import org.hibernate.type.CompositeType;
import org.hibernate.type.EntityType;
import org.hibernate.type.Type;

/**
 * @author Steve Ebersole
 */
public class ModelMetadataImpl implements ModelMetadata {
	private final SessionFactoryImplementor sessionFactory;

	private final Map<Class, BasicTypeDescriptor> basicTypeMap;

	private final Map<EntityPersister,EntityTypeDescriptorImpl> entityTypeDescriptorMap;
	private Map<String,PolymorphicEntityTypeDescriptorImpl> polymorphicEntityTypeDescriptorMap;


	public ModelMetadataImpl(SessionFactoryImplementor sessionFactory) {
		this.sessionFactory = sessionFactory;
		this.basicTypeMap = buildBasicTypeMaps();

		// todo : better account for inheritance
		this.entityTypeDescriptorMap = buildEntityTypeDescriptorMap();
	}

	private static Map<Class, BasicTypeDescriptor> buildBasicTypeMaps() {
		final Map<Class, BasicTypeDescriptor> map = new HashMap<Class, BasicTypeDescriptor>();

		for ( Field field : StandardBasicTypeDescriptors.class.getDeclaredFields() ) {
			if ( BasicTypeDescriptor.class.isAssignableFrom( field.getType() ) ) {
				try {
					final BasicTypeDescriptor descriptor = (BasicTypeDescriptor) field.get( StandardBasicTypeDescriptors.INSTANCE );
					map.put( descriptor.getCorrespondingJavaType(), descriptor );
				}
				catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}

		return map;
	}

	private Map<EntityPersister, EntityTypeDescriptorImpl> buildEntityTypeDescriptorMap() {
		final Map<EntityPersister, EntityTypeDescriptorImpl> map = new HashMap<EntityPersister, EntityTypeDescriptorImpl>();
		for ( EntityPersister entityPersister : sessionFactory.getEntityPersisters().values() ) {
			map.put(
					entityPersister,
					new EntityTypeDescriptorImpl(
							this,
							new ImprovedEntityPersisterImpl( entityPersister )
					)
			);
		}
		return map;
	}

	@Override
	public EntityTypeDescriptor resolveEntityReference(String reference) {
		final String importedName = sessionFactory.getImportedClassName( reference );
		if ( importedName != null ) {
			reference = importedName;
		}

		// look at existing non-polymorphic descriptors
		final EntityPersister persister = sessionFactory.getEntityPersister( reference );
		if ( persister != null ) {
			return entityTypeDescriptorMap.get( persister );
		}

		// look at existing polymorphic descriptors
		if ( polymorphicEntityTypeDescriptorMap != null ) {
			PolymorphicEntityTypeDescriptorImpl existingEntry = polymorphicEntityTypeDescriptorMap.get( reference );
			if ( existingEntry != null ) {
				return existingEntry;
			}
		}


		final String[] implementors = sessionFactory.getImplementors( reference );
		if ( implementors != null ) {
			if ( implementors.length == 1 ) {
				return entityTypeDescriptorMap.get( sessionFactory.getEntityPersister( implementors[0] ) );
			}
			else if ( implementors.length > 1 ) {
				final List<EntityTypeDescriptor> implementDescriptors = new ArrayList<EntityTypeDescriptor>();
				for ( String implementor : implementors ) {
					implementDescriptors.add(
							entityTypeDescriptorMap.get( sessionFactory.getEntityPersister( implementor ) )
					);
				}
				if ( polymorphicEntityTypeDescriptorMap == null ) {
					polymorphicEntityTypeDescriptorMap = new HashMap<String, PolymorphicEntityTypeDescriptorImpl>();
				}
				PolymorphicEntityTypeDescriptorImpl descriptor = new PolymorphicEntityTypeDescriptorImpl(
						this,
						reference,
						implementDescriptors
				);
				polymorphicEntityTypeDescriptorMap.put( reference, descriptor );
				return descriptor;
			}
		}

		throw new HibernateException( "Could not resolve entity reference [" + reference + "] from query" );
	}

	public TypeDescriptor toTypeDescriptor(Type ormType) {
		if ( ormType.isAnyType() ) {
			anyType( (AnyType) ormType );
		}
		else if ( ormType.isEntityType() ) {
			entityType( (EntityType) ormType );
		}
		else if ( ormType.isComponentType() ) {
			return compositeType( ( CompositeType) ormType );
		}
		else if ( ormType.isCollectionType() ) {
			final CollectionType collectionType = (CollectionType) ormType;
			return collectionType( (CollectionPersister) collectionType.getAssociatedJoinable( sessionFactory ) );
		}
		else {
			return basicType( ormType.getReturnedClass() );
		}

		throw new HibernateException( "Unexpected Type implementation : " + ormType );
	}

	public AnyTypeDescriptorImpl anyType(AnyType ormType) {
		throw new NotYetImplementedException();
	}

	public EntityTypeDescriptorImpl entityType(EntityType entityType) {
		return entityType( (EntityPersister) entityType.getAssociatedJoinable( sessionFactory ) );
	}

	public EntityTypeDescriptorImpl entityType(EntityPersister persister) {
		return entityTypeDescriptorMap.get( persister );
	}

	public TypeDescriptor compositeType(CompositeType ormType) {
		throw new NotYetImplementedException();
	}

	public TypeDescriptor collectionType(CollectionType collectionType) {
		return collectionType( (CollectionPersister) collectionType.getAssociatedJoinable( sessionFactory ) );
	}

	public TypeDescriptor collectionType(CollectionPersister collectionPersister) {
		throw new NotYetImplementedException();
	}

	public BasicTypeDescriptor basicType(Class javaType) {
		BasicTypeDescriptor descriptor = basicTypeMap.get( javaType );
		if ( descriptor == null ) {
			descriptor = new BasicTypeDescriptorImpl( javaType );
			basicTypeMap.put( javaType, descriptor );
		}
		return descriptor;
	}
}
