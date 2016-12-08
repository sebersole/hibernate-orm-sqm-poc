/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.persister.common.internal;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.persister.common.spi.AbstractSingularAttributeDescriptor;
import org.hibernate.persister.common.spi.AttributeContainer;
import org.hibernate.persister.common.spi.Column;
import org.hibernate.sqm.domain.EntityReference;
import org.hibernate.type.BasicType;

/**
 * @author Steve Ebersole
 */
public class SingularAttributeBasic extends AbstractSingularAttributeDescriptor<BasicType> {
	private final Column[] columns;

	public SingularAttributeBasic(
			AttributeContainer declaringType,
			String name,
			BasicType ormType,
			Column[] columns) {
		super( declaringType, name, ormType, true );
		this.columns = columns;
	}

	@Override
	public SingularAttributeClassification getAttributeTypeClassification() {
		return SingularAttributeClassification.BASIC;
	}

	@Override
	public int getColumnCount(boolean shallow, SessionFactoryImplementor factory) {
		return columns.length;
	}

	@Override
	public List<Column> getColumns(boolean shallow, SessionFactoryImplementor factory) {
		return Arrays.asList( columns );
	}

	public Column[] getColumns() {
		return columns;
	}

	@Override
	public String asLoggableText() {
		return "SingularAttributeBasic(" + getLeftHandSide().asLoggableText() + '.' + getAttributeName() + ')';
	}

	@Override
	public Optional<EntityReference> toEntityReference() {
		return Optional.empty();
	}
}
