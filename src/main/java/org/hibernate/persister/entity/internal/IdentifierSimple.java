/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.persister.entity.internal;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.persister.common.spi.AbstractSingularAttributeDescriptor;
import org.hibernate.persister.common.spi.AttributeContainer;
import org.hibernate.persister.common.spi.Column;
import org.hibernate.persister.common.spi.SingularAttributeDescriptor;
import org.hibernate.persister.entity.spi.IdentifierDescriptor;
import org.hibernate.sqm.domain.EntityReference;
import org.hibernate.type.BasicType;

/**
 * @author Steve Ebersole
 */
public class IdentifierSimple
		extends AbstractSingularAttributeDescriptor<BasicType>
		implements IdentifierDescriptor, SingularAttributeDescriptor {
	private final Column[] columns;

	public IdentifierSimple(
			AttributeContainer declaringType,
			String attributeName,
			BasicType ormType,
			Column[] columns) {
		super( declaringType, attributeName, ormType, false );
		this.columns = columns;
	}

	@Override
	public BasicType getIdType() {
		return getOrmType();
	}

	@Override
	public boolean hasSingleIdAttribute() {
		return true;
	}

	@Override
	public Column[] getColumns() {
		return columns;
	}

	@Override
	public SingularAttributeDescriptor getIdAttribute() {
		return this;
	}

	@Override
	public SingularAttributeClassification getAttributeTypeClassification() {
		return SingularAttributeClassification.BASIC;
	}

	@Override
	public String asLoggableText() {
		return "IdentifierSimple(" + getLeftHandSide().asLoggableText() + ")";
	}

	@Override
	public Optional<EntityReference> toEntityReference() {
		return Optional.empty();
	}

	@Override
	public int getColumnCount(boolean shallow, SessionFactoryImplementor factory) {
		return columns.length;
	}

	@Override
	public List<Column> getColumns(boolean shallow, SessionFactoryImplementor factory) {
		return Arrays.asList( columns );
	}
}
