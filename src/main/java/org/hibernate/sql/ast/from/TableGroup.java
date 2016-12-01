/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.sql.ast.from;

import java.util.List;

import org.hibernate.persister.common.spi.SingularAttributeDescriptor;
import org.hibernate.persister.common.spi.Table;
import org.hibernate.sql.ast.expression.domain.ColumnBindingSource;
import org.hibernate.sql.ast.expression.domain.EntityReferenceExpression;

/**
 * Group together related {@link TableBinding} references (generally related by EntityPersister or CollectionPersister),
 *
 * @author Steve Ebersole
 */
public interface TableGroup extends ColumnBindingSource {
	TableSpace getTableSpace();
	String getUid();
	String getAliasBase();
	TableBinding getRootTableBinding();
	List<TableJoin> getTableJoins();


	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// questionable

	/**
	 * We most likely need this (or similar) but its poorly named.  It is used to build
	 * join predicates - so this resolves the lhs/rhs column bindings.
	 */
	ColumnBinding[] resolveBindings(SingularAttributeDescriptor attribute);

	EntityReferenceExpression resolveEntityReference();

	TableBinding locateTableBinding(Table table);
}
