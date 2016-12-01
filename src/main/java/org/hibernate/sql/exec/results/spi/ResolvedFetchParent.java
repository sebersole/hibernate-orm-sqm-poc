/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.exec.results.spi;

import java.util.List;

import org.hibernate.loader.PropertyPath;

/**
 * Contract for things that can be the parent of a fetch
 *
 * @author Steve Ebersole
 */
public interface ResolvedFetchParent {
	/**
	 * Get the property path to this parent
	 *
	 * @return The property path
	 */
	PropertyPath getPropertyPath();

	/**
	 * Get the UID for this fetch source's query space.
	 *
	 * @return The query space UID.
	 */
	String getQuerySpaceUid();
	/**
	 * Retrieve the fetches owned by this fetch source.
	 *
	 * @return The owned fetches.
	 */
	List<ResolvedFetch> getFetches();
}
