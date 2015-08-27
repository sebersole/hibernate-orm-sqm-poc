/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.sql.orm;

/**
 * This is largely {@link org.hibernate.engine.spi.QueryParameters}.  But I have always hated that name, as it
 * is so confusing.
 *
 * @author Steve Ebersole
 */
public interface QueryOptions {
	QueryParameterBindings getParameterBindings();
}
