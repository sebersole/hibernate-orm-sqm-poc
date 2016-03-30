/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.exec.internal;

import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Ref;
import java.sql.Struct;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Calendar;
import java.util.concurrent.ConcurrentHashMap;

import org.hibernate.mapping.Array;

import org.jboss.logging.Logger;

/**
 * @author Steve Ebersole
 *
 * @deprecated See {@link org.hibernate.type.descriptor.sql.SqlTypeDescriptor#getJdbcRecommendedJavaTypeMapping()}
 * and {@link org.hibernate.type.descriptor.java.JavaTypeDescriptor#getJdbcRecommendedSqlType} instead.
 */
@Deprecated
public class RecommendedJdbcTypeMappings {
	// todo : port to org.hibernate.type.descriptor.sql.JdbcTypeJavaClassMappings

	private static final Logger log = Logger.getLogger( RecommendedJdbcTypeMappings.class );

	/**
	 * Singleton access
	 */
	public static final RecommendedJdbcTypeMappings INSTANCE = new RecommendedJdbcTypeMappings();

	private final ConcurrentHashMap<Class, Integer> javaClassToJdbcTypeCodeMap = buildJavaClassToJdbcTypeCodeMap();
	private final ConcurrentHashMap<Integer, Class> jdbcTypeCodeToJavaClassMap = buildJdbcTypeCodeToJavaClassMap();

	public int determineJdbcTypeCodeForJavaClass(Class cls) {
		Integer typeCode = javaClassToJdbcTypeCodeMap.get( cls );
		if ( typeCode != null ) {
			return typeCode;
		}

		int specialCode = cls.hashCode();
		log.debug(
				"JDBC type code mapping not known for class [" + cls.getName() + "]; using custom code [" + specialCode + "]"
		);
		return specialCode;
	}

	public Class determineJavaClassForJdbcTypeCode(Integer typeCode) {
		Class cls = jdbcTypeCodeToJavaClassMap.get( typeCode );
		if ( cls != null ) {
			return cls;
		}

		log.debugf(
				"Java Class mapping not known for JDBC type code [%s]; using java.lang.Object",
				typeCode
		);
		return Object.class;
	}

	public Class determineJavaClassForJdbcTypeCode(int typeCode) {
		return determineJavaClassForJdbcTypeCode( Integer.valueOf( typeCode ) );
	}

	private static ConcurrentHashMap<Class, Integer> buildJavaClassToJdbcTypeCodeMap() {
		ConcurrentHashMap<Class, Integer> jdbcJavaClassMappings = new ConcurrentHashMap<Class, Integer>();

		// these mappings are the ones outlined specifically in the spec
		jdbcJavaClassMappings.put( String.class, Types.VARCHAR );
		jdbcJavaClassMappings.put( BigDecimal.class, Types.NUMERIC );
		jdbcJavaClassMappings.put( Boolean.class, Types.BIT );
		jdbcJavaClassMappings.put( Integer.class, Types.INTEGER );
		jdbcJavaClassMappings.put( Long.class, Types.BIGINT );
		jdbcJavaClassMappings.put( Float.class, Types.REAL );
		jdbcJavaClassMappings.put( Double.class, Types.DOUBLE );
		jdbcJavaClassMappings.put( byte[].class, Types.LONGVARBINARY );
		jdbcJavaClassMappings.put( java.sql.Date.class, Types.DATE );
		jdbcJavaClassMappings.put( Time.class, Types.TIME );
		jdbcJavaClassMappings.put( Timestamp.class, Types.TIMESTAMP );
		jdbcJavaClassMappings.put( Blob.class, Types.BLOB );
		jdbcJavaClassMappings.put( Clob.class, Types.CLOB );
		jdbcJavaClassMappings.put( Array.class, Types.ARRAY );
		jdbcJavaClassMappings.put( Struct.class, Types.STRUCT );
		jdbcJavaClassMappings.put( Ref.class, Types.REF );
		jdbcJavaClassMappings.put( Class.class, Types.JAVA_OBJECT );

		// additional "common sense" registrations
		jdbcJavaClassMappings.put( Character.class, Types.CHAR );
		jdbcJavaClassMappings.put( char[].class, Types.VARCHAR );
		jdbcJavaClassMappings.put( Character[].class, Types.VARCHAR );
		jdbcJavaClassMappings.put( Byte[].class, Types.LONGVARBINARY );
		jdbcJavaClassMappings.put( java.util.Date.class, Types.TIMESTAMP );
		jdbcJavaClassMappings.put( Calendar.class, Types.TIMESTAMP );

		return jdbcJavaClassMappings;
	}


	private static ConcurrentHashMap<Integer, Class> buildJdbcTypeCodeToJavaClassMap() {
		ConcurrentHashMap<Integer, Class> jdbcTypeCodeToJavaClassMap = new ConcurrentHashMap<Integer, Class>();

		// these mappings are the ones outlined specifically in the spec
		jdbcTypeCodeToJavaClassMap.put( Types.BIT, Boolean.class );
		jdbcTypeCodeToJavaClassMap.put( Types.BOOLEAN, Boolean.class );
		jdbcTypeCodeToJavaClassMap.put( Types.NUMERIC, BigDecimal.class );
		jdbcTypeCodeToJavaClassMap.put( Types.INTEGER, Integer.class );
		jdbcTypeCodeToJavaClassMap.put( Types.BIGINT, Long.class );
		jdbcTypeCodeToJavaClassMap.put( Types.DOUBLE, Double.class );
		jdbcTypeCodeToJavaClassMap.put( Types.REAL, Float.class );

		jdbcTypeCodeToJavaClassMap.put( Types.VARCHAR, String.class );
		jdbcTypeCodeToJavaClassMap.put( Types.NVARCHAR, String.class );
		jdbcTypeCodeToJavaClassMap.put( Types.LONGVARCHAR, String.class );
		jdbcTypeCodeToJavaClassMap.put( Types.LONGNVARCHAR, String.class );
		jdbcTypeCodeToJavaClassMap.put( Types.CHAR, String.class );
		jdbcTypeCodeToJavaClassMap.put( Types.NCHAR, String.class );
		jdbcTypeCodeToJavaClassMap.put( Types.CLOB, Clob.class );
		jdbcTypeCodeToJavaClassMap.put( Types.NCLOB, Clob.class );

		jdbcTypeCodeToJavaClassMap.put( Types.LONGVARBINARY, byte[].class );
		jdbcTypeCodeToJavaClassMap.put( Types.BLOB, Blob.class );

		jdbcTypeCodeToJavaClassMap.put( Types.DATE, java.sql.Date.class );
		jdbcTypeCodeToJavaClassMap.put( Types.TIME, Time.class );
		jdbcTypeCodeToJavaClassMap.put( Types.TIMESTAMP, Timestamp.class );
		jdbcTypeCodeToJavaClassMap.put( Types.JAVA_OBJECT, Class.class );

		// additional mappings
		jdbcTypeCodeToJavaClassMap.put( Types.VARBINARY, byte[].class );
		jdbcTypeCodeToJavaClassMap.put( Types.BINARY, byte[].class );

		return jdbcTypeCodeToJavaClassMap;
	}
}
