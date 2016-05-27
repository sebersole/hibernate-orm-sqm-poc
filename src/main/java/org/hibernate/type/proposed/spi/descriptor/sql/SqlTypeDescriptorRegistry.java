/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.type.proposed.spi.descriptor.sql;

import java.io.Serializable;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;

import org.hibernate.type.descriptor.JdbcTypeNameMapper;
import org.hibernate.type.descriptor.ValueBinder;
import org.hibernate.type.descriptor.ValueExtractor;
import org.hibernate.type.descriptor.WrapperOptions;
import org.hibernate.type.descriptor.sql.JdbcTypeFamilyInformation;
import org.hibernate.type.proposed.spi.descriptor.java.JavaTypeDescriptor;

import org.jboss.logging.Logger;

/**
 * Basically a map from JDBC type code (int) -> {@link org.hibernate.type.proposed.spi.descriptor.sql.SqlTypeDescriptor}
 *
 * @author Steve Ebersole
 */
public class SqlTypeDescriptorRegistry {
	public static final SqlTypeDescriptorRegistry INSTANCE = new SqlTypeDescriptorRegistry();

	private static final Logger log = Logger.getLogger( SqlTypeDescriptorRegistry.class );

	private ConcurrentHashMap<Integer, org.hibernate.type.proposed.spi.descriptor.sql.SqlTypeDescriptor> descriptorMap = new ConcurrentHashMap<Integer, org.hibernate.type.proposed.spi.descriptor.sql.SqlTypeDescriptor>();

	private SqlTypeDescriptorRegistry() {
		addDescriptor( BooleanTypeDescriptor.INSTANCE );

		addDescriptor( BitTypeDescriptor.INSTANCE );
		addDescriptor( BigIntTypeDescriptor.INSTANCE );
		addDescriptor( DecimalTypeDescriptor.INSTANCE );
		addDescriptor( org.hibernate.type.proposed.spi.descriptor.sql.DoubleTypeDescriptor.INSTANCE );
		addDescriptor( FloatTypeDescriptor.INSTANCE );
		addDescriptor( org.hibernate.type.proposed.spi.descriptor.sql.IntegerTypeDescriptor.INSTANCE );
		addDescriptor( org.hibernate.type.proposed.spi.descriptor.sql.NumericTypeDescriptor.INSTANCE );
		addDescriptor( org.hibernate.type.proposed.spi.descriptor.sql.RealTypeDescriptor.INSTANCE );
		addDescriptor( SmallIntTypeDescriptor.INSTANCE );
		addDescriptor( TinyIntTypeDescriptor.INSTANCE );

		addDescriptor( org.hibernate.type.proposed.spi.descriptor.sql.DateTypeDescriptor.INSTANCE );
		addDescriptor( org.hibernate.type.proposed.spi.descriptor.sql.TimestampTypeDescriptor.INSTANCE );
		addDescriptor( org.hibernate.type.proposed.spi.descriptor.sql.TimeTypeDescriptor.INSTANCE );

		addDescriptor( org.hibernate.type.proposed.spi.descriptor.sql.BinaryTypeDescriptor.INSTANCE );
		addDescriptor( org.hibernate.type.proposed.spi.descriptor.sql.VarbinaryTypeDescriptor.INSTANCE );
		addDescriptor( LongVarbinaryTypeDescriptor.INSTANCE );
		addDescriptor( BlobTypeDescriptor.DEFAULT );

		addDescriptor( CharTypeDescriptor.INSTANCE );
		addDescriptor( org.hibernate.type.proposed.spi.descriptor.sql.VarcharTypeDescriptor.INSTANCE );
		addDescriptor( LongVarcharTypeDescriptor.INSTANCE );
		addDescriptor( ClobTypeDescriptor.DEFAULT );

		addDescriptor( org.hibernate.type.proposed.spi.descriptor.sql.NCharTypeDescriptor.INSTANCE );
		addDescriptor( NVarcharTypeDescriptor.INSTANCE );
		addDescriptor( org.hibernate.type.proposed.spi.descriptor.sql.LongNVarcharTypeDescriptor.INSTANCE );
		addDescriptor( NClobTypeDescriptor.DEFAULT );
	}

	public void addDescriptor(SqlTypeDescriptor sqlTypeDescriptor) {
		descriptorMap.put( sqlTypeDescriptor.getSqlType(), sqlTypeDescriptor );
	}

	public org.hibernate.type.proposed.spi.descriptor.sql.SqlTypeDescriptor getDescriptor(int jdbcTypeCode) {
		org.hibernate.type.proposed.spi.descriptor.sql.SqlTypeDescriptor descriptor = descriptorMap.get( Integer.valueOf( jdbcTypeCode ) );
		if ( descriptor != null ) {
			return descriptor;
		}

		if ( JdbcTypeNameMapper.isStandardTypeCode( jdbcTypeCode ) ) {
			log.debugf(
					"A standard JDBC type code [%s] was not defined in SqlTypeDescriptorRegistry",
					jdbcTypeCode
			);
		}

		// see if the typecode is part of a known type family...
		JdbcTypeFamilyInformation.Family family = JdbcTypeFamilyInformation.INSTANCE.locateJdbcTypeFamilyByTypeCode( jdbcTypeCode );
		if ( family != null ) {
			for ( int potentialAlternateTypeCode : family.getTypeCodes() ) {
				if ( potentialAlternateTypeCode != jdbcTypeCode ) {
					final SqlTypeDescriptor potentialAlternateDescriptor = descriptorMap.get( Integer.valueOf( potentialAlternateTypeCode ) );
					if ( potentialAlternateDescriptor != null ) {
						// todo : add a SqlTypeDescriptor.canBeAssignedFrom method...
						return potentialAlternateDescriptor;
					}

					if ( JdbcTypeNameMapper.isStandardTypeCode( potentialAlternateTypeCode ) ) {
						log.debugf(
								"A standard JDBC type code [%s] was not defined in SqlTypeDescriptorRegistry",
								potentialAlternateTypeCode
						);
					}
				}
			}
		}

		// finally, create a new descriptor mapping to getObject/setObject for this type code...
		final ObjectSqlTypeDescriptor fallBackDescriptor = new ObjectSqlTypeDescriptor( jdbcTypeCode );
		addDescriptor( fallBackDescriptor );
		return fallBackDescriptor;
	}

	public static class ObjectSqlTypeDescriptor implements SqlTypeDescriptor {
		private final int jdbcTypeCode;

		public ObjectSqlTypeDescriptor(int jdbcTypeCode) {
			this.jdbcTypeCode = jdbcTypeCode;
		}

		@Override
		public int getSqlType() {
			return jdbcTypeCode;
		}

		@Override
		public boolean canBeRemapped() {
			return true;
		}

		@Override
		public JavaTypeDescriptor getJdbcRecommendedJavaTypeMapping() {
			throw new UnsupportedOperationException( "No recommended Java-type mapping known for JDBC type code [" + jdbcTypeCode + "]" );
		}

		@Override
		public <X> ValueBinder<X> getBinder(JavaTypeDescriptor<X> javaTypeDescriptor) {
			if ( Serializable.class.isAssignableFrom( javaTypeDescriptor.getJavaTypeClass() ) ) {
				return VarbinaryTypeDescriptor.INSTANCE.getBinder( javaTypeDescriptor );
			}

			return new org.hibernate.type.proposed.spi.descriptor.sql.BasicBinder<X>( javaTypeDescriptor, this ) {
				@Override
				protected void doBind(PreparedStatement st, X value, int index, WrapperOptions options)
						throws SQLException {
					st.setObject( index, value, jdbcTypeCode );
				}

				@Override
				protected void doBind(CallableStatement st, X value, String name, WrapperOptions options)
						throws SQLException {
					st.setObject( name, value, jdbcTypeCode );
				}
			};
		}

		@Override
		@SuppressWarnings("unchecked")
		public ValueExtractor getExtractor(org.hibernate.type.proposed.spi.descriptor.java.JavaTypeDescriptor javaTypeDescriptor) {
			if ( Serializable.class.isAssignableFrom( javaTypeDescriptor.getJavaTypeClass() ) ) {
				return org.hibernate.type.proposed.spi.descriptor.sql.VarbinaryTypeDescriptor.INSTANCE.getExtractor( javaTypeDescriptor );
			}

			return new BasicExtractor( javaTypeDescriptor, this ) {
				@Override
				protected Object doExtract(ResultSet rs, String name, WrapperOptions options) throws SQLException {
					return rs.getObject( name );
				}

				@Override
				protected Object doExtract(CallableStatement statement, int index, WrapperOptions options) throws SQLException {
					return statement.getObject( index );
				}

				@Override
				protected Object doExtract(CallableStatement statement, String name, WrapperOptions options) throws SQLException {
					return statement.getObject( name );
				}
			};
		}
	}
}
