/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.persister.common.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.persister.common.spi.AbstractSingularAttributeDescriptor;
import org.hibernate.persister.common.spi.AttributeContainer;
import org.hibernate.persister.common.spi.Column;
import org.hibernate.persister.common.spi.SingularAttributeDescriptor;
import org.hibernate.persister.entity.spi.ImprovedEntityPersister;
import org.hibernate.sqm.domain.AttributeReference;
import org.hibernate.sqm.domain.EntityReference;
import org.hibernate.sqm.domain.PluralAttributeReference;
import org.hibernate.type.EntityType;

/**
 * @author Steve Ebersole
 */
public class SingularAttributeEntity extends AbstractSingularAttributeDescriptor<EntityType> {
	private final SingularAttributeClassification classification;
	private final ImprovedEntityPersister entityPersister;
	private final Column[] columns;

	public SingularAttributeEntity(
			AttributeContainer declaringType,
			String name,
			SingularAttributeClassification classification,
			EntityType ormType,
			ImprovedEntityPersister entityPersister,
			Column[] columns) {
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

	public Column[] getColumns() {
		return columns;
	}

	@Override
	protected Column[] collectColumns(boolean shallow) {
		if ( shallow ) {
			return getColumns();
		}

		List<Column> columnList = new ArrayList<>(  );
		for ( AttributeReference attributeReference : entityPersister.getNonIdentifierAttributes() ) {
			if ( attributeReference instanceof PluralAttributeReference ) {
				continue;
			}

			final SingularAttributeDescriptor attrRef = (SingularAttributeDescriptor) attributeReference;
			Collections.addAll( columnList, attrRef.getColumns() );
		}
		return columnList.toArray( new Column[ columnList.size() ] );
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

	@Override
	public int getColumnCount(boolean shallow, SessionFactoryImplementor factory) {
		// todo : plus the FK column(s)?
		return entityPersister.getColumnCount( shallow, factory );
	}

	@Override
	public List<Column> getColumns(boolean shallow, SessionFactoryImplementor factory) {
		// todo : add the FK column(s)?
		return entityPersister.getColumns( shallow, factory );
	}

	public String getEntityName() {
		return entityPersister.getEntityName();
	}
}
