/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.persister.embeddable;

import java.util.HashMap;
import java.util.Map;

import org.hibernate.persister.common.internal.DatabaseModel;
import org.hibernate.persister.common.internal.DomainMetamodelImpl;
import org.hibernate.persister.common.internal.Helper;
import org.hibernate.persister.common.spi.AbstractAttributeImpl;
import org.hibernate.persister.common.spi.AttributeImplementor;
import org.hibernate.persister.common.spi.Column;
import org.hibernate.persister.common.spi.DomainReferenceImplementor;
import org.hibernate.persister.common.spi.SqmTypeImplementor;
import org.hibernate.persister.entity.spi.AttributeReferenceSource;
import org.hibernate.type.CompositeType;

/**
 * @author Steve Ebersole
 */
public class EmbeddablePersister implements SqmTypeImplementor, DomainReferenceImplementor, AttributeReferenceSource {
	private final String compositeName;
	private final String roleName;
	private final CompositeType ormType;
	private final Column[] allColumns;

	private final Map<String, AbstractAttributeImpl> attributeMap = new HashMap<>();

	public EmbeddablePersister(
			String compositeName,
			String roleName,
			CompositeType ormType,
			DatabaseModel databaseModel,
			DomainMetamodelImpl domainMetamodel,
			Column[] allColumns) {
		this.compositeName = compositeName;
		this.roleName = roleName;
		this.ormType = ormType;
		this.allColumns = allColumns;

		assert ormType.getPropertyNames().length == ormType.getSubtypes().length;

		int columnSpanStart = 0, columnSpanEnd;

		for ( int i = 0; i < ormType.getPropertyNames().length; i++ ) {
			final String propertyName = ormType.getPropertyNames()[i];
			final org.hibernate.type.Type propertyType = ormType.getSubtypes()[i];

			final int columnSpan = propertyType.getColumnSpan( domainMetamodel.getSessionFactory() );
			final Column[] columns = new Column[columnSpan];
			columnSpanEnd = columnSpanStart + columnSpan;
			System.arraycopy( allColumns,  columnSpanStart, columns, 0, columnSpan );

			final AbstractAttributeImpl attribute = Helper.INSTANCE.buildAttribute(
					databaseModel,
					domainMetamodel,
					this,
					propertyName,
					propertyType,
					columns
			);
			attributeMap.put( propertyName, attribute );

			columnSpanStart = columnSpanEnd;
		}
	}

	public Column[] collectColumns() {
		return allColumns;
	}

	@Override
	public AttributeImplementor findAttribute(String name) {
		return attributeMap.get( name );
	}

	@Override
	public CompositeType getOrmType() {
		return ormType;
	}

	@Override
	public String asLoggableText() {
		return "EmdeddablePersister(" + roleName + " [" + compositeName + "])";
	}
}
