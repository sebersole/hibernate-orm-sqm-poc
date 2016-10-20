/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.persister.collection.spi;

import org.hibernate.persister.common.spi.Column;
import org.hibernate.persister.common.spi.SqmTypeImplementor;
import org.hibernate.type.Type;

/**
 * @author Steve Ebersole
 */
public class PluralAttributeKey implements SqmTypeImplementor {
	private final Type type;
	private final Column[] foreignKeyColumns;
	// todo : referenced values?

	public PluralAttributeKey(Type type, Column[] foreignKeyValues) {
		this.type = type;
		this.foreignKeyColumns = foreignKeyValues;
	}

	@Override
	public Type getOrmType() {
		return type;
	}

	public Column[] getForeignKeyColumns() {
		return foreignKeyColumns;
	}
}
