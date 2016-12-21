/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.persister.entity.internal;

import java.sql.Types;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.hibernate.persister.common.internal.DerivedColumn;
import org.hibernate.persister.common.spi.AttributeContainer;
import org.hibernate.persister.common.spi.Column;
import org.hibernate.persister.entity.Loadable;
import org.hibernate.persister.entity.spi.DiscriminatorDescriptor;
import org.hibernate.persister.entity.spi.ImprovedEntityPersister;
import org.hibernate.sqm.domain.EntityReference;
import org.hibernate.type.Type;

/**
 * @author Steve Ebersole
 */
public class DiscriminatorDescriptorImpl implements DiscriminatorDescriptor {

	// NOTE : Total hack until EntityPersister gets ImprovedEntityPersister applied to it
	//
	// The problem here is the lack of exposed information about an entity's discriminator column/formula

	private final ImprovedEntityPersister persister;
	private final Column column;

	public DiscriminatorDescriptorImpl(ImprovedEntityPersister persister) {
		this.persister = persister;

		// here is the hack:
		this.column = new DerivedColumn(
				persister.getRootTable(),
				"clazz_",
				( (Loadable) persister.getEntityPersister() ).getDiscriminatorType().sqlTypes( persister.getEntityPersister().getFactory() )[0]
		);
	}

	@Override
	public Type getOrmType() {
		return ( (Loadable) persister ).getDiscriminatorType();
	}

	@Override
	public Optional<EntityReference> toEntityReference() {
		return null;
	}

	@Override
	public String getAttributeName() {
		return "<discriminator>";
	}

	@Override
	public AttributeContainer getAttributeContainer() {
		return persister;
	}

	@Override
	public List<Column> getColumns() {
		return Collections.singletonList( column );
	}

	@Override
	public boolean isNullable() {
		return false;
	}

	@Override
	public SingularAttributeClassification getAttributeTypeClassification() {
		return SingularAttributeClassification.BASIC;
	}

	@Override
	public String asLoggableText() {
		return "<discriminator>";
	}
}
