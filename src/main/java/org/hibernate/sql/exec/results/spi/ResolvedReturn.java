/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.exec.results.spi;

import java.util.List;

import org.hibernate.sql.ast.select.SqlSelection;
import org.hibernate.sql.exec.results.process.spi2.ReturnAssembler;

/**
 * @author Steve Ebersole
 */
public interface ResolvedReturn {
	int getNumberOfSelectablesConsumed();

	List<SqlSelection> getSqlSelections();

	ReturnAssembler getReturnAssembler();

	Class getReturnedJavaType();

	// todo : either -
	//		1) an Initializer graph
	//		2) List of Entity and List of Collection Initializers
}
