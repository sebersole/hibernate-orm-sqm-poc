/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.persister.common.internal;

import org.hibernate.persister.common.spi.SqmTypeImplementor;
import org.hibernate.sqm.domain.BasicType;

/**
 * @author Steve Ebersole
 */
public class BasicTypeImpl<X> implements BasicType<X>, SqmTypeImplementor {
	private org.hibernate.type.BasicType ormBasicType;

	public BasicTypeImpl(org.hibernate.type.BasicType ormBasicType) {
		this.ormBasicType = ormBasicType;
	}

	@Override
	public org.hibernate.type.BasicType getOrmType() {
		return ormBasicType;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Class<X> getJavaType() {
		return ormBasicType.getReturnedClass();
	}

	@Override
	public String getTypeName() {
		return getJavaType().getName();
	}
}
