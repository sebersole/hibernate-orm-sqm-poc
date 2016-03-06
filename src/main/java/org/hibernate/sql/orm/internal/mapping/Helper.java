/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.sql.orm.internal.mapping;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.persister.collection.CollectionPersister;
import org.hibernate.persister.entity.AbstractEntityPersister;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.sql.gen.NotYetImplementedException;
import org.hibernate.sql.orm.internal.sqm.model.DomainMetamodelImpl;
import org.hibernate.sqm.domain.ManagedType;
import org.hibernate.sqm.domain.PluralAttribute.CollectionClassification;
import org.hibernate.sqm.domain.SingularAttribute;
import org.hibernate.type.CollectionType;
import org.hibernate.type.CompositeType;
import org.hibernate.type.Type;

/**
 * For now mainly a helper for reflection into stuff not exposed on the entity/collection persister
 * contracts
 *
 * @author Steve Ebersole
 */
public class Helper {
	private final Method subclassTableSpanMethod;
	private final Method subclassPropertyTableNumberMethod;
	private final Method subclassPropertyColumnsMethod;
	private final Method subclassPropertyFormulasMethod;

	/**
	 * Singleton access
	 */
	public static final Helper INSTANCE = new Helper();

	private Helper() {
		try {
			subclassTableSpanMethod = AbstractEntityPersister.class.getDeclaredMethod( "getSubclassTableSpan" );
			subclassTableSpanMethod.setAccessible( true );

			subclassPropertyTableNumberMethod = AbstractEntityPersister.class.getDeclaredMethod( "getSubclassPropertyTableNumber", int.class );
			subclassPropertyTableNumberMethod.setAccessible( true );

			subclassPropertyColumnsMethod = AbstractEntityPersister.class.getDeclaredMethod( "getSubclassPropertyColumnReaderClosure" );
			subclassPropertyColumnsMethod.setAccessible( true );

			subclassPropertyFormulasMethod = AbstractEntityPersister.class.getDeclaredMethod( "getSubclassPropertyFormulaTemplateClosure" );
			subclassPropertyFormulasMethod.setAccessible( true );
		}
		catch (Exception e) {
			throw new HibernateException( "Unable to initialize access to AbstractEntityPersister#getSubclassTableSpan", e );
		}
	}

	public int extractSubclassTableCount(EntityPersister persister) {
		try {
			return (Integer) subclassTableSpanMethod.invoke( persister );
		}
		catch (InvocationTargetException e) {
			throw new HibernateException(
					"Unable to access AbstractEntityPersister#getSubclassTableSpan [" + persister.toString() + "]",
					e.getTargetException()
			);
		}
		catch (Exception e) {
			throw new HibernateException(
					"Unable to access AbstractEntityPersister#getSubclassTableSpan [" + persister.toString() + "]",
					e
			);
		}
	}

	public int getSubclassPropertyTableNumber(EntityPersister persister, int subclassPropertyNumber) {
		try {
			return (Integer) subclassPropertyTableNumberMethod.invoke( persister, subclassPropertyNumber );
		}
		catch (InvocationTargetException e) {
			throw new HibernateException(
					"Unable to access AbstractEntityPersister#getSubclassPropertyTableNumber [" + persister.toString() + "]",
					e.getTargetException()
			);
		}
		catch (Exception e) {
			throw new HibernateException(
					"Unable to access AbstractEntityPersister#getSubclassPropertyTableNumber [" + persister.toString() + "]",
					e
			);
		}
	}

	public String[] getSubclassPropertyColumnExpressions(EntityPersister persister, int subclassPropertyNumber) {
		try {
			final String[][] columnExpressions = (String[][]) subclassPropertyColumnsMethod.invoke( persister );
			return columnExpressions[subclassPropertyNumber];
		}
		catch (InvocationTargetException e) {
			throw new HibernateException(
					"Unable to access AbstractEntityPersister#getSubclassPropertyTableNumber [" + persister.toString() + "]",
					e.getTargetException()
			);
		}
		catch (Exception e) {
			throw new HibernateException(
					"Unable to access AbstractEntityPersister#getSubclassPropertyTableNumber [" + persister.toString() + "]",
					e
			);
		}
	}

	public String[] getSubclassPropertyFormulaExpressions(EntityPersister persister, int subclassPropertyNumber) {
		try {
			final String[][] columnExpressions = (String[][]) subclassPropertyFormulasMethod.invoke( persister );
			return columnExpressions[subclassPropertyNumber];
		}
		catch (InvocationTargetException e) {
			throw new HibernateException(
					"Unable to access AbstractEntityPersister#getSubclassPropertyTableNumber [" + persister.toString() + "]",
					e.getTargetException()
			);
		}
		catch (Exception e) {
			throw new HibernateException(
					"Unable to access AbstractEntityPersister#getSubclassPropertyTableNumber [" + persister.toString() + "]",
					e
			);
		}
	}

	public static SingularAttribute.Classification interpretSingularAttributeClassification(Type attributeType) {
		assert !attributeType.isCollectionType();

		if ( attributeType.isAnyType() ) {
			return SingularAttribute.Classification.ANY;
		}
		else if ( attributeType.isEntityType() ) {
			// todo : we don't really know if this is a many-to-one or one-to-one
			return SingularAttribute.Classification.MANY_TO_ONE;
		}
		else if ( attributeType.isComponentType() ) {
			return SingularAttribute.Classification.EMBEDDED;
		}
		else {
			return SingularAttribute.Classification.BASIC;
		}
	}

	public static CollectionClassification interpretCollectionClassification(CollectionType collectionType) {
		return org.hibernate.sql.orm.internal.sqm.model.Helper.interpretCollectionClassification( collectionType );
	}

	public static Column[] makeValues(
			SessionFactoryImplementor factory,
			AbstractTable containingTable,
			Type type,
			String[] columns,
			String[] formulas) {
		assert formulas == null || columns.length == formulas.length;

		final Column[] values = new Column[columns.length];

		for ( int i = 0; i < columns.length; i++ ) {
			final int jdbcType = type.sqlTypes( factory )[i];

			if ( columns[i] != null ) {
				values[i] = containingTable.makeColumn( columns[i], jdbcType );
			}
			else {
				if ( formulas == null ) {
					throw new IllegalStateException( "Column name was null and no formula information was supplied" );
				}
				values[i] = containingTable.makeFormula( formulas[i], jdbcType );
			}
		}

		return values;
	}

	public AbstractAttributeImpl buildAttribute(
			DatabaseModel databaseModel,
			DomainMetamodelImpl domainMetamodel,
			ManagedType source,
			String propertyName,
			Type propertyType,
			Column[] columns) {
		if ( propertyType.isCollectionType() ) {
			return buildPluralAttribute(
					databaseModel,
					domainMetamodel,
					source,
					propertyName,
					propertyType,
					columns
			);
		}
		else {
			return buildSingularAttribute(
					databaseModel,
					domainMetamodel,
					source,
					propertyName,
					propertyType,
					columns
			);
		}
	}

	public AbstractAttributeImpl buildSingularAttribute(
			DatabaseModel databaseModel,
			DomainMetamodelImpl domainMetamodel,
			ManagedType source,
			String attributeName,
			org.hibernate.type.Type attributeType,
			Column[] columns) {
		final SingularAttribute.Classification classification = interpretSingularAttributeClassification( attributeType );
		final org.hibernate.sqm.domain.Type type;
		if ( classification == SingularAttribute.Classification.ANY ) {
			throw new NotYetImplementedException();
		}
		else if ( classification == SingularAttribute.Classification.EMBEDDED ) {
			return new SingularAttributeEmbedded(
					source,
					attributeName,
					buildEmbeddablePersister(
							databaseModel,
							domainMetamodel,
							source.getTypeName() + '.' + attributeName,
							(CompositeType) attributeType,
							columns
					)
			);
		}
		else if ( classification == SingularAttribute.Classification.BASIC ) {
			return new SingularAttributeBasic(
					source,
					attributeName,
					(org.hibernate.type.BasicType) attributeType,
					domainMetamodel.toSqmType( (org.hibernate.type.BasicType) attributeType ),
					columns
			);
		}
		else {
			return new SingularAttributeEntity(
					source,
					attributeName,
					SingularAttribute.Classification.MANY_TO_ONE,
					(org.hibernate.type.EntityType) attributeType,
					domainMetamodel.toSqmType( (org.hibernate.type.EntityType) attributeType ),
					columns
			);
		}
	}

	public EmbeddablePersister buildEmbeddablePersister(
			DatabaseModel databaseModel,
			DomainMetamodelImpl domainMetamodel,
			String role,
			CompositeType compositeType,
			Column[] columns) {
		return new EmbeddablePersister(
				extractEmbeddableName( compositeType ),
				role,
				compositeType,
				databaseModel,
				domainMetamodel,
				columns
		);
	}

	private static String extractEmbeddableName(org.hibernate.type.Type attributeType) {
		// todo : fixme
		return attributeType.getName();
	}

	public AbstractAttributeImpl buildPluralAttribute(
			DatabaseModel databaseModel,
			DomainMetamodelImpl domainMetamodel,
			ManagedType source,
			String subclassPropertyName,
			org.hibernate.type.Type attributeType,
			Column[] columns) {
		final CollectionType collectionType = (CollectionType) attributeType;
		final CollectionPersister collectionPersister = domainMetamodel.getSessionFactory().getCollectionPersister( collectionType.getRole() );

		return new ImprovedCollectionPersisterImpl(
				databaseModel,
				domainMetamodel,
				source,
				subclassPropertyName,
				collectionPersister,
				columns
		);
	}
}
