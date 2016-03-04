/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.orm.internal.mapping;

import java.util.Map;
import java.util.TreeMap;

import org.hibernate.MappingException;

/**
 * @author Steve Ebersole
 */
public abstract class AbstractTable implements TableReference {
	private final Map<String,Value> valueMap = new TreeMap<String, Value>( String.CASE_INSENSITIVE_ORDER );

	public Column makeColumn(String name, int jdbcType) {
		if ( valueMap.containsKey( name ) ) {
			// assume it is a Column
			@SuppressWarnings("UnnecessaryLocalVariable") final Column existing = (Column) valueMap.get( name );
			// todo : "type compatibility" checks would be nice
			return existing;
		}
		final Column column = new Column( this, name, jdbcType );
		valueMap.put( name, column );
		return column;
	}

	public Formula makeFormula(String expression, int jdbcType) {
		// for now, we use expression as registration key but that allows reuse of formula mappings, we may want to
		// force separate expressions in this case...
		final String registrationKey = expression;

		if ( valueMap.containsKey( registrationKey ) ) {
			// assume it is a Formula
			@SuppressWarnings("UnnecessaryLocalVariable") final Formula existing = (Formula) valueMap.get( registrationKey );
			// todo : "type compatibility" checks would be nice
			return existing;
		}
		final Formula formula = new Formula( this, expression, jdbcType );
		valueMap.put( registrationKey, formula );
		return formula;
	}

	@Override
	public Value getValue(String name) {
		final Value match = valueMap.get( name );
		if ( match == null ) {
			throw new MappingException( "Could not locate value : " + name );
		}
		return match;
	}

}
