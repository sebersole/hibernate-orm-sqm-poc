/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.persister.common.internal;

import java.util.List;
import java.util.Optional;

import org.hibernate.persister.common.spi.AbstractSingularAttributeDescriptor;
import org.hibernate.persister.common.spi.AttributeContainer;
import org.hibernate.persister.common.spi.Column;
import org.hibernate.persister.common.spi.SingularAttributeDescriptor;
import org.hibernate.persister.embeddable.EmbeddablePersister;
import org.hibernate.sqm.domain.EntityReference;
import org.hibernate.type.CompositeType;

/**
 * @author Steve Ebersole
 */
public class SingularAttributeEmbedded
		extends AbstractSingularAttributeDescriptor<CompositeType>
		implements SingularAttributeDescriptor {

	private final EmbeddablePersister embeddablePersister;

	public SingularAttributeEmbedded(
			AttributeContainer declaringType,
			String attributeName,
			EmbeddablePersister embeddablePersister) {
		super( declaringType, attributeName, embeddablePersister.getOrmType(), true );
		this.embeddablePersister = embeddablePersister;
	}

	public EmbeddablePersister getEmbeddablePersister() {
		return embeddablePersister;
	}

	@Override
	public SingularAttributeClassification getAttributeTypeClassification() {
		return SingularAttributeClassification.EMBEDDED;
	}

	@Override
	public List<Column> getColumns() {
		return embeddablePersister.collectColumns();
	}

	@Override
	public String asLoggableText() {
		return toString();
	}

	@Override
	public Optional<EntityReference> toEntityReference() {
		return Optional.empty();
	}
}
