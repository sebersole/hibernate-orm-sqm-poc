/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.persister.common.spi;

import java.util.List;

import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.sqm.domain.DomainReference;

/**
 * @todo merge (tbd how exactly) with org.hibernate.persister.walking.spi
 *
 * @author Steve Ebersole
 */
public interface DomainDescriptor extends DomainReference {
	int getColumnCount(boolean shallow, SessionFactoryImplementor factory);

	List<Column> getColumns(boolean shallow, SessionFactoryImplementor factory);
}
