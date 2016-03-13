/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.exec.spi;

import org.hibernate.CacheMode;
import org.hibernate.Incubating;

/**
 * Access to options for query execution
 *
 * @author Steve Ebersole
 */
@Incubating
public interface ExecutionOptions {
	Integer getTimeout();
	Integer getFetchSize();
	CacheMode getCacheMode();
	String getCacheRegion();
}
