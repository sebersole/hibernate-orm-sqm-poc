/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.orm.internal.sqm.model;

import org.hibernate.sqm.domain.BasicType;
import org.hibernate.sqm.domain.ManagedType;
import org.hibernate.sqm.domain.PluralAttribute;
import org.hibernate.sqm.domain.Type;

/**
 * @author Steve Ebersole
 */
public class PluralAttributeImpl extends AbstractAttributeImpl implements PluralAttribute {
	private final CollectionClassification collectionClassification;
	private final ElementClassification elementClassification;
	private final BasicType collectionIdType;
	private final Type collectionIndexType;
	private final Type collectionElementType;

	public PluralAttributeImpl(
			ManagedType declaringType,
			String name,
			CollectionClassification collectionClassification,
			ElementClassification elementClassification,
			BasicType collectionIdType,
			Type collectionIndexType,
			Type collectionElementType) {
		super( declaringType, name );
		this.collectionClassification = collectionClassification;
		this.elementClassification = elementClassification;
		this.collectionIdType = collectionIdType;
		this.collectionIndexType = collectionIndexType;
		this.collectionElementType = collectionElementType;
	}

	public CollectionClassification getCollectionClassification() {
		return collectionClassification;
	}

	@Override
	public ElementClassification getElementClassification() {
		return elementClassification;
	}

	@Override
	public BasicType getCollectionIdType() {
		return collectionIdType;
	}

	@Override
	public Type getIndexType() {
		return collectionIndexType;
	}

	@Override
	public Type getElementType() {
		return collectionElementType;
	}

	@Override
	public Type getBoundType() {
		return collectionElementType;
	}

	@Override
	public ManagedType asManagedType() {
		// todo : for now, just let the ClassCastException happen
		return (ManagedType) collectionElementType;
	}
}
