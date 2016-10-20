/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.persister.common.internal;

import org.hibernate.persister.common.spi.AbstractSingularAttribute;
import org.hibernate.persister.common.spi.Column;
import org.hibernate.persister.common.spi.DomainReferenceImplementor;
import org.hibernate.type.EntityType;

/**
 * @author Steve Ebersole
 */
public class SingularAttributeEntity extends AbstractSingularAttribute<EntityType> {
	private final SingularAttributeClassification classification;
	private final Column[] columns;

	public SingularAttributeEntity(
			DomainReferenceImplementor declaringType,
			String name,
			SingularAttributeClassification classification,
			EntityType ormType,
			Column[] columns) {
		super( declaringType, name, ormType );
		this.classification = classification;
		this.columns = columns;
	}

	@Override
	public SingularAttributeClassification getAttributeTypeClassification() {
		return classification;
	}

	public Column[] getColumns() {
		return columns;
	}

	@Override
	public String asLoggableText() {
		return "SingularAttributeEntity([" + getAttributeTypeClassification().name() + "] " +
				getLeftHandSide().asLoggableText() + '.' + getAttributeName() +
				")";
	}
}
