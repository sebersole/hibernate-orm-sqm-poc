/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.orm.internal.mapping;

import org.hibernate.sqm.domain.EntityType;
import org.hibernate.sqm.domain.PluralAttribute.ElementClassification;
import org.hibernate.sqm.domain.Type;

/**
 * @author Steve Ebersole
 */
public class PluralAttributeElementEntity implements PluralAttributeElement {
	private final ElementClassification classification;
	private final org.hibernate.type.EntityType type;
	private final EntityType sqmType;

	public PluralAttributeElementEntity(
			ElementClassification classification,
			org.hibernate.type.EntityType type,
			EntityType sqmType) {
		this.classification = classification;
		this.type = type;
		this.sqmType = sqmType;
	}

	@Override
	public ElementClassification getElementClassification() {
		return classification;
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
