/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.orm.internal.mapping;

import org.hibernate.sqm.domain.BasicType;
import org.hibernate.sqm.domain.PluralAttribute.ElementClassification;

/**
 * @author Steve Ebersole
 */
public class PluralAttributeElementBasic implements PluralAttributeElement {
	private final org.hibernate.type.BasicType type;
	private final BasicType sqmType;

	public PluralAttributeElementBasic(org.hibernate.type.BasicType type, BasicType sqmType) {
		this.type = type;
		this.sqmType = sqmType;
	}

	@Override
	public ElementClassification getElementClassification() {
		return ElementClassification.BASIC;
	}

	@Override
	public BasicType getSqmType() {
		return sqmType;
	}

	@Override
	public org.hibernate.type.BasicType getType() {
		return type;
	}
}
