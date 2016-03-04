/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.orm.internal.mapping;

import org.hibernate.type.Type;

/**
 * @author Steve Ebersole
 */
public class PluralAttributeKey {
	private final Type type;
	private final org.hibernate.sqm.domain.Type sqmType;
	private final Value[] foreignKeyValues;
	// todo : referenced values?

	public PluralAttributeKey(
			Type type,
			org.hibernate.sqm.domain.Type sqmType,
			Value[] foreignKeyValues) {
		this.type = type;
		this.sqmType = sqmType;
		this.foreignKeyValues = foreignKeyValues;
	}

	public Type getType() {
		return type;
	}

	public org.hibernate.sqm.domain.Type getSqmType() {
		return sqmType;
	}

	public Value[] getForeignKeyValues() {
		return foreignKeyValues;
	}
}
