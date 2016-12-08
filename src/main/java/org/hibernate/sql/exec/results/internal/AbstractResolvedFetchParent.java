/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.exec.results.internal;

import java.util.List;

import org.hibernate.sql.ast.select.SqlSelectionDescriptor;
import org.hibernate.sql.convert.results.spi.Fetch;
import org.hibernate.sql.exec.results.spi.ResolvedFetch;
import org.hibernate.sql.exec.results.spi.ResolvedFetchParent;

/**
 * @author Steve Ebersole
 */
public abstract class AbstractResolvedFetchParent implements ResolvedFetchParent {
	@Override
	public ResolvedFetch addFetch(
			List<SqlSelectionDescriptor> sqlSelectionDescriptors,
			boolean shallow,
			Fetch queryFetch) {
		final ResolvedFetch resolvedFetch = queryFetch.resolve(
				this,
				sqlSelectionDescriptors,
				shallow
		);
		getInitializerParentForFetchInitializers().addChildFetchInitializer(
				resolvedFetch.getFetchInitializer( getInitializerParentForFetchInitializers() )
		);
		return resolvedFetch;
	}
}
