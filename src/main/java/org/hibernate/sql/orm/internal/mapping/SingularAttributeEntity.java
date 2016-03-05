/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.orm.internal.mapping;

import org.hibernate.sqm.domain.EntityType;
import org.hibernate.sqm.domain.ManagedType;
import org.hibernate.sqm.domain.SingularAttribute;
import org.hibernate.sqm.domain.Type;

/**
 * @author Steve Ebersole
 */
public class SingularAttributeEntity extends AbstractAttributeImpl implements SingularAttribute {
	private final Classification classification;
	private final org.hibernate.type.EntityType type;
	private final EntityType sqmType;
	private final Column[] columns;

	public SingularAttributeEntity(
			ManagedType declaringType,
			String name,
			Classification classification,
			org.hibernate.type.EntityType type,
			EntityType sqmType,
			Column[] columns) {
		super( declaringType, name );
		this.classification = classification;
		this.type = type;
		this.sqmType = sqmType;
		this.columns = columns;
	}

	@Override
	public Classification getAttributeTypeClassification() {
		return classification;
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
		return sqmType;
	}

	@Override
	public ManagedType asManagedType() {
		return sqmType;
	}
}
