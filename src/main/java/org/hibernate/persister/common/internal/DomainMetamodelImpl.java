/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.persister.common.internal;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.persister.collection.CollectionPersister;
import org.hibernate.persister.collection.internal.ImprovedCollectionPersisterImpl;
import org.hibernate.persister.collection.spi.ImprovedCollectionPersister;
import org.hibernate.persister.common.spi.SqmTypeImplementor;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.persister.entity.internal.ImprovedEntityPersisterImpl;
import org.hibernate.persister.entity.spi.AttributeReferenceSource;
import org.hibernate.persister.entity.spi.ImprovedEntityPersister;
import org.hibernate.sqm.domain.AttributeReference;
import org.hibernate.sqm.domain.BasicType;
import org.hibernate.sqm.domain.DomainMetamodel;
import org.hibernate.sqm.domain.DomainReference;
import org.hibernate.sqm.domain.EntityReference;
import org.hibernate.sqm.domain.NoSuchAttributeException;
import org.hibernate.sqm.query.expression.BinaryArithmeticSqmExpression;
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
	private Map<String,PolymorphicEntityReferenceImpl> polymorphicEntityTypeDescriptorMap;

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

	private Map<CollectionPersister, ImprovedCollectionPersister> collectionPersisterMap = new HashMap<>();

	public void registerCollectionPersister(ImprovedCollectionPersisterImpl persister) {
		collectionPersisterMap.put( persister.getPersister(), persister );
	}


	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// DomainMetamodel impl

	public SessionFactoryImplementor getSessionFactory() {
		return sessionFactory;
	}

	@Override
	public EntityReference resolveEntityReference(String entityName) {
		final String importedName = sessionFactory.getMetamodel().getImportedClassName( entityName );
		if ( importedName != null ) {
			entityName = importedName;
		}

		// look at existing non-polymorphic descriptors
		final EntityPersister persister = sessionFactory.getMetamodel().entityPersister( entityName );
		if ( persister != null ) {
			return entityTypeDescriptorMap.get( persister );
		}

		// look at existing polymorphic descriptors
		if ( polymorphicEntityTypeDescriptorMap != null ) {
			PolymorphicEntityReferenceImpl existingEntry = polymorphicEntityTypeDescriptorMap.get( entityName );
			if ( existingEntry != null ) {
				return existingEntry;
			}
		}


		final String[] implementors = sessionFactory.getMetamodel().getImplementors( entityName );
		if ( implementors != null ) {
			if ( implementors.length == 1 ) {
				return entityTypeDescriptorMap.get( sessionFactory.getMetamodel().entityPersister( implementors[0] ) );
			}
			else if ( implementors.length > 1 ) {
				final List<ImprovedEntityPersister> implementDescriptors = new ArrayList<>();
				for ( String implementor : implementors ) {
					implementDescriptors.add(
							entityTypeDescriptorMap.get( sessionFactory.getMetamodel().entityPersister( implementor ) )
					);
				}
				if ( polymorphicEntityTypeDescriptorMap == null ) {
					polymorphicEntityTypeDescriptorMap = new HashMap<>();
				}
				PolymorphicEntityReferenceImpl descriptor = new PolymorphicEntityReferenceImpl(
						this,
						entityName,
						implementDescriptors
				);
				polymorphicEntityTypeDescriptorMap.put( entityName, descriptor );
				return descriptor;
			}
		}

		throw new HibernateException( "Could not resolve entity reference [" + entityName + "] from query" );
	}

	@Override
	public EntityReference resolveEntityReference(Class javaType) {
		return resolveEntityReference( javaType.getName() );
	}

	@Override
	public AttributeReference resolveAttributeReference(DomainReference source, String attributeName) {
		final AttributeReference attrRef = locateAttributeReference( source, attributeName );
		if ( attrRef == null ) {
			throw new NoSuchAttributeException( "Could not locate attribute named [" + attributeName + "] relative to [" + source.asLoggableText() + "]" );
		}
		return attrRef;
	}

	@Override
	public AttributeReference locateAttributeReference(DomainReference sourceBinding, String attributeName) {
		if ( sourceBinding instanceof AttributeReferenceSource ) {
			return ( (AttributeReferenceSource) sourceBinding ).findAttribute( attributeName );
		}

		if ( sourceBinding instanceof SingularAttributeEmbedded ) {
			return ( (SingularAttributeEmbedded) sourceBinding ).getEmbeddablePersister().findAttribute( attributeName );
		}

		if ( sourceBinding instanceof SqmTypeImplementor ) {
			return resolveAttributeReferenceSource( ( (SqmTypeImplementor) sourceBinding ) ).findAttribute( attributeName );
		}

		throw new IllegalArgumentException( "Unexpected type [" + sourceBinding + "] passed as 'attribute source'" );
	}

	private AttributeReferenceSource resolveAttributeReferenceSource(SqmTypeImplementor typeAccess) {
		final Type type = typeAccess.getOrmType();
		if ( type instanceof EntityType ) {
			return (ImprovedEntityPersister) resolveEntityReference( ( (EntityType) type ).getAssociatedEntityName( sessionFactory ) );
		}
		else if ( type instanceof CollectionType ) {
			return resolveAttributeReferenceSource(
					collectionPersisterMap.get( sessionFactory.getMetamodel().collectionPersister( ( (CollectionType) type ).getRole() ) ).getElementReference()
			);
		}
		else if ( type instanceof CompositeType ) {
			throw new org.hibernate.cfg.NotYetImplementedException( "Resolving CompositeType attribute references is not yet implemented; requires Type system changes" );
		}

		throw new IllegalArgumentException( "Unexpected type [" + typeAccess + "] passed" );
	}


	@Override
	public BasicType resolveBasicType(Class javaType) {
		// see if we've cached it so far...
		BasicType basicType = basicTypeMap.get( javaType );
		if ( basicType == null ) {
			basicType = new BasicTypeNonOrmImpl( javaType );
			basicTypeMap.put( javaType, basicType );
		}
		return basicType;
	}

	@Override
	public BasicType resolveArithmeticType(
			DomainReference firstType,
			DomainReference secondType,
			BinaryArithmeticSqmExpression.Operation operation) {
		return resolveArithmeticType( toBasicType( firstType ), toBasicType( secondType ), operation );
	}

	private BasicType toBasicType(DomainReference domainReference) {
		if ( domainReference == null ) {
			return null;
		}

		if ( domainReference instanceof BasicType ) {
			return (BasicType) domainReference;
		}

		if ( domainReference instanceof SqmTypeImplementor ) {
			return resolveBasicType( ( (SqmTypeImplementor) domainReference ).getOrmType().getReturnedClass() );
		}

		throw new IllegalArgumentException( "Unexpected type [" + domainReference + "]" );
	}

	private BasicType resolveArithmeticType(
			BasicType firstType,
			BasicType secondType,
			BinaryArithmeticSqmExpression.Operation operation) {
		if ( operation == BinaryArithmeticSqmExpression.Operation.DIVIDE ) {
			// covered under the note in 6.5.7.1 discussing the unportable
			// "semantics of the SQL division operation"..
			return resolveBasicType( Number.class );
		}
		else if ( firstType != null && Double.class.isAssignableFrom( firstType.getJavaType() ) ) {
			return firstType;
		}
		else if ( secondType != null && Double.class.isAssignableFrom( secondType.getJavaType() ) ) {
			return secondType;
		}
		else if ( firstType != null && Float.class.isAssignableFrom( firstType.getJavaType() ) ) {
			return firstType;
		}
		else if ( secondType != null && Float.class.isAssignableFrom( secondType.getJavaType() ) ) {
			return secondType;
		}
		else if ( firstType != null && BigDecimal.class.isAssignableFrom( firstType.getJavaType() ) ) {
			return firstType;
		}
		else if ( secondType != null && BigDecimal.class.isAssignableFrom( secondType.getJavaType() ) ) {
			return secondType;
		}
		else if ( firstType != null && BigInteger.class.isAssignableFrom( firstType.getJavaType() ) ) {
			return firstType;
		}
		else if ( secondType != null && BigInteger.class.isAssignableFrom( secondType.getJavaType() ) ) {
			return secondType;
		}
		else if ( firstType != null && Long.class.isAssignableFrom( firstType.getJavaType() ) ) {
			return firstType;
		}
		else if ( secondType != null && Long.class.isAssignableFrom( secondType.getJavaType() ) ) {
			return secondType;
		}
		else if ( firstType != null && Integer.class.isAssignableFrom( firstType.getJavaType() ) ) {
			return firstType;
		}
		else if ( secondType != null && Integer.class.isAssignableFrom( secondType.getJavaType() ) ) {
			return secondType;
		}
		else if ( firstType != null && Short.class.isAssignableFrom( firstType.getJavaType() ) ) {
			return resolveBasicType( Integer.class );
		}
		else if ( secondType != null && Short.class.isAssignableFrom( secondType.getJavaType() ) ) {
			return resolveBasicType( Integer.class );
		}
		else {
			return resolveBasicType( Number.class );
		}
	}

	@Override
	public BasicType resolveSumFunctionType(DomainReference argumentType) {
		return resolveSumFunctionType( toBasicType( argumentType ) );
	}

	public BasicType resolveSumFunctionType(BasicType argumentType) {
		if ( argumentType == null ) {
			return resolveBasicType( Number.class );
		}

		if ( Double.class.isAssignableFrom( argumentType.getJavaType() ) ) {
			return argumentType;
		}
		else if ( Float.class.isAssignableFrom( argumentType.getJavaType() ) ) {
			return argumentType;
		}
		else if ( BigDecimal.class.isAssignableFrom( argumentType.getJavaType() ) ) {
			return argumentType;
		}
		else if ( BigInteger.class.isAssignableFrom( argumentType.getJavaType() ) ) {
			return argumentType;
		}
		else if ( Long.class.isAssignableFrom( argumentType.getJavaType() ) ) {
			return argumentType;
		}
		else if ( Integer.class.isAssignableFrom( argumentType.getJavaType() ) ) {
			return argumentType;
		}
		else if ( Short.class.isAssignableFrom( argumentType.getJavaType() ) ) {
			return resolveBasicType( Integer.class );
		}
		else {
			return resolveBasicType( Number.class );
		}
	}

	@Override
	public BasicType resolveCastTargetType(String name) {
		// we assume only casts to basic types are valid, because that is what SQM assumes
		org.hibernate.type.BasicType ormBasicType = (org.hibernate.type.BasicType) sessionFactory.getTypeHelper().heuristicType( name );
		if ( ormBasicType == null ) {
			return null;
		}

		return resolveBasicType( ormBasicType.getReturnedClass() );
	}
}
