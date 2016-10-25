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
public class IdentifierCompositeNonAggregated
		implements IdentifierDescriptor, SingularAttributeImplementor, AttributeReferenceSource {
	// todo : IdClass handling eventually

	private final DomainReferenceImplementor declaringType;
	private final EmbeddablePersister embeddablePersister;

	public IdentifierCompositeNonAggregated(DomainReferenceImplementor declaringType, EmbeddablePersister embeddablePersister) {
		this.declaringType = declaringType;
		this.embeddablePersister = embeddablePersister;
	}

	@Override
	public Column[] getColumns() {
		return embeddablePersister.collectColumns();
	}

	@Override
	public CompositeType getIdType() {
		return embeddablePersister.getOrmType();
	}

	@Override
	public boolean hasSingleIdAttribute() {
		return false;
	}

	@Override
	public SingularAttributeImplementor getIdAttribute() {
		return this;
	}


	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// SingularAttributeImplementor

	@Override
	public Type getOrmType() {
		return embeddablePersister.getOrmType();
	}

	@Override
	public SingularAttributeClassification getAttributeTypeClassification() {
		return SingularAttributeClassification.EMBEDDED;
	}

	@Override
	public DomainReference getLeftHandSide() {
		return declaringType;
	}

	@Override
	public String getAttributeName() {
		return "<id>";
	}

	@Override
	public String asLoggableText() {
		return "IdentifierCompositeNonAggregated(" + declaringType.asLoggableText() + ")";
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
