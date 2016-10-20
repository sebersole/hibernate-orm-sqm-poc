/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.persister.common.internal;

import org.hibernate.persister.common.spi.AbstractAttributeImpl;
import org.hibernate.persister.common.spi.Column;
import org.hibernate.persister.common.spi.DomainReferenceImplementor;
import org.hibernate.persister.common.spi.SingularAttributeImplementor;
import org.hibernate.persister.embeddable.EmbeddablePersister;
import org.hibernate.type.Type;

/**
 * @author Steve Ebersole
 */
public class SingularAttributeEmbedded
		extends AbstractAttributeImpl
		implements SingularAttributeImplementor {

	private final EmbeddablePersister embeddablePersister;

	public SingularAttributeEmbedded(
			DomainReferenceImplementor declaringType,
			String attributeName,
			EmbeddablePersister embeddablePersister) {
		super( declaringType, attributeName );
		this.embeddablePersister = embeddablePersister;
	}

	public EmbeddablePersister getEmbeddablePersister() {
		return embeddablePersister;
	}

	@Override
	public SingularAttributeClassification getAttributeTypeClassification() {
		return SingularAttributeClassification.EMBEDDED;
	}

	@Override
	public Type getOrmType() {
		return embeddablePersister.getOrmType();
	}

	@Override
	public Column[] getColumns() {
		return embeddablePersister.collectColumns();
	}

	@Override
	public String asLoggableText() {
		return toString();
	}
}
