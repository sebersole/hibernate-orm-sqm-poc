/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.exec.spi;

import org.hibernate.ScrollMode;
import org.hibernate.cache.spi.QueryCache;
import org.hibernate.sql.orm.QueryParameterBindings;

/**
 * Access to options for query execution
 *
 * @author Steve Ebersole
 */
public interface ExecutionOptions {
	QueryParameterBindings getParameterBindings();
	Integer getTimeout();
	Integer getFetchSize();
	ScrollMode getScrollMode();
	QueryCache getQueryResultCache();
}
