/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.persister.entity.internal;

import java.util.Optional;

import org.hibernate.persister.common.spi.Column;
import org.hibernate.persister.common.spi.DomainReferenceImplementor;
import org.hibernate.persister.common.spi.SingularAttributeImplementor;
import org.hibernate.persister.embeddable.EmbeddablePersister;
import org.hibernate.persister.entity.spi.AttributeReferenceSource;
import org.hibernate.persister.entity.spi.IdentifierDescriptor;
import org.hibernate.sqm.domain.AttributeReference;
import org.hibernate.sqm.domain.DomainReference;
import org.hibernate.sqm.domain.EntityReference;
import org.hibernate.type.CompositeType;
import org.hibernate.type.Type;

/**
 * @author Steve Ebersole
 */
public class IdentifierCompositeAggregated
		implements IdentifierDescriptor, SingularAttributeImplementor, AttributeReferenceSource {
	private final DomainReferenceImplementor declaringType;
	private final String attributeName;
	private final EmbeddablePersister embeddablePersister;

	public IdentifierCompositeAggregated(
			DomainReferenceImplementor declaringType,
			String attributeName,
			EmbeddablePersister embeddablePersister) {
		this.declaringType = declaringType;
		this.attributeName = attributeName;
		this.embeddablePersister = embeddablePersister;
	}

	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// IdentifierDescriptor

	@Override
	public CompositeType getIdType() {
		return embeddablePersister.getOrmType();
	}

	@Override
	public boolean hasSingleIdAttribute() {
		return true;
	}

	@Override
	public SingularAttributeImplementor getIdAttribute() {
		return this;
	}

	@Override
	public Column[] getColumns() {
		return embeddablePersister.collectColumns();
	}


	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// SingularAttribute

	@Override
	public SingularAttributeClassification getAttributeTypeClassification() {
		return SingularAttributeClassification.EMBEDDED;
	}

	@Override
	public Type getOrmType() {
		return getIdType();
	}

	@Override
	public DomainReference getLeftHandSide() {
		return declaringType;
	}

	@Override
	public String getAttributeName() {
		return attributeName;
	}

	@Override
	public String asLoggableText() {
		return "IdentifierCompositeAggregated(" + embeddablePersister.asLoggableText() + ")";
	}

	@Override
	public AttributeReference findAttribute(String name) {
		return embeddablePersister.findAttribute( name );
	}

	@Override
	public Optional<EntityReference> toEntityReference() {
		return Optional.empty();
	}
}
