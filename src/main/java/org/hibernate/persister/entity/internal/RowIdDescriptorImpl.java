/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.persister.entity.internal;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.hibernate.persister.common.internal.PhysicalColumn;
import org.hibernate.persister.common.spi.AttributeContainer;
import org.hibernate.persister.common.spi.Column;
import org.hibernate.persister.entity.spi.ImprovedEntityPersister;
import org.hibernate.persister.entity.spi.RowIdDescriptor;
import org.hibernate.sqm.domain.EntityReference;
import org.hibernate.type.Type;

/**
 * @author Steve Ebersole
 */
public class RowIdDescriptorImpl implements RowIdDescriptor {
	private final ImprovedEntityPersister persister;
	// todo : really need to expose AbstractEntityPersister.rowIdName for this to work.
	//		for now we will just always assume a selection name of "ROW_ID"
	private final PhysicalColumn column;

	public RowIdDescriptorImpl(ImprovedEntityPersister persister) {
		this.persister = persister;
		column = new PhysicalColumn(
				persister.getRootTable(),
				"ROW_ID",
				Integer.MAX_VALUE
		);

	}

	@Override
	public Type getOrmType() {
		return persister.getOrmType();
	}

	@Override
	public Optional<EntityReference> toEntityReference() {
		return null;
	}

	@Override
	public String getAttributeName() {
		return "<row_id>";
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
		return "ROW_ID";
	}
}
