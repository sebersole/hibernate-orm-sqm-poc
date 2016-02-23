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

import javax.persistence.TemporalType;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.persister.collection.CollectionPersister;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.sql.gen.NotYetImplementedException;
import org.hibernate.sql.orm.internal.mapping.ImprovedEntityPersisterImpl;
import org.hibernate.sqm.domain.BasicType;
import org.hibernate.sqm.domain.DomainMetamodel;
import org.hibernate.type.AnyType;
import org.hibernate.type.CollectionType;
import org.hibernate.type.CompositeType;
import org.hibernate.type.EntityType;
import org.hibernate.type.StandardBasicTypes;
import org.hibernate.type.Type;

/**
 * @author Steve Ebersole
 */
public class DomainMetamodelImpl implements DomainMetamodel {
	private final SessionFactoryImplementor sessionFactory;

	private final Map<Class, BasicType> basicTypeMap;

	private final Map<EntityPersister,EntityTypeImpl> entityTypeDescriptorMap;
	private Map<String,PolymorphicEntityTypeImpl> polymorphicEntityTypeDescriptorMap;

	public DomainMetamodelImpl(SessionFactoryImplementor sessionFactory) {
		this.sessionFactory = sessionFactory;

		this.basicTypeMap = buildBasicTypeMaps();

		// todo : better account for inheritance
		this.entityTypeDescriptorMap = buildEntityTypeDescriptorMap();
	}

	public SessionFactoryImplementor getSessionFactory() {
		return sessionFactory;
	}

	private static Map<Class, BasicType> buildBasicTypeMaps() {
		final Map<Class,BasicType> map = new HashMap<Class,BasicType>();

		for ( Field field : StandardBasicTypes.class.getDeclaredFields() ) {
			if ( org.hibernate.type.BasicType.class.isAssignableFrom( field.getType() ) ) {
				try {
					final org.hibernate.type.BasicType ormBasicType = (org.hibernate.type.BasicType) field.get( null );
					final BasicType sqmBasicType = new BasicTypeImpl( ormBasicType );
					map.put( sqmBasicType.getJavaType(), sqmBasicType );
				}
				catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}

		return map;
	}

	private Map<EntityPersister, EntityTypeImpl> buildEntityTypeDescriptorMap() {
		final Map<EntityPersister, EntityTypeImpl> map = new HashMap<EntityPersister, EntityTypeImpl>();
		for ( EntityPersister entityPersister : sessionFactory.getEntityPersisters().values() ) {
			map.put(
					entityPersister,
					new EntityTypeImpl(
							this,
							new ImprovedEntityPersisterImpl( entityPersister )
					)
			);
		}
		return map;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> BasicType<T> getBasicType(Class<T> javaType) {
		return toSqmType( sessionFactory.getTypeResolver().basic( javaType.getName() ) );
	}

	@Override
	public <T> BasicType<T> getBasicType(Class<T> javaType, TemporalType temporalType) {
		return null;
	}

	@Override
	public org.hibernate.sqm.domain.EntityType resolveEntityType(Class javaType) {
		return resolveEntityType( javaType.getName() );
	}

	@Override
	public org.hibernate.sqm.domain.EntityType resolveEntityType(String name) {
		final String importedName = sessionFactory.getImportedClassName( name );
		if ( importedName != null ) {
			name = importedName;
		}

		// look at existing non-polymorphic descriptors
		final EntityPersister persister = sessionFactory.getEntityPersister( name );
		if ( persister != null ) {
			return entityTypeDescriptorMap.get( persister );
		}

		// look at existing polymorphic descriptors
		if ( polymorphicEntityTypeDescriptorMap != null ) {
			PolymorphicEntityTypeImpl existingEntry = polymorphicEntityTypeDescriptorMap.get( name );
			if ( existingEntry != null ) {
				return existingEntry;
			}
		}


		final String[] implementors = sessionFactory.getImplementors( name );
		if ( implementors != null ) {
			if ( implementors.length == 1 ) {
				return entityTypeDescriptorMap.get( sessionFactory.getEntityPersister( implementors[0] ) );
			}
			else if ( implementors.length > 1 ) {
				final List<org.hibernate.sqm.domain.EntityType> implementDescriptors = new ArrayList<org.hibernate.sqm.domain.EntityType>();
				for ( String implementor : implementors ) {
					implementDescriptors.add(
							entityTypeDescriptorMap.get( sessionFactory.getEntityPersister( implementor ) )
					);
				}
				if ( polymorphicEntityTypeDescriptorMap == null ) {
					polymorphicEntityTypeDescriptorMap = new HashMap<String, PolymorphicEntityTypeImpl>();
				}
				PolymorphicEntityTypeImpl descriptor = new PolymorphicEntityTypeImpl(
						this,
						name,
						implementDescriptors
				);
				polymorphicEntityTypeDescriptorMap.put( name, descriptor );
				return descriptor;
			}
		}

		throw new HibernateException( "Could not resolve entity reference [" + name + "] from query" );
	}

	public org.hibernate.sqm.domain.Type toSqmType(Type ormType) {
		if ( ormType.isAnyType() ) {
			return toSqmType( (AnyType) ormType );
		}
		else if ( ormType.isEntityType() ) {
			return toSqmType( (EntityType) ormType );
		}
		else if ( ormType.isComponentType() ) {
			return toSqmType( ( CompositeType) ormType );
		}
		else if ( ormType.isCollectionType() ) {
			final CollectionType collectionType = (CollectionType) ormType;
			return toSqmType( (CollectionPersister) collectionType.getAssociatedJoinable( sessionFactory ) );
		}
		else {
			return toSqmType( (org.hibernate.type.BasicType) ormType );
		}
	}

	public org.hibernate.sqm.domain.BasicType toSqmType(org.hibernate.type.BasicType ormBasicType) {
		org.hibernate.sqm.domain.BasicType descriptor = basicTypeMap.get( ormBasicType.getReturnedClass() );
		if ( descriptor == null ) {
			descriptor = new BasicTypeImpl( ormBasicType );
			basicTypeMap.put( ormBasicType.getReturnedClass(), descriptor );
		}
		return descriptor;
	}

	public AnyTypeImpl toSqmType(AnyType ormType) {
		return new AnyTypeImpl(
				toSqmType( ( org.hibernate.type.BasicType) ormType.getDiscriminatorType() ),
				toSqmType( ormType.getIdentifierType() )
		);
	}

	public EntityTypeImpl toSqmType(EntityType entityType) {
		return toSqmType( (EntityPersister) entityType.getAssociatedJoinable( sessionFactory ) );
	}

	public EntityTypeImpl toSqmType(EntityPersister persister) {
		return entityTypeDescriptorMap.get( persister );
	}

	public org.hibernate.sqm.domain.Type toSqmType(CompositeType ormType) {
		throw new NotYetImplementedException();
	}

	public org.hibernate.sqm.domain.Type toSqmType(CollectionType collectionType) {
		return toSqmType( (CollectionPersister) collectionType.getAssociatedJoinable( sessionFactory ) );
	}

	public org.hibernate.sqm.domain.Type toSqmType(CollectionPersister collectionPersister) {
		throw new NotYetImplementedException();
	}
}
