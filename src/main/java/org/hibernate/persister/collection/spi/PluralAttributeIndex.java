/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.persister.collection.spi;

import org.hibernate.persister.common.spi.Column;
import org.hibernate.persister.common.spi.DomainReferenceImplementor;
import org.hibernate.persister.common.spi.SqmTypeImplementor;
import org.hibernate.sqm.domain.PluralAttributeReference.IndexReference;
import org.hibernate.type.Type;

/**
 * @author Steve Ebersole
 */
public interface PluralAttributeIndex<O extends Type> extends SqmTypeImplementor, IndexReference, DomainReferenceImplementor {
	@Override
	O getOrmType();

	Column[] getColumns();
}
