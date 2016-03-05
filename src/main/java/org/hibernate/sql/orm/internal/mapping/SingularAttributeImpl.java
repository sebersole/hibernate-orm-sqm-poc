/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.orm.internal.mapping;

import org.hibernate.sqm.domain.ManagedType;
import org.hibernate.sqm.domain.SingularAttribute;
import org.hibernate.sqm.domain.Type;

/**
 * @author Steve Ebersole
 */
public class SingularAttributeImpl extends AbstractAttributeImpl implements SingularAttribute {
	private final Classification classification;
	private final Type sqmType;

	public SingularAttributeImpl(
			ManagedType declaringType,
			String name,
			Classification classification,
			Type sqmType) {
		super( declaringType, name );
		this.classification = classification;
		this.sqmType = sqmType;
	}

	@Override
	public Classification getAttributeTypeClassification() {
		return classification;
	}

	@Override
	public Type getType() {
		return sqmType;
	}

	@Override
	public boolean isId() {
		return false;
	}

	@Override
	public boolean isVersion() {
		return false;
	}

	@Override
	public Type getBoundType() {
		return getType();
	}

	@Override
	public ManagedType asManagedType() {
		if ( getType() instanceof ManagedType ) {
			return (ManagedType) getType();
		}

		return null;
	}
}
