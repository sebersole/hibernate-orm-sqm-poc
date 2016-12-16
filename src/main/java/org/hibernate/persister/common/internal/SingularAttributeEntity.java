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
import org.hibernate.persister.entity.spi.ImprovedEntityPersister;
import org.hibernate.sqm.domain.EntityReference;
import org.hibernate.type.EntityType;

/**
 * @author Steve Ebersole
 */
public class SingularAttributeEntity extends AbstractSingularAttributeDescriptor<EntityType> {
	private final SingularAttributeClassification classification;
	private final ImprovedEntityPersister entityPersister;
	private final List<Column> columns;

	public SingularAttributeEntity(
			AttributeContainer declaringType,
			String name,
			SingularAttributeClassification classification,
			EntityType ormType,
			ImprovedEntityPersister entityPersister,
			List<Column> columns) {
		super( declaringType, name, ormType, true );
		this.classification = classification;
		this.entityPersister = entityPersister;
		this.columns = columns;
	}

	public ImprovedEntityPersister getEntityPersister() {
		return entityPersister;
	}

	@Override
	public SingularAttributeClassification getAttributeTypeClassification() {
		return classification;
	}

	public List<Column> getColumns() {
		return columns;
	}

	@Override
	public String asLoggableText() {
		return "SingularAttributeEntity([" + getAttributeTypeClassification().name() + "] " +
				getLeftHandSide().asLoggableText() + '.' + getAttributeName() +
				")";
	}

	@Override
	public String toString() {
		return asLoggableText();
	}

	@Override
	public Optional<EntityReference> toEntityReference() {
		return Optional.of( entityPersister );
	}

	public String getEntityName() {
		return entityPersister.getEntityName();
	}
}
