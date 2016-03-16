/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.ast.expression.instantiation;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.sql.exec.results.spi.ResultSetProcessingOptions;
import org.hibernate.sql.exec.results.spi.ReturnReader;
import org.hibernate.sql.exec.results.spi.RowProcessingState;
import org.hibernate.sql.gen.NotYetImplementedException;

/**
 * @author Steve Ebersole
 */
public class ReturnReaderDynamicInstantiationMapImpl implements ReturnReader {
	private final List<EntryReader> entryReaders;

	public ReturnReaderDynamicInstantiationMapImpl(List<DynamicInstantiationArgument> arguments) {
		this.entryReaders = new ArrayList<EntryReader>();
		for ( DynamicInstantiationArgument argument : arguments ) {
			final ReturnReader reader = argument.getExpression().getReturnReader();
			entryReaders.add( new EntryReader( argument.getAlias(), reader ) );
		}
	}

	@Override
	public void readBasicValues(
			RowProcessingState processingState,
			ResultSetProcessingOptions options) throws SQLException {
		throw new NotYetImplementedException();
	}

	@Override
	public void resolveBasicValues(
			RowProcessingState processingState,
			ResultSetProcessingOptions options) throws SQLException {
		throw new NotYetImplementedException();
	}

	@Override
	public Object assemble(
			RowProcessingState processingState,
			ResultSetProcessingOptions options) throws SQLException {
		throw new NotYetImplementedException();
	}

	@Override
	public Class getReturnedJavaType() {
		return Map.class;
	}

	@Override
	public Object readResult(
			RowProcessingState processingState,
			ResultSetProcessingOptions options,
			int startPosition,
			Object owner) throws SQLException {
		final HashMap result = new HashMap();

		int position = startPosition;
		for ( EntryReader entryReader : entryReaders ) {
			result.put(
					entryReader.alias,
					entryReader.reader.readResult(
							processingState,
							options,
							position,
							owner
					)
			);
			position += entryReader.reader.getNumberOfColumnsRead( processingState );
		}

		return result;
	}

	@Override
	public int getNumberOfColumnsRead(RowProcessingState processingState) {
		int i = 0;
		for ( EntryReader entryReader : entryReaders ) {
			i += entryReader.reader.getNumberOfColumnsRead( processingState );
		}
		return i;
	}

	private static class EntryReader {
		private final String alias;
		private final ReturnReader reader;

		EntryReader(String alias, ReturnReader reader) {
			this.alias = alias;
			this.reader = reader;
		}
	}
}
