/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.orm.internal.mapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.sql.gen.NotYetImplementedException;
import org.hibernate.sql.orm.internal.sqm.model.DomainMetamodelImpl;
import org.hibernate.sqm.domain.Attribute;
import org.hibernate.sqm.domain.EmbeddableType;
import org.hibernate.sqm.domain.ManagedType;
import org.hibernate.sqm.domain.Type;
import org.hibernate.type.CompositeType;

/**
 * @author Steve Ebersole
 */
public class EmbeddablePersister implements EmbeddableType {
	private final String compositeName;
	private final String roleName;
	private final CompositeType ormType;
	private final Column[] allColumns;

	private final Map<String, AbstractAttributeImpl> attributeMap = new HashMap<String, AbstractAttributeImpl>();

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

	public CompositeType getOrmType() {
		return ormType;
	}

	@Override
	public Type getBoundType() {
		return this;
	}

	@Override
	public ManagedType asManagedType() {
		return this;
	}

	@Override
	public ManagedType getSuperType() {
		return null;
	}

	@Override
	public Attribute findAttribute(String name) {
		return attributeMap.get( name );
	}

	@Override
	public Attribute findDeclaredAttribute(String name) {
		return findAttribute( name );
	}

	@Override
	public String getTypeName() {
		return roleName;
	}
}
