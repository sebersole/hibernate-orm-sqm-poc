/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.persister.common.spi;

import java.util.List;

import org.hibernate.sql.ast.from.ColumnBinding;
import org.hibernate.sql.ast.from.TableGroup;
import org.hibernate.sqm.domain.SingularAttributeReference;

/**
 * @author Steve Ebersole
 */
public interface SingularAttributeDescriptor extends SingularAttributeReference, OrmTypeExporter, AttributeDescriptor, DomainDescriptor {
	Column[] getColumns();
	List<ColumnBinding> resolveColumnBindings(TableGroup tableGroup, boolean shallow);
}
