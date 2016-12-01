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

import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.internal.util.collections.CollectionHelper;
import org.hibernate.sql.exec.results.process.spi.ResultSetProcessingOptions;
import org.hibernate.sql.exec.results.process.spi.ReturnReader;
import org.hibernate.sql.exec.results.process.spi.RowProcessingState;

import org.jboss.logging.Logger;

/**
 * @author Steve Ebersole
 */
public class ReturnReaderDynamicInstantiationListImpl implements ReturnReader<List> {
	private static final Logger log = Logger.getLogger( ReturnReaderDynamicInstantiationListImpl.class );

	private final List<ReturnReader> argumentReaders;
	private final int numberOfColumnsConsumed;

	public ReturnReaderDynamicInstantiationListImpl(
			List<AliasedReturnReader> aliasedArgumentReaders,
			int numberOfColumnsConsumed) {
		this.numberOfColumnsConsumed = numberOfColumnsConsumed;

		final List<ReturnReader> argumentReaders = CollectionHelper.arrayList( aliasedArgumentReaders.size() );
		for ( AliasedReturnReader aliasedArgumentReader : aliasedArgumentReaders ) {
			if ( StringHelper.isNotEmpty( aliasedArgumentReader.getAlias() ) ) {
				log.debugf( "Argument for list dynamic instantiation (`new list(...)`) specified alias, ignoring" );
			}
			argumentReaders.add( aliasedArgumentReader.getReturnReader() );
		}

		this.argumentReaders = argumentReaders;
	}

	@Override
	public Class<List> getReturnedJavaType() {
		return List.class;
	}

	@Override
	public int getNumberOfColumnsRead(SessionFactoryImplementor sessionFactory) {
		return numberOfColumnsConsumed;
	}

	@Override
	public void readBasicValues(
			RowProcessingState processingState,
			ResultSetProcessingOptions options) throws SQLException {
		for ( ReturnReader argumentReader : argumentReaders ) {
			argumentReader.readBasicValues( processingState, options );
		}
	}

	@Override
	public void resolveBasicValues(
			RowProcessingState processingState,
			ResultSetProcessingOptions options) throws SQLException {
		for ( ReturnReader argumentReader : argumentReaders ) {
			argumentReader.resolveBasicValues( processingState, options );
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public List assemble(
			RowProcessingState processingState, ResultSetProcessingOptions options) throws SQLException {
		final ArrayList result = new ArrayList();
		for ( ReturnReader argumentReader : argumentReaders ) {
			result.add( argumentReader.assemble( processingState, options ) );
		}
		return result;
	}
}
