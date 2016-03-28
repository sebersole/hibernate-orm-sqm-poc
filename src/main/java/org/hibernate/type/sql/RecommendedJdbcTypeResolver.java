/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.type.sql;

import org.hibernate.type.descriptor.java.JavaTypeDescriptor;
import org.hibernate.type.descriptor.sql.SqlTypeDescriptor;

/**
 * @author Steve Ebersole
 */
public interface RecommendedJdbcTypeResolver {
	SqlTypeDescriptor resolveRecommendedJdbcType(JavaTypeDescriptor javaTypeDescriptor, RecommendedJdbcTypeResolutionContext context);
}
