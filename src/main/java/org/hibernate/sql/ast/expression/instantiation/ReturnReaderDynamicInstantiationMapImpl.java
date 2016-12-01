/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.ast.expression.instantiation;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.sql.exec.results.process.spi.ResultSetProcessingOptions;
import org.hibernate.sql.exec.results.process.spi.ReturnReader;
import org.hibernate.sql.exec.results.process.spi.RowProcessingState;

/**
 * @author Steve Ebersole
 */
public class ReturnReaderDynamicInstantiationMapImpl implements ReturnReader {
	private final List<AliasedReturnReader> argumentReaders;
	private final int numberOfColumnsConsumed;

	public ReturnReaderDynamicInstantiationMapImpl(
			List<AliasedReturnReader> argumentReaders,
			int numberOfColumnsConsumed) {
		this.argumentReaders = argumentReaders;
		this.numberOfColumnsConsumed = numberOfColumnsConsumed;
	}

	@Override
	public int getNumberOfColumnsRead(SessionFactoryImplementor sessionFactory) {
		return numberOfColumnsConsumed;
	}

	@Override
	public Class getReturnedJavaType() {
		return Map.class;
	}


	@Override
	public void readBasicValues(
			RowProcessingState processingState,
			ResultSetProcessingOptions options) throws SQLException {
		for ( AliasedReturnReader entryReader : argumentReaders ) {
			entryReader.getReturnReader().readBasicValues( processingState, options );
		}
	}

	@Override
	public void resolveBasicValues(
			RowProcessingState processingState,
			ResultSetProcessingOptions options) throws SQLException {
		for ( AliasedReturnReader entryReader : argumentReaders ) {
			entryReader.getReturnReader().resolveBasicValues( processingState, options );
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public Object assemble(
			RowProcessingState processingState,
			ResultSetProcessingOptions options) throws SQLException {
		final HashMap result = new HashMap();

		for ( AliasedReturnReader entryReader : argumentReaders ) {
			result.put(
					entryReader.getAlias(),
					entryReader.getReturnReader().assemble( processingState, options )
			);
		}

		return result;
	}
}
