/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.orm.internal.mapping;

import org.hibernate.sqm.domain.BasicType;
import org.hibernate.sqm.domain.ManagedType;
import org.hibernate.sqm.domain.SingularAttribute;
import org.hibernate.sqm.domain.Type;

/**
 * @author Steve Ebersole
 */
public class SingularAttributeBasic extends AbstractAttributeImpl implements SingularAttribute {
	private final org.hibernate.type.Type type;
	private final BasicType sqmType;
	private final Column[] columns;

	public SingularAttributeBasic(
			ManagedType declaringType,
			String name,
			org.hibernate.type.Type type,
			BasicType sqmType,
			Column[] columns) {
		super( declaringType, name );
		this.type = type;
		this.sqmType = sqmType;
		this.columns = columns;
	}

	@Override
	public Classification getAttributeTypeClassification() {
		return Classification.BASIC;
	}

	public Column[] getColumns() {
		return columns;
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
		return null;
	}
}
