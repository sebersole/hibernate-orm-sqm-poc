/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.sql.ast.from;

import java.util.HashMap;
import java.util.Map;

import org.hibernate.sql.ast.expression.ColumnReference;

/**
 * @author Steve Ebersole
 */
public abstract class AbstractTableSpecification implements TableSpecification {
	private final String alias;

	private Map<String,ColumnReferenceImpl> columnReferenceMap;

	public AbstractTableSpecification(String alias) {
		this.alias = alias;
	}

	@Override
	public String getCorrelationName() {
		return alias;
	}

	@Override
	public ColumnReference getColumnReference(String name) {
		ColumnReferenceImpl columnReference = null;
		if ( columnReferenceMap == null ) {
			columnReferenceMap = new HashMap<String, ColumnReferenceImpl>();
		}
		else {
			columnReference = columnReferenceMap.get( name );
		}

		if ( columnReference == null ) {
			columnReference = new ColumnReferenceImpl( this, name );
			columnReferenceMap.put( name, columnReference );
		}

		return columnReference;
	}
}
