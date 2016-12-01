/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.ast.from;

import org.hibernate.persister.common.spi.Column;
import org.hibernate.sql.ast.select.SqlSelectable;
import org.hibernate.sql.exec.results.process.internal.SqlSelectionReaderImpl;
import org.hibernate.sql.exec.results.process.spi2.SqlSelectionReader;
import org.hibernate.type.BasicType;

/**
 * Represents a binding of a column (derived or physical) into a SQL statement
 *
 * @author Steve Ebersole
 */
public class ColumnBinding implements SqlSelectable {
	private final Column column;
	private final SqlSelectionReader sqlSelectionReader;
	private final String identificationVariable;

	public ColumnBinding(Column column, BasicType type, TableBinding tableBinding) {
		this.column = column;
		this.sqlSelectionReader = new SqlSelectionReaderImpl( type );
		this.identificationVariable = tableBinding.getIdentificationVariable();
	}

	public ColumnBinding(Column column, int jdbcTypeCode, TableBinding tableBinding) {
		this.column = column;
		this.sqlSelectionReader = new SqlSelectionReaderImpl( jdbcTypeCode );
		this.identificationVariable = tableBinding.getIdentificationVariable();
	}

	public ColumnBinding(Column column, TableBinding tableBinding) {
		this.column = column;
		this.sqlSelectionReader = new SqlSelectionReaderImpl( column.getJdbcType() );
		this.identificationVariable = tableBinding.getIdentificationVariable();
	}

	public Column getColumn() {
		return column;
	}

	public String getIdentificationVariable() {
		return identificationVariable;
	}

	@Override
	public SqlSelectionReader getSqlSelectionReader() {
		return sqlSelectionReader;
	}
}
