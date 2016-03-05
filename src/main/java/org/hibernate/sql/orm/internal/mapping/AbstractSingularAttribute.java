/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.orm.internal.mapping;

import org.hibernate.sqm.domain.ManagedType;
import org.hibernate.type.Type;

/**
 * @author Steve Ebersole
 */
public abstract class AbstractSingularAttribute<O extends org.hibernate.type.Type, S extends org.hibernate.sqm.domain.Type>
		extends AbstractAttributeImpl
		implements SingularAttributeImplementor {
	private final O ormType;
	private final S sqmType;

	public AbstractSingularAttribute(
			ManagedType declaringType,
			String name,
			O ormType,
			S sqmType) {
		super( declaringType, name );
		this.ormType = ormType;
		this.sqmType = sqmType;
	}

	@Override
	public O getOrmType() {
		return ormType;
	}

	@Override
	public S getType() {
		return sqmType;
	}

	@Override
	public S getBoundType() {
		return getType();
	}
}
