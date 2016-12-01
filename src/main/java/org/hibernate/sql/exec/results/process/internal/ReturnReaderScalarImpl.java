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
import java.util.ArrayList;
import java.util.List;

import org.hibernate.EntityMode;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.sql.ast.select.SqlSelectionDescriptor;
import org.hibernate.sql.exec.internal.RecommendedJdbcTypeMappings;
import org.hibernate.sql.exec.results.process.spi.ResultSetProcessingOptions;
import org.hibernate.sql.exec.results.process.spi.ReturnReader;
import org.hibernate.sql.exec.results.process.spi.RowProcessingState;
import org.hibernate.type.CompositeType;
import org.hibernate.type.Type;
import org.hibernate.type.descriptor.java.JavaTypeDescriptor;
import org.hibernate.type.descriptor.java.JavaTypeDescriptorRegistry;

import org.jboss.logging.Logger;

/**
 * @author Steve Ebersole
 */
public class ReturnReaderScalarImpl implements ReturnReader {
	private static final Logger log = Logger.getLogger( ReturnReaderScalarImpl.class );

	private final List<SqlSelectionDescriptor> selectionDescriptorList;
	private final Type returnType;

	public ReturnReaderScalarImpl(
			List<SqlSelectionDescriptor> selectionDescriptorList,
			Type returnType) {
		// make a copy of the selection descriptor list
		this.selectionDescriptorList = new ArrayList<>( selectionDescriptorList );
		this.returnType = returnType;

		if ( returnType == null ) {
			throw new IllegalArgumentException( "Passed `returnType` cannot be null" );
		}
	}

	@Override
	public Class getReturnedJavaType() {
		return returnType.getReturnedClass();
	}

	@Override
	public int getNumberOfColumnsRead(SessionFactoryImplementor sessionFactory) {
		return returnType.sqlTypes( sessionFactory ).length;
	}

	@Override
	public void readBasicValues(RowProcessingState processingState, ResultSetProcessingOptions options) {
		// nothing to do
	}

	@Override
	public void resolveBasicValues(RowProcessingState processingState, ResultSetProcessingOptions options) {
		// nothing to do
	}

	@Override
	public Object assemble(RowProcessingState processingState, ResultSetProcessingOptions options) throws SQLException {
		// for now we assume basic types with no attribute conversion etc.
		// a more correct implementation requires the "positional read" changes to Type.

		final SharedSessionContractImplementor session = processingState.getResultSetProcessingState().getSession();
		final ResultSet resultSet = processingState.getResultSetProcessingState().getResultSet();

		final int columnSpan = returnType.getColumnSpan( session.getFactory() );
		final int[] jdbcTypes = returnType.sqlTypes( session.getFactory() );
		if ( columnSpan > 1 ) {
			// has to be a CompositeType for now (and a very basic, one-level one)...
			final CompositeType ctype = (CompositeType) returnType;
			final Object[] values = new Object[ columnSpan ];
			for ( int i = 0; i < columnSpan; i++ ) {
				values[i] = readResultValue( resultSet, selectionDescriptorList.get( i ), jdbcTypes[i] );
			}
			try {
				final Object result = ctype.getReturnedClass().newInstance();
				ctype.setPropertyValues( result, values, EntityMode.POJO );
				return result;
			}
			catch (Exception e) {
				throw new RuntimeException( "Unable to instantiate composite : " +  ctype.getReturnedClass().getName(), e );
			}
		}
		else {
			return readResultValue( resultSet, selectionDescriptorList.get( 0 ), jdbcTypes[0] );
		}
	}

	private Object readResultValue(ResultSet resultSet, SqlSelectionDescriptor selectionDescriptor, int jdbcType) throws SQLException {
		final Class javaClassMapping = RecommendedJdbcTypeMappings.INSTANCE.determineJavaClassForJdbcTypeCode( jdbcType );
		final JavaTypeDescriptor javaTypeDescriptor = JavaTypeDescriptorRegistry.INSTANCE.getDescriptor( javaClassMapping );

		switch ( jdbcType ) {
			case Types.BIGINT: {
				return javaTypeDescriptor.wrap( resultSet.getLong( selectionDescriptor.getIndex() ), null );
			}
			case Types.BIT: {
				return javaTypeDescriptor.wrap( resultSet.getBoolean( selectionDescriptor.getIndex() ), null );
			}
			case Types.BOOLEAN: {
				return javaTypeDescriptor.wrap( resultSet.getBoolean( selectionDescriptor.getIndex() ), null );
			}
			case Types.CHAR: {
				return javaTypeDescriptor.wrap( resultSet.getString( selectionDescriptor.getIndex() ), null );
			}
			case Types.DATE: {
				return javaTypeDescriptor.wrap( resultSet.getDate( selectionDescriptor.getIndex() ), null );
			}
			case Types.DECIMAL: {
				return javaTypeDescriptor.wrap( resultSet.getBigDecimal( selectionDescriptor.getIndex() ), null );
			}
			case Types.DOUBLE: {
				return javaTypeDescriptor.wrap( resultSet.getDouble( selectionDescriptor.getIndex() ), null );
			}
			case Types.FLOAT: {
				return javaTypeDescriptor.wrap( resultSet.getFloat( selectionDescriptor.getIndex() ), null );
			}
			case Types.INTEGER: {
				return javaTypeDescriptor.wrap( resultSet.getInt( selectionDescriptor.getIndex() ), null );
			}
			case Types.LONGNVARCHAR: {
				return javaTypeDescriptor.wrap( resultSet.getString( selectionDescriptor.getIndex() ), null );
			}
			case Types.LONGVARCHAR: {
				return javaTypeDescriptor.wrap( resultSet.getString( selectionDescriptor.getIndex() ), null );
			}
			case Types.LONGVARBINARY: {
				return javaTypeDescriptor.wrap( resultSet.getBytes( selectionDescriptor.getIndex() ), null );
			}
			case Types.NCHAR: {
				return javaTypeDescriptor.wrap( resultSet.getString( selectionDescriptor.getIndex() ), null );
			}
			case Types.NUMERIC: {
				return javaTypeDescriptor.wrap( resultSet.getBigDecimal( selectionDescriptor.getIndex() ), null );
			}
			case Types.NVARCHAR: {
				return javaTypeDescriptor.wrap( resultSet.getString( selectionDescriptor.getIndex() ), null );
			}
			case Types.TIME: {
				return javaTypeDescriptor.wrap( resultSet.getTime( selectionDescriptor.getIndex() ), null );
			}
			case Types.TIMESTAMP: {
				return javaTypeDescriptor.wrap( resultSet.getTimestamp( selectionDescriptor.getIndex() ), null );
			}
			case Types.VARCHAR: {
				return javaTypeDescriptor.wrap( resultSet.getString( selectionDescriptor.getIndex() ), null );
			}
		}

		throw new UnsupportedOperationException( "JDBC type [" + jdbcType + " not supported" );
	}
}
