/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.exec.results.process.internal;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.internal.util.collections.streams.GenericArrayCollector;
import org.hibernate.loader.spi.AfterLoadAction;
import org.hibernate.sql.ast.select.SqlSelectionDescriptor;
import org.hibernate.sql.convert.ConversionException;
import org.hibernate.sql.exec.internal.RecommendedJdbcTypeMappings;
import org.hibernate.sql.exec.results.process.spi2.QueryCacheDataAccess;
import org.hibernate.sql.exec.results.process.spi.ResultSetProcessingOptions;
import org.hibernate.sql.exec.results.process.spi.ResultSetProcessingState;
import org.hibernate.sql.exec.results.process.spi.RowProcessingState;
import org.hibernate.sql.exec.results.process.spi.RowReader;
import org.hibernate.sql.exec.results.process.spi2.ReturnAssembler;
import org.hibernate.sql.exec.results.spi.ResolvedReturn;
import org.hibernate.sql.exec.spi.RowTransformer;
import org.hibernate.type.descriptor.java.JavaTypeDescriptor;
import org.hibernate.type.descriptor.java.JavaTypeDescriptorRegistry;

/**
 * @author Steve Ebersole
 */
public class RowReaderStandardImpl<T> implements RowReader<T> {
	private final RowTransformer<T> rowTransformer;
	private final QueryCacheDataAccess queryCacheDataAccess;

	private final SqlSelectionDescriptor[] sqlSelectionDescriptors;
	private final ReturnAssembler[] returnAssemblers;

	public RowReaderStandardImpl(
			List<ResolvedReturn> returns,
			QueryCacheDataAccess queryCacheDataAccess,
			RowTransformer<T> rowTransformer) {
		this.rowTransformer = rowTransformer;
		this.queryCacheDataAccess = queryCacheDataAccess;

		final int count = returns.size();
		final ReturnAssembler[] returnReaders = new ReturnAssembler[ count ];
		final ArrayList<SqlSelectionDescriptor> selectionDescriptorList = new ArrayList<>();

		for ( int i = 0; i < count; i++ ) {
			selectionDescriptorList.addAll( returns.get( i ).getSqlSelectionDescriptors() );
			returnReaders[i] = returns.get( i ).getReturnAssembler();
		}

		this.returnAssemblers = returnReaders;
		this.sqlSelectionDescriptors = selectionDescriptorList.stream().collect( new GenericArrayCollector<>( SqlSelectionDescriptor.class ) );
	}

	@Override
	public T readRow(RowProcessingState rowProcessingState, ResultSetProcessingOptions options) throws SQLException {
		// NOTE : atm we only support reading scalar values...
		// todo : support other stuff ^^


		// finally assemble the results
		final int returnCount = returnAssemblers.length;
		final Object[] result = new Object[returnCount];

		for ( int i = 0; i < returnCount; i++ ) {
			result[i] = returnAssemblers[i].assemble( rowProcessingState, options );
		}

		return rowTransformer.transformRow( result );
	}

	@Override
	public void finishUp(ResultSetProcessingState context, List<AfterLoadAction> afterLoadActionList) {
	}
}
