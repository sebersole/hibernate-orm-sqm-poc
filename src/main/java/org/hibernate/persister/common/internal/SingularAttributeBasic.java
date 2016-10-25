/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.persister.common.internal;

import java.util.Optional;

import org.hibernate.persister.common.spi.AbstractSingularAttribute;
import org.hibernate.persister.common.spi.Column;
import org.hibernate.persister.common.spi.DomainReferenceImplementor;
import org.hibernate.sqm.domain.EntityReference;
import org.hibernate.type.BasicType;

/**
 * @author Steve Ebersole
 */
public class SingularAttributeBasic extends AbstractSingularAttribute<BasicType> {
	private final Column[] columns;

	public SingularAttributeBasic(
			DomainReferenceImplementor declaringType,
			String name,
			BasicType ormType,
			Column[] columns) {
		super( declaringType, name, ormType );
		this.columns = columns;
	}

	@Override
	public SingularAttributeClassification getAttributeTypeClassification() {
		return SingularAttributeClassification.BASIC;
	}

	public Column[] getColumns() {
		return columns;
	}

	@Override
	public String asLoggableText() {
		return "SingularAttributeBasic(" + getLeftHandSide().asLoggableText() + '.' + getAttributeName() + ')';
	}

	@Override
	public Optional<EntityReference> toEntityReference() {
		return Optional.empty();
	}
}
