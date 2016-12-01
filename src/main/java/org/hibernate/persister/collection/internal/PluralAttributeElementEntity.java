/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.persister.collection.internal;

import java.util.List;
import java.util.Optional;

import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.persister.collection.spi.PluralAttributeElement;
import org.hibernate.persister.common.spi.Column;
import org.hibernate.persister.entity.spi.ImprovedEntityPersister;
import org.hibernate.sqm.domain.DomainReference;
import org.hibernate.sqm.domain.EntityReference;
import org.hibernate.type.EntityType;

/**
 * @author Steve Ebersole
 */
public class PluralAttributeElementEntity implements PluralAttributeElement<EntityType> {
	private final ImprovedCollectionPersisterImpl collectionPersister;
	private final ImprovedEntityPersister elementPersister;
	private final ElementClassification classification;
	private final EntityType type;
	private final Column[] columns;

	public PluralAttributeElementEntity(
			ImprovedCollectionPersisterImpl collectionPersister,
			ImprovedEntityPersister elementPersister,
			ElementClassification classification,
			EntityType type,
			Column[] columns) {
		this.collectionPersister = collectionPersister;
		this.elementPersister = elementPersister;
		this.classification = classification;
		this.type = type;
		this.columns = columns;
	}

	public ImprovedEntityPersister getElementPersister() {
		return elementPersister;
	}

	@Override
	public ElementClassification getClassification() {
		return classification;
	}

	@Override
	public DomainReference getType() {
		return this;
	}

	@Override
	public EntityType getOrmType() {
		return type;
	}

	public Column[] getColumns() {
		return columns;
	}

	@Override
	public String asLoggableText() {
		return "PluralAttributeElement(" + collectionPersister.getPersister().getRole() + " [" + getOrmType().getName() + "])" ;
	}

	@Override
	public Optional<EntityReference> toEntityReference() {
		return Optional.of( elementPersister );
	}

	@Override
	public int getColumnCount(boolean shallow, SessionFactoryImplementor factory) {
		// todo : plus the number of columns in the FK?
		return elementPersister.getColumnCount( shallow, factory );
	}

	@Override
	public List<Column> getColumns(boolean shallow, SessionFactoryImplementor factory) {
		// todo : add the FK column(s)?
		return elementPersister.getColumns( shallow, factory );
	}
}
