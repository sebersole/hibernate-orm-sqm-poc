/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.ast.select;

/**
 * @author Steve Ebersole
 */
public interface SqlSelectionDescriptor {
	// todo : would be nice to hook this in with an array of the raw selection values per row.
	//		the idea being to have an array of the raw SQL row values for cases where we
	// 		need them multiple times; plus would help in terms of reading cached
	//		query results (the value array would be the same).  The array would be the same
	//		length as the SQL selections.
	//
	// also usable when building the cache entries.  Possibly as a builder contract to
	//		account for no-caching.  Maybe ResultSetProcessor could act as this contract
	//		to collect the rows to be cached.
	//
	// another option is varying levels of "reader": RawValueReader, HydratedValueReader, ResolvedValueReader
	//		RawValueReader works on the individual SqlSelectionDescriptor instances which would mean
	//		we need some resolution of SqlSelectionDescriptor->Type (possibly limited to just BasicType).
	//		But the idea here is that we could use the "RawValueReader" to manage that process from
	//		the RowProcessor, building the "sql row array" which can be cached directly and can be used
	//		by the next reader

	SqlSelectable getSqlSelectable();

	/**
	 * Get the position within the values array
	 */
	int getValuesArrayPosition();

	default int getJdbcResultSetIndex() {
		return getValuesArrayPosition() + 1;
	}
}
