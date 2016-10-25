/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.persister.common.spi;

import org.hibernate.persister.collection.internal.ImprovedCollectionPersisterImpl;
import org.hibernate.persister.collection.spi.PluralAttributeIndex;
import org.hibernate.type.Type;

/**
 * @author Steve Ebersole
 */
public abstract class AbstractPluralAttributeIndex<O extends Type> implements PluralAttributeIndex {
	private final ImprovedCollectionPersisterImpl persister;
	private final O ormType;
	private final Column[] columns;

	public AbstractPluralAttributeIndex(ImprovedCollectionPersisterImpl persister, O ormType, Column[] columns) {
		this.persister = persister;
		this.ormType = ormType;
		this.columns = columns;
	}

	public ImprovedCollectionPersisterImpl getPersister() {
		return persister;
	}

	@Override
	public O getOrmType() {
		return ormType;
	}

	@Override
	public Column[] getColumns() {
		return columns;
	}

	@Override
	public String asLoggableText() {
		return "PluralAttributeIndex(" + persister.getPersister().getRole() + " [" + getOrmType().getName() + "])";
	}
}