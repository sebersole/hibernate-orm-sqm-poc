/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.persister.common.internal;

import org.hibernate.persister.common.spi.AbstractSingularAttribute;
import org.hibernate.persister.common.spi.Column;
import org.hibernate.sqm.domain.BasicType;
import org.hibernate.sqm.domain.ManagedType;

/**
 * @author Steve Ebersole
 */
public class SingularAttributeBasic
		extends AbstractSingularAttribute<org.hibernate.type.BasicType, BasicType> {
	private final Column[] columns;

	public SingularAttributeBasic(
			ManagedType declaringType,
			String name,
			org.hibernate.type.BasicType ormType,
			BasicType sqmType,
			Column[] columns) {
		super( declaringType, name, ormType, sqmType );
		this.columns = columns;
	}

	@Override
	public Classification getAttributeTypeClassification() {
		return Classification.BASIC;
	}

	public Column[] getColumns() {
		return columns;
	}

	@Override
	public boolean isId() {
		return false;
	}

	@Override
	public boolean isVersion() {
		return false;
	}

	@Override
	public ManagedType asManagedType() {
		return null;
	}
}
