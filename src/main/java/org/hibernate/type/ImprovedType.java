/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.type;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.engine.jdbc.Size;
import org.hibernate.engine.spi.Mapping;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.type.descriptor.java.JavaTypeDescriptor;

/**
 * @author Steve Ebersole
 */
public interface ImprovedType extends org.hibernate.sqm.domain.Type {
	/**
	 * Enumerated values for the classification of the Type.
	 */
	enum Classification {
		/**
		 * Represents basic types (Strings, Integers, enums, etc).  Types classified as
		 * BASIC will be castable to {@link ImprovedBasicType}
		 */
		BASIC,
		/**
		 * Represents composite values (what JPA calls embedded/embeddable).  Types classified as
		 * COMPOSITE will be castable to {@link ImprovedCompositeType}
		 */
		COMPOSITE,
		/**
		 * Represents reverse-discriminated values (where the discriminator is on the FK side of the association).
		 * Types classified as ANY will be castable to {@link ImprovedAnyType}
		 */
		ANY,
		/**
		 * Represents an entity value (either as a root, one-to-one or many-to-one).  Types classified
		 * as ENTITY will be castable to {@link ImprovedEntityType}
		 */
		ENTITY,
		/**
		 * Represents a plural attribute, including the FK.   Types classified as COLLECTION
		 * will be castable to {@link ImprovedCollectionType}
		 */
		COLLECTION
	}

	/**
	 * Return the classification of this Type.
	 *
	 * @return
	 */
	Classification getClassification();

	/**
	 * Returns the abbreviated name of the Type.  Mostly used historically for short-name
	 * referencing of the Type in {@code hbm.xml} mappings.
	 *
	 * @return The Type name
	 */
	String getName();

	/**
	 * Obtain a descriptor for the Java side of a value mapping.
	 *
	 * @return
	 */
	JavaTypeDescriptor getJavaTypeDescriptor();

	/**
	 * How many columns are used to persist this type.  Always the same as {@code sqlTypes(mapping).length}
	 *
	 * @param mapping The mapping object :/
	 *
	 * @return The number of columns
	 *
	 * @throws MappingException Generally indicates an issue accessing the passed mapping object.
	 */
	int getColumnSpan(Mapping mapping) throws MappingException;

	/**
	 * Return the JDBC types codes (per {@link java.sql.Types}) for the columns mapped by this type.
	 * <p/>
	 * NOTE: The number of elements in this array matches the return from {@link #getColumnSpan}.
	 *
	 * @param mapping The mapping object :/
	 *
	 * @return The JDBC type codes.
	 *
	 * @throws MappingException Generally indicates an issue accessing the passed mapping object.
	 */
	int[] sqlTypes(Mapping mapping) throws MappingException;

	/**
	 * Return the column sizes dictated by this type.  For example, the mapping for a {@code char}/{@link Character} would
	 * have a dictated length limit of 1; for a string-based {@link java.util.UUID} would have a size limit of 36; etc.
	 * <p/>
	 * NOTE: The number of elements in this array matches the return from {@link #getColumnSpan}.
	 *
	 * @param mapping The mapping object :/
	 * @todo Would be much much better to have this aware of Dialect once the service/metamodel split is done
	 *
	 * @return The dictated sizes.
	 *
	 * @throws MappingException Generally indicates an issue accessing the passed mapping object.
	 */
	Size[] dictatedSizes(Mapping mapping) throws MappingException;

	/**
	 * Defines the column sizes to use according to this type if the user did not explicitly say (and if no
	 * {@link #dictatedSizes} were given).
	 * <p/>
	 * NOTE: The number of elements in this array matches the return from {@link #getColumnSpan}.
	 *
	 * @param mapping The mapping object :/
	 * @todo Would be much much better to have this aware of Dialect once the service/metamodel split is done
	 *
	 * @return The default sizes.
	 *
	 * @throws MappingException Generally indicates an issue accessing the passed mapping object.
	 */
	Size[] defaultSizes(Mapping mapping) throws MappingException;

	/**
	 * Extract a value from the JDBC result set.  This is useful for 2-phase property initialization - the second
	 * phase is a call to {@link #resolve}
	 * This hydrated value will be either:<ul>
	 *     <li>in the case of an entity or collection type, the key</li>
	 *     <li>otherwise, the value itself</li>
	 * </ul>
	 *
	 * @param rs The JDBC result set
	 * @param names the column names making up this type value (use to read from result set)
	 * @param session The originating session
	 * @param owner the parent entity
	 *
	 * @return An entity or collection key, or an actual value.
	 *
	 * @throws HibernateException An error from Hibernate
	 * @throws SQLException An error from the JDBC driver
	 *
	 * @see #resolve
	 */
	Object hydrate(ResultSet rs, String[] names, SessionImplementor session, Object owner)
			throws HibernateException, SQLException;

	/**
	 * The second phase of 2-phase loading.  Only really pertinent for entities and collections.  Here we resolve the
	 * identifier to an entity or collection instance
	 *
	 * @param value an identifier or value returned by <tt>hydrate()</tt>
	 * @param owner the parent entity
	 * @param session the session
	 *
	 * @return the given value, or the value associated with the identifier
	 *
	 * @throws HibernateException An error from Hibernate
	 *
	 * @see #hydrate
	 */
	public Object resolve(Object value, SessionImplementor session, Object owner)
			throws HibernateException;

	/**
	 * Given a hydrated, but unresolved value, return a value that may be used to reconstruct property-ref
	 * associations.
	 *
	 * @param value The unresolved, hydrated value
	 * @param session THe originating session
	 * @param owner The value owner
	 *
	 * @return The semi-resolved value
	 *
	 * @throws HibernateException An error from Hibernate
	 */
	public Object semiResolve(Object value, SessionImplementor session, Object owner)
			throws HibernateException;

	/**
	 * As part of 2-phase loading, when we perform resolving what is the resolved type for this type?  Generally
	 * speaking the type and its semi-resolved type will be the same.  The main deviation from this is in the
	 * case of an entity where the type would be the entity type and semi-resolved type would be its identifier type
	 *
	 * @param factory The session factory
	 *
	 * @return The semi-resolved type
	 */
	public ImprovedType getSemiResolvedType(SessionFactoryImplementor factory);

	/**
	 * Generate a representation of the value for logging purposes.
	 *
	 * @param value The value to be logged
	 * @param factory The session factory
	 *
	 * @return The loggable representation
	 *
	 * @throws HibernateException An error from Hibernate
	 */
	String toLoggableString(Object value, SessionFactoryImplementor factory);
}
