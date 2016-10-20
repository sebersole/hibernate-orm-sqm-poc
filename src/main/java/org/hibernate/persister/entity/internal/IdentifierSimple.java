/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.persister.entity.internal;

import org.hibernate.persister.common.spi.Column;
import org.hibernate.persister.common.spi.DomainReferenceImplementor;
import org.hibernate.persister.common.spi.SingularAttributeImplementor;
import org.hibernate.persister.entity.spi.IdentifierDescriptor;
import org.hibernate.sqm.domain.DomainReference;
import org.hibernate.type.BasicType;

/**
 * @author Steve Ebersole
 */
public class IdentifierSimple implements IdentifierDescriptor, SingularAttributeImplementor {
	private final DomainReferenceImplementor declaringType;
	private final String attributeName;
	private final org.hibernate.type.BasicType ormType;
	private final Column[] columns;

	public IdentifierSimple(
			DomainReferenceImplementor declaringType,
			String attributeName,
			org.hibernate.type.BasicType ormType,
			Column[] columns) {
		this.declaringType = declaringType;
		this.attributeName = attributeName;
		this.ormType = ormType;
		this.columns = columns;
	}

	@Override
	public BasicType getIdType() {
		return ormType;
	}

	@Override
	public boolean hasSingleIdAttribute() {
		return true;
	}

	@Override
	public Column[] getColumns() {
		return columns;
	}

	@Override
	public SingularAttributeImplementor getIdAttribute() {
		return this;
	}

	@Override
	public BasicType getOrmType() {
		return getIdType();
	}

	@Override
	public SingularAttributeClassification getAttributeTypeClassification() {
		return SingularAttributeClassification.BASIC;
	}

	@Override
	public DomainReference getLeftHandSide() {
		return declaringType;
	}

	@Override
	public String getAttributeName() {
		return attributeName;
	}

	@Override
	public String asLoggableText() {
		return "IdentifierSimple(" + declaringType.asLoggableText() + ")";
	}
}
