/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.hql.parser.antlr;

import java.util.Arrays;
import java.util.Collection;

import org.hibernate.hql.parser.model.BasicTypeDescriptor;
import org.hibernate.hql.parser.model.CollectionTypeDescriptor;
import org.hibernate.hql.parser.model.CompositeTypeDescriptor;
import org.hibernate.hql.parser.model.EntityTypeDescriptor;
import org.hibernate.hql.parser.model.ModelMetadata;
import org.hibernate.hql.parser.model.PolymorphicEntityTypeDescriptor;
import org.hibernate.hql.parser.model.TypeDescriptor;

/**
 * Simple implementation of ModelMetadata used for testing when there is no domain model.
 * Uses a simple naming based strategy for determining what to return.
 *
 * @author Steve Ebersole
 */
public class ModelMetadataTestingImpl implements ModelMetadata {
	@Override
	public EntityTypeDescriptor resolveEntityReference(String reference) {
		if ( reference.startsWith( "Polymorphic" ) ) {
			final String baseName = reference.substring( 11 );
			final EntityTypeDescriptorImpl first = new EntityTypeDescriptorImpl( baseName + "1" );
			final EntityTypeDescriptorImpl second = new EntityTypeDescriptorImpl( baseName + "2" );
			return new PolymorphicEntityTypeDescriptorImpl(
					reference,
					first,
					second
			);
		}
		else {
			return new EntityTypeDescriptorImpl( reference );
		}
	}

	public static abstract class AbstractTypeDescriptorImpl
			implements TypeDescriptor {
		@Override
		public TypeDescriptor getAttributeType(String attributeName) {
			if ( attributeName.startsWith( "basic" ) ) {
				return buildBasicAttribute( attributeName );
			}
			else if ( attributeName.startsWith( "composite" ) ) {
				return buildCompositeAttribute( attributeName );
			}
			else if ( attributeName.startsWith( "collection" ) ) {
				return buildCollectionAttribute( attributeName );
			}
			else if ( attributeName.startsWith( "basicCollection" ) ) {
				return buildBasicCollectionAttribute( attributeName );
			}
			else if ( attributeName.startsWith( "map" ) ) {
				return buildMapAttribute( attributeName );
			}
			else if ( attributeName.startsWith( "basicMap" ) ) {
				return buildBasicMapAttribute( attributeName );
			}

			return null;
		}

		protected BasicTypeDescriptor buildBasicAttribute(String attributeName) {
			return new BasicTypeDescriptorImpl();
		}

		protected CompositeTypeDescriptor buildCompositeAttribute(String attributeName) {
			return new CompositeTypeDescriptorImpl();
		}

		protected CollectionTypeDescriptor buildCollectionAttribute(String attributeName) {
			return new CollectionTypeDescriptorImpl( new EntityTypeDescriptorImpl( attributeName ) );
		}

		protected CollectionTypeDescriptor buildBasicCollectionAttribute(String attributeName) {
			return new CollectionTypeDescriptorImpl( new BasicTypeDescriptorImpl() );
		}

		protected CollectionTypeDescriptor buildMapAttribute(String attributeName) {
			return new CollectionTypeDescriptorImpl(
					new BasicTypeDescriptorImpl(),
					new EntityTypeDescriptorImpl( attributeName )
			);
		}

		protected CollectionTypeDescriptor buildBasicMapAttribute(String attributeName) {
			return new CollectionTypeDescriptorImpl(
					new BasicTypeDescriptorImpl(),
					new BasicTypeDescriptorImpl()
			);
		}
	}

	public static class EntityTypeDescriptorImpl
			extends AbstractTypeDescriptorImpl
			implements EntityTypeDescriptor {
		private final String entityName;

		public EntityTypeDescriptorImpl(String entityName) {
			if ( entityName.contains( "." ) ) {
				this.entityName = entityName;
			}
			else {
				this.entityName = "com.acme." + entityName;
			}
		}

		@Override
		public String getEntityName() {
			return entityName;
		}
	}

	private class PolymorphicEntityTypeDescriptorImpl
			extends AbstractTypeDescriptorImpl
			implements PolymorphicEntityTypeDescriptor {
		private final String entityName;
		private final Collection<EntityTypeDescriptor> implementors;

		public PolymorphicEntityTypeDescriptorImpl(
				String entityName,
				EntityTypeDescriptor... implementors) {
			if ( entityName.contains( "." ) ) {
				this.entityName = entityName;
			}
			else {
				this.entityName = "com.acme." + entityName;
			}

			this.implementors = Arrays.asList( implementors );
		}

		@Override
		public String getEntityName() {
			return entityName;
		}

		@Override
		public Collection<EntityTypeDescriptor> getImplementors() {
			return implementors;
		}

		@Override
		public TypeDescriptor getAttributeType(String attributeName) {
			TypeDescriptor typeDescriptor = null;
			for ( EntityTypeDescriptor implementor : implementors ) {
				typeDescriptor = implementor.getAttributeType( attributeName );
				if ( typeDescriptor == null ) {
					return null;
				}
			}
			return typeDescriptor;
		}
	}

	public static class BasicTypeDescriptorImpl
			extends AbstractTypeDescriptorImpl
			implements BasicTypeDescriptor {
		@Override
		public TypeDescriptor getAttributeType(String attributeName) {
			return null;
		}
	}

	public static class CompositeTypeDescriptorImpl
			extends AbstractTypeDescriptorImpl
			implements CompositeTypeDescriptor {
	}

	public static class CollectionTypeDescriptorImpl
			extends AbstractTypeDescriptorImpl
			implements CollectionTypeDescriptor {
		private final TypeDescriptor indexType;
		private final TypeDescriptor elementType;

		public CollectionTypeDescriptorImpl(TypeDescriptor elementType) {
			this( null, elementType );
		}

		public CollectionTypeDescriptorImpl(TypeDescriptor indexType, TypeDescriptor elementType) {
			this.indexType = indexType;
			this.elementType = elementType;
		}

		@Override
		public TypeDescriptor getAttributeType(String attributeName) {
			return null;
		}

		@Override
		public TypeDescriptor getIndexTypeDescriptor() {
			return indexType;
		}

		@Override
		public TypeDescriptor getElementTypeDescriptor() {
			return elementType;
		}
	}
}
