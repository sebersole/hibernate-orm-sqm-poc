/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.spi;

import java.util.Set;

import org.hibernate.query.QueryParameter;
import org.hibernate.sqm.query.Statement;

/**
 * @author Steve Ebersole
 */
public interface QueryPlan {
	Statement getSqm();
	Set<QueryParameter> getQueryParameters();

	// todo : expose SQL trees too.
}
