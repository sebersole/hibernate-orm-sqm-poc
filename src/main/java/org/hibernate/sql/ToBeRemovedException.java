/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql;

/**
 * Corollary to NotYetImplementedException; ToBeRemovedException indicates
 * something that has been previously implemented but that needs to be removed.
 *
 * @author Steve Ebersole
 */
public class ToBeRemovedException extends RuntimeException {
	public ToBeRemovedException() {
		super( "Method to be removed" );
	}
}
