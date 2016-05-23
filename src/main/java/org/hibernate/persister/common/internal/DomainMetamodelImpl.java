/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.persister.common.internal;

import javax.persistence.TemporalType;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.persister.collection.CollectionPersister;
import org.hibernate.persister.collection.internal.ImprovedCollectionPersisterImpl;
import org.hibernate.persister.collection.spi.ImprovedCollectionPersister;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.sql.gen.NotYetImplementedException;
import org.hibernate.persister.entity.internal.ImprovedEntityPersisterImpl;
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
	private final DatabaseModel databaseModel  = new DatabaseModel();
	private final SessionFactoryImplementor sessionFactory;

	private final Map<Class, BasicType> basicTypeMap;

	private final Map<EntityPersister, ImprovedEntityPersisterImpl> entityTypeDescriptorMap;
	private Map<String,PolymorphicEntityTypeImpl> polymorphicEntityTypeDescriptorMap;

	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// This section needs a bit of explanation...
	//
	// First, note too that it works on the assumption of PersisterFactoryImpl.INSTANCE having
	// been specified as the PersisterFactory used to build the SessionFactory.
	//
	// From there the first thing we do is to ask the PersisterFactoryImpl to "finish up" its
	// processing.  Internally PersisterFactoryImpl builds the legacy persisters and returns
	// them to the caller as per its contracts.  However, additionally for entity persisters
	// it will build the ImprovedEntityPersister variant*.  finishUp performs a second init
	// phase on each of the ImprovedEntityPersister instances, part of which is to build
	// its attribute descriptors.  When a plural attribute is processed, an ImprovedCollectionPersister
	// instance is built, but much like with ImprovedEntityPersister that is just a "shell"; we delay
	// most of its init until the ImprovedCollectionPersister.finishInitialization call done in
	// the DomainMetamodelImpl ctor.
	//
	// So functionally we:
	//		1) build all ImprovedEntityPersister instances
	//		2) finalize all ImprovedEntityPersister instances (side effect being creation of ImprovedCollectionPersister instances)
	//		3) finalize all ImprovedCollectionPersister instances.
	//
	// * - obviously a lot of this changes as we integrate this into ORM properly,  For example
	// the improved persister contracts will just simply be part of the ORM persister contracts so
	// no {persister}->{improved persister} mapping is needed.  Will need some thought on how to locate
	// the "declaring ManagedType" for the improved CollectionPersister from the PersisterFactory
	public DomainMetamodelImpl(SessionFactoryImplementor sessionFactory) {
		this.sessionFactory = sessionFactory;
		this.basicTypeMap = buildBasicTypeMaps();
		this.entityTypeDescriptorMap = PersisterFactoryImpl.INSTANCE.getEntityPersisterMap();
		PersisterFactoryImpl.INSTANCE.finishUp( databaseModel, this );
		for ( ImprovedCollectionPersister improvedCollectionPersister : collectionPersisterMap.values() ) {
			improvedCollectionPersister.finishInitialization( databaseModel, this );
		}
	}

	private Map<CollectionPersister, ImprovedCollectionPersister> collectionPersisterMap = new HashMap<>();

	public void registerCollectionPersister(ImprovedCollectionPersisterImpl persister) {
		collectionPersisterMap.put( persister.getPersister(), persister );
	}
	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	public SessionFactoryImplementor getSessionFactory() {
		return sessionFactory;
	}

	private static Map<Class, BasicType> buildBasicTypeMaps() {
		final Map<Class,BasicType> map = new HashMap<>();

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

	@Override
	@SuppressWarnings("unchecked")
	public <T> BasicType<T> getBasicType(Class<T> javaType) {
		final org.hibernate.type.BasicType ormType = sessionFactory.getTypeResolver().basic( javaType.getName() );
		if ( ormType != null ) {
			return toSqmType( ormType );
		}

		BasicType<T> type = new BasicTypeNonOrmImpl<T>( javaType );
		basicTypeMap.put( javaType, type );

		return type;
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
		final String importedName = sessionFactory.getMetamodel().getImportedClassName( name );
		if ( importedName != null ) {
			name = importedName;
		}

		// look at existing non-polymorphic descriptors
		final EntityPersister persister = sessionFactory.getMetamodel().entityPersister( name );
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


		final String[] implementors = sessionFactory.getMetamodel().getImplementors( name );
		if ( implementors != null ) {
			if ( implementors.length == 1 ) {
				return entityTypeDescriptorMap.get( sessionFactory.getMetamodel().entityPersister( implementors[0] ) );
			}
			else if ( implementors.length > 1 ) {
				final List<org.hibernate.sqm.domain.EntityType> implementDescriptors = new ArrayList<>();
				for ( String implementor : implementors ) {
					implementDescriptors.add(
							entityTypeDescriptorMap.get( sessionFactory.getMetamodel().entityPersister( implementor ) )
					);
				}
				if ( polymorphicEntityTypeDescriptorMap == null ) {
					polymorphicEntityTypeDescriptorMap = new HashMap<>();
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

	@Override
	public BasicType resolveCastTargetType(String name) {
		return toSqmType( (org.hibernate.type.BasicType) sessionFactory.getTypeHelper().heuristicType( name ) );
	}

	public org.hibernate.sqm.domain.Type toSqmType(Type ormType) {
		if ( ormType == null ) {
			return null;
		}
		else if ( ormType.isAnyType() ) {
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
		org.hibernate.sqm.domain.BasicType descriptor = null;
		if ( ormBasicType != null ) {
			descriptor = basicTypeMap.get( ormBasicType.getReturnedClass() );

			if ( descriptor == null ) {
				descriptor = new BasicTypeImpl( ormBasicType );
				basicTypeMap.put( ormBasicType.getReturnedClass(), descriptor );
			}
		}
		return descriptor;
	}

	public AnyTypeImpl toSqmType(AnyType ormType) {
		return new AnyTypeImpl(
				ormType,
				toSqmType( ( org.hibernate.type.BasicType) ormType.getDiscriminatorType() ),
				toSqmType( ormType.getIdentifierType() )
		);
	}

	public ImprovedEntityPersisterImpl toSqmType(EntityType entityType) {
		return toSqmType( (EntityPersister) entityType.getAssociatedJoinable( sessionFactory ) );
	}

	public ImprovedEntityPersisterImpl toSqmType(EntityPersister persister) {
		ImprovedEntityPersisterImpl entityType = entityTypeDescriptorMap.get( persister );
		if ( entityType == null ) {
			throw new IllegalArgumentException( "No ImprovedEntityPersister known for " + persister );
		}
		return entityType;
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
