/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.sql.ast.from;

import org.hibernate.sql.orm.internal.mapping.TableReference;

/**
 * Represents a reference to a mapped "table reference".
 *
 * @author Steve Ebersole
 */
public class Table {
	private final TableReference tableReference;
	private final String identificationVariable;

	public Table(TableReference tableReference, String identificationVariable) {
		this.tableReference = tableReference;
		this.identificationVariable = identificationVariable;
	}

	public TableReference getTableReference() {
		return tableReference;
	}

	public String getIdentificationVariable() {
		return identificationVariable;
	}
}
