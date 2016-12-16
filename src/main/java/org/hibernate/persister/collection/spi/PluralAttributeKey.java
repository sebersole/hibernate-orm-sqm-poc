/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.persister.collection.spi;

import java.util.List;

import org.hibernate.persister.common.spi.Column;
import org.hibernate.persister.common.spi.OrmTypeExporter;
import org.hibernate.type.Type;

/**
 * @author Steve Ebersole
 */
public class PluralAttributeKey implements OrmTypeExporter {
	private final Type type;
	private final List<Column> foreignKeyColumns;
	// todo : referenced values?

	public PluralAttributeKey(Type type, List<Column> foreignKeyValues) {
		this.type = type;
		this.foreignKeyColumns = foreignKeyValues;
	}

	@Override
	public Type getOrmType() {
		return type;
	}

	public List<Column> getForeignKeyColumns() {
		return foreignKeyColumns;
	}
}
