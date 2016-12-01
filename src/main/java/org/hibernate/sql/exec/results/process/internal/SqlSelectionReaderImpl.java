/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.exec.results.process.internal;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.hibernate.sql.convert.spi.NotYetImplementedException;
import org.hibernate.sql.exec.internal.RecommendedJdbcTypeMappings;
import org.hibernate.sql.exec.results.process.spi.ResultSetProcessingOptions;
import org.hibernate.sql.exec.results.process.spi.RowProcessingState;
import org.hibernate.sql.exec.results.process.spi2.SqlSelectionReader;
import org.hibernate.type.BasicType;
import org.hibernate.type.descriptor.java.JavaTypeDescriptor;
import org.hibernate.type.descriptor.java.JavaTypeDescriptorRegistry;

/**
 * @author Steve Ebersole
 */
public class SqlSelectionReaderImpl implements SqlSelectionReader {
	private final Reader reader;

	public SqlSelectionReaderImpl(int jdbcTypeCode) {
		reader = new JdbcTypeCodeReaderImpl( jdbcTypeCode );
	}

	public SqlSelectionReaderImpl(BasicType basicType) {
		reader = new BasicTypeReaderAdapterImpl( basicType );
	}

	public SqlSelectionReaderImpl(BasicType basicType, int jdbcTypeCode) {
		if ( basicType != null ) {
			reader = new BasicTypeReaderAdapterImpl( basicType );
		}
		else {
			reader = new JdbcTypeCodeReaderImpl( jdbcTypeCode );
		}
	}

	@Override
	public Object read(
			RowProcessingState rowProcessingState, ResultSetProcessingOptions options, int position)
			throws SQLException {
		return reader.read( rowProcessingState, options, position );
	}

	private interface Reader {
		Object read(RowProcessingState rowProcessingState, ResultSetProcessingOptions options, int position)
				throws SQLException;
	}

	static class JdbcTypeCodeReaderImpl implements Reader {
		private final int jdbcTypeCode;

		public JdbcTypeCodeReaderImpl(int jdbcTypeCode) {
			this.jdbcTypeCode = jdbcTypeCode;
		}

		@Override
		public Object read(
				RowProcessingState rowProcessingState,
				ResultSetProcessingOptions options,
				int position) throws SQLException {
			final Class javaClassMapping = RecommendedJdbcTypeMappings.INSTANCE.determineJavaClassForJdbcTypeCode( jdbcTypeCode );
			final JavaTypeDescriptor javaTypeDescriptor = JavaTypeDescriptorRegistry.INSTANCE.getDescriptor( javaClassMapping );

			final ResultSet resultSet = rowProcessingState.getResultSetProcessingState().getResultSet();

			switch ( jdbcTypeCode ) {
				case Types.BIGINT: {
					return javaTypeDescriptor.wrap( resultSet.getLong( position ), null );
				}
				case Types.BIT: {
					return javaTypeDescriptor.wrap( resultSet.getBoolean( position ), null );
				}
				case Types.BOOLEAN: {
					return javaTypeDescriptor.wrap( resultSet.getBoolean( position ), null );
				}
				case Types.CHAR: {
					return javaTypeDescriptor.wrap( resultSet.getString( position ), null );
				}
				case Types.DATE: {
					return javaTypeDescriptor.wrap( resultSet.getDate( position ), null );
				}
				case Types.DECIMAL: {
					return javaTypeDescriptor.wrap( resultSet.getBigDecimal( position ), null );
				}
				case Types.DOUBLE: {
					return javaTypeDescriptor.wrap( resultSet.getDouble( position ), null );
				}
				case Types.FLOAT: {
					return javaTypeDescriptor.wrap( resultSet.getFloat( position ), null );
				}
				case Types.INTEGER: {
					return javaTypeDescriptor.wrap( resultSet.getInt( position ), null );
				}
				case Types.LONGNVARCHAR: {
					return javaTypeDescriptor.wrap( resultSet.getString( position ), null );
				}
				case Types.LONGVARCHAR: {
					return javaTypeDescriptor.wrap( resultSet.getString( position ), null );
				}
				case Types.LONGVARBINARY: {
					return javaTypeDescriptor.wrap( resultSet.getBytes( position ), null );
				}
				case Types.NCHAR: {
					return javaTypeDescriptor.wrap( resultSet.getString( position ), null );
				}
				case Types.NUMERIC: {
					return javaTypeDescriptor.wrap( resultSet.getBigDecimal( position ), null );
				}
				case Types.NVARCHAR: {
					return javaTypeDescriptor.wrap( resultSet.getString( position ), null );
				}
				case Types.TIME: {
					return javaTypeDescriptor.wrap( resultSet.getTime( position ), null );
				}
				case Types.TIMESTAMP: {
					return javaTypeDescriptor.wrap( resultSet.getTimestamp( position ), null );
				}
				case Types.VARCHAR: {
					return javaTypeDescriptor.wrap( resultSet.getString( position ), null );
				}
			}

			throw new UnsupportedOperationException( "JDBC type [" + jdbcTypeCode + " not supported" );
		}
	}

	private class BasicTypeReaderAdapterImpl implements Reader {
		private final BasicType basicType;

		public BasicTypeReaderAdapterImpl(BasicType basicType) {
			this.basicType = basicType;
		}

		@Override
		public Object read(
				RowProcessingState rowProcessingState,
				ResultSetProcessingOptions options,
				int position) throws SQLException {
			throw new NotYetImplementedException( "Type does not support read-by-position" );
		}
	}
}
