/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.persister.common.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.persister.common.spi.Column;
import org.hibernate.persister.common.spi.Table;

/**
 * @author Steve Ebersole
 */
public class UnionSubclassTable implements Table {
	private final String unionQuery;
	private final PhysicalTable physicalTable;
	private final UnionSubclassTable superTable;

	public UnionSubclassTable(
			String unionQuery,
			PhysicalTable physicalTable,
			UnionSubclassTable superTable) {
		this.unionQuery = unionQuery;
		this.physicalTable = physicalTable;
		this.superTable = superTable;
	}

	@Override
	public String getTableExpression() {
		return unionQuery;
	}

	@Override
	public PhysicalColumn makeColumn(String columnName, int jdbcType) {
		if ( superTable != null ) {
			final Column column = superTable.locateColumn( columnName );
			if ( column != null ) {
				// todo : error or simply return the super's column?
				return (PhysicalColumn) column;
//				throw new HibernateException( "Attempt to add column already part of the UnionSubclassTable's super-entity table" );
			}
		}
		return physicalTable.makeColumn( columnName, jdbcType );
	}

	@Override
	public DerivedColumn makeFormula(String formula, int jdbcType) {
		if ( superTable != null ) {
			final Column column = superTable.locateColumn( formula );
			if ( column != null ) {
				// todo : error or simply return the super's column?
				return (DerivedColumn) column;
//				throw new HibernateException( "Attempt to add formula already part of the UnionSubclassTable's super-entity table" );
			}
		}
		return physicalTable.makeFormula( formula, jdbcType );
	}

	@Override
	public Column getColumn(String columnName) {
		final Column column = physicalTable.locateColumn( columnName );
		if ( column != null ) {
			return column;
		}
		throw new MappingException( "Could not locate column : " + columnName );
	}

	@Override
	public Column locateColumn(String columnName) {
		Column column = physicalTable.locateColumn( columnName );
		if ( column != null ) {
			return column;
		}

		if ( superTable != null ) {
			column = physicalTable.locateColumn( columnName );
			if ( column != null ) {
				return column;
			}
		}

		return null;
	}

	@Override
	public Collection<Column> getColumns() {
		final List<Column> columns = new ArrayList<>();
		columns.addAll( physicalTable.getColumns() );
		if ( superTable != null ) {
			columns.addAll( superTable.getColumns() );
		}
		return columns;
	}

	public boolean includes(Table table) {
		return includes( table.getTableExpression() );
	}

	public boolean includes(String tableExpression) {
		if ( tableExpression == null ) {
			throw new IllegalArgumentException( "Passed tableExpression cannot be null" );
		}

		if ( tableExpression.equals( unionQuery ) ) {
			return true;
		}

		if ( tableExpression.equals( physicalTable.getTableExpression() ) ) {
			return true;
		}

		if ( superTable != null ) {
			return superTable.includes( tableExpression );
		}

		return false;
	}
}
