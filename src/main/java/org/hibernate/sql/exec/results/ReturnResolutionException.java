/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.exec.results;

import org.hibernate.HibernateException;

/**
 * @author Steve Ebersole
 */
public class ReturnResolutionException extends HibernateException {
	public ReturnResolutionException(String message) {
		super( "Problem resolving query Return into ResolvedReturn : " + message );
	}

	public ReturnResolutionException(Throwable cause) {
		super( "Problem resolving query Return into ResolvedReturn", cause );
	}

	public ReturnResolutionException(String message, Throwable cause) {
		super( "Problem resolving query Return into ResolvedReturn : " + message, cause );
	}
}
