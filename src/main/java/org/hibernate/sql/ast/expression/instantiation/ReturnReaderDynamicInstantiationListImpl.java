/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.ast.expression.instantiation;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.sql.exec.results.spi.ResultSetProcessingOptions;
import org.hibernate.sql.exec.results.spi.ReturnReader;
import org.hibernate.sql.exec.results.spi.RowProcessingState;
import org.hibernate.sql.gen.NotYetImplementedException;

/**
 * @author Steve Ebersole
 */
public class ReturnReaderDynamicInstantiationListImpl implements ReturnReader<List> {
	private final List<ReturnReader> argumentReaders;

	public ReturnReaderDynamicInstantiationListImpl(List<DynamicInstantiationArgument> arguments) {
		this.argumentReaders = new ArrayList<ReturnReader>();
		for ( DynamicInstantiationArgument argument : arguments ) {
			argumentReaders.add( argument.getExpression().getReturnReader() );
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
			RowProcessingState processingState, ResultSetProcessingOptions options) throws SQLException {
		throw new NotYetImplementedException();
	}

	@Override
	public List assemble(
			RowProcessingState processingState, ResultSetProcessingOptions options) throws SQLException {
		throw new NotYetImplementedException();
	}

	@Override
	public Class<List> getReturnedJavaType() {
		return List.class;
	}

	@Override
	public List readResult(
			RowProcessingState processingState,
			ResultSetProcessingOptions options,
			int startPosition,
			Object owner) throws SQLException {
		final ArrayList result = new ArrayList();

		int position = startPosition;
		for ( ReturnReader argumentReader : argumentReaders ) {
			result.add(
					argumentReader.readResult(
							processingState,
							options,
							position,
							owner
					)
			);
			position += argumentReader.getNumberOfColumnsRead( processingState );
		}

		return result;
	}

	@Override
	public int getNumberOfColumnsRead(RowProcessingState processingState) {
		int i = 0;
		for ( ReturnReader argumentReader : argumentReaders ) {
			i += argumentReader.getNumberOfColumnsRead( processingState );
		}
		return i;
	}
}
