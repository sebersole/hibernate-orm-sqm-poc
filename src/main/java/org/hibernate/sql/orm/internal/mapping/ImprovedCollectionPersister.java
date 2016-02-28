/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.sql.orm.internal.mapping;

import org.hibernate.persister.collection.CollectionPersister;
import org.hibernate.sql.ast.from.CollectionTableSpecificationGroup;
import org.hibernate.sql.ast.from.TableSpace;
import org.hibernate.sql.gen.internal.FromClauseIndex;
import org.hibernate.sql.gen.internal.SqlAliasBaseManager;
import org.hibernate.sqm.query.from.JoinedFromElement;

/**
 * @author Steve Ebersole
 */
public interface ImprovedCollectionPersister {
	CollectionPersister getPersister();

	CollectionTableSpecificationGroup getCollectionTableSpecificationGroup(
			JoinedFromElement joinedFromElement,
			TableSpace tableSpace,
			SqlAliasBaseManager sqlAliasBaseManager,
			FromClauseIndex fromClauseIndex);
}
