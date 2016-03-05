/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.orm.internal.mapping;

import org.hibernate.sqm.domain.EmbeddableType;
import org.hibernate.sqm.domain.ManagedType;
import org.hibernate.type.CompositeType;

/**
 * @author Steve Ebersole
 */
public class SingularAttributeEmbedded
		extends AbstractSingularAttribute<CompositeType, EmbeddableType> {

	public SingularAttributeEmbedded(
			ManagedType declaringType,
			String attributeName,
			EmbeddablePersister embeddablePersister) {
		super( declaringType, attributeName, embeddablePersister.getOrmType(), embeddablePersister );
	}

	@Override
	public Classification getAttributeTypeClassification() {
		return Classification.EMBEDDED;
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
	public EmbeddablePersister asManagedType() {
		return (EmbeddablePersister) getBoundType();
	}
}
