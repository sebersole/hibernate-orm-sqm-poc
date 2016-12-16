/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.persister.entity.internal;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.hibernate.persister.common.spi.AbstractSingularAttributeDescriptor;
import org.hibernate.persister.common.spi.AttributeContainer;
import org.hibernate.persister.common.spi.AttributeDescriptor;
import org.hibernate.persister.common.spi.Column;
import org.hibernate.persister.common.spi.SingularAttributeDescriptor;
import org.hibernate.persister.embeddable.EmbeddablePersister;
import org.hibernate.persister.entity.spi.IdentifierDescriptor;
import org.hibernate.sqm.domain.EntityReference;
import org.hibernate.type.CompositeType;

/**
 * @author Steve Ebersole
 */
public class IdentifierCompositeAggregated
		extends AbstractSingularAttributeDescriptor<CompositeType>
		implements IdentifierDescriptor, SingularAttributeDescriptor, AttributeContainer {
	private final EmbeddablePersister embeddablePersister;

	public IdentifierCompositeAggregated(
			AttributeContainer declaringType,
			String attributeName,
			EmbeddablePersister embeddablePersister) {
		super( declaringType, attributeName, embeddablePersister.getOrmType(), false );
		this.embeddablePersister = embeddablePersister;
	}

	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// IdentifierDescriptor

	@Override
	public CompositeType getIdType() {
		return getOrmType();
	}

	@Override
	public boolean hasSingleIdAttribute() {
		return true;
	}

	@Override
	public SingularAttributeDescriptor getIdAttribute() {
		return this;
	}

	@Override
	public List<Column> getColumns() {
		return embeddablePersister.collectColumns();
	}


	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// SingularAttribute

	@Override
	public SingularAttributeClassification getAttributeTypeClassification() {
		return SingularAttributeClassification.EMBEDDED;
	}

	@Override
	public String asLoggableText() {
		return "IdentifierCompositeAggregated(" + embeddablePersister.asLoggableText() + ")";
	}

	@Override
	public List<AttributeDescriptor> getNonIdentifierAttributes() {
		return Collections.emptyList();
	}

	@Override
	public AttributeDescriptor findAttribute(String name) {
		return embeddablePersister.findAttribute( name );
	}

	@Override
	public Optional<EntityReference> toEntityReference() {
		return Optional.empty();
	}
}
