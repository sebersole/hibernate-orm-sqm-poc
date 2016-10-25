/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.persister.collection.internal;

import java.util.Optional;

import org.hibernate.persister.common.spi.AbstractPluralAttributeIndex;
import org.hibernate.persister.embeddable.EmbeddablePersister;
import org.hibernate.sqm.domain.DomainReference;
import org.hibernate.sqm.domain.EntityReference;
import org.hibernate.type.CompositeType;

/**
 * @author Steve Ebersole
 */
public class PluralAttributeIndexEmbeddable extends AbstractPluralAttributeIndex<CompositeType> {
	public PluralAttributeIndexEmbeddable(ImprovedCollectionPersisterImpl persister, EmbeddablePersister embeddablePersister) {
		super( persister, embeddablePersister.getOrmType(),embeddablePersister.collectColumns() );
	}

	@Override
	public IndexClassification getClassification() {
		return IndexClassification.EMBEDDABLE;
	}

	@Override
	public DomainReference getType() {
		return this;
	}

	@Override
	public Optional<EntityReference> toEntityReference() {
		return Optional.empty();
	}
}
