/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.persister.collection.internal;

import org.hibernate.persister.collection.spi.PluralAttributeElement;
import org.hibernate.persister.embeddable.EmbeddablePersister;
import org.hibernate.sqm.domain.DomainReference;
import org.hibernate.sqm.domain.PluralAttributeReference.ElementReference.ElementClassification;
import org.hibernate.type.CompositeType;

/**
 * @author Steve Ebersole
 */
public class PluralAttributeElementEmbeddable implements PluralAttributeElement<CompositeType> {
	private final ImprovedCollectionPersisterImpl collectionPersister;
	private final EmbeddablePersister embeddablePersister;

	public PluralAttributeElementEmbeddable(ImprovedCollectionPersisterImpl collectionPersister, EmbeddablePersister embeddablePersister) {
		this.collectionPersister = collectionPersister;
		this.embeddablePersister = embeddablePersister;
	}

	public EmbeddablePersister getEmbeddablePersister() {
		return embeddablePersister;
	}

	@Override
	public ElementClassification getClassification() {
		return ElementClassification.EMBEDDABLE;
	}

	@Override
	public CompositeType getOrmType() {
		return embeddablePersister.getOrmType();
	}

	@Override
	public DomainReference getType() {
		return this;
	}

	@Override
	public String asLoggableText() {
		return "PluralAttributeElement(" + collectionPersister.getPersister().getRole() + " [" + getOrmType().getName() + "])" ;
	}
}
