/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.ast.expression.domain;

import java.util.List;

import org.hibernate.persister.common.spi.DomainDescriptor;
import org.hibernate.sql.ast.from.ColumnBinding;
import org.hibernate.sql.ast.from.TableGroup;

/**
 * Defines a "source" for ColumnBindings related to a DomainReference (relative
 * to this source).
 *
 * @author Steve Ebersole
 */
public interface ColumnBindingSource {
	TableGroup getTableGroup();

	/**
	 * Resolve the ColumnBindings for the passed DomainReferenceExpression.  Generally
	 * this resolves the bindings for the columns as returned from
	 * {@link DomainDescriptor#getColumns}
	 * via {@link DomainReferenceExpression#getDomainReference}
	 *
	 * @param expression The DomainReferenceExpression for which to resolve ColumnBindings
	 * @param shallow Should we perform a shallow resolution?
	 *
	 * @return The ColumnBindings
	 */
	List<ColumnBinding> resolveColumnBindings(DomainReferenceExpression expression, boolean shallow);
}
