/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.orm.internal.mapping;

import org.hibernate.sqm.domain.AnyType;
import org.hibernate.sqm.domain.PluralAttribute;
import org.hibernate.sqm.domain.Type;

/**
 * @author Steve Ebersole
 */
public class PluralAttributeElementAny implements PluralAttributeElement {
	private final org.hibernate.type.AnyType type;
	private final AnyType sqmType;

	public PluralAttributeElementAny(org.hibernate.type.AnyType type, AnyType sqmType) {
		this.type = type;
		this.sqmType = sqmType;
	}

	@Override
	public PluralAttribute.ElementClassification getElementClassification() {
		return PluralAttribute.ElementClassification.ANY;
	}

	@Override
	public Type getSqmType() {
		return sqmType;
	}

	@Override
	public org.hibernate.type.Type getType() {
		return type;
	}
}
