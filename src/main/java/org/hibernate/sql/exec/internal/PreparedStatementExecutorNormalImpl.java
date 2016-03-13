/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.exec.internal;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.resource.jdbc.spi.LogicalConnectionImplementor;
import org.hibernate.sql.exec.results.spi.ReturnReader;
import org.hibernate.sql.exec.spi.PreparedStatementExecutor;
import org.hibernate.sql.exec.spi.RowTransformer;
import org.hibernate.sql.gen.Return;

/**
 * Normal PreparedStatement execution which:<ol>
 *     <li>calls {@link PreparedStatement#executeQuery()}</li>
 *     <li>immediately reads all the rows in the ResultSet returning a List of the transformed results</li>
 * </ol>
 *
 * @author Steve Ebersole
 */
public class PreparedStatementExecutorNormalImpl<T> implements PreparedStatementExecutor<List<T>, T> {
	/**
	 * Singleton access
	 */
	public static final PreparedStatementExecutorNormalImpl INSTANCE = new PreparedStatementExecutorNormalImpl();

	@Override
	public List<T> execute(
			PreparedStatement ps,
			List<Return> returns,
			RowTransformer<T> rowTransformer,
			SessionImplementor session) throws SQLException {
		final LogicalConnectionImplementor logicalConnection = session.getJdbcCoordinator().getLogicalConnection();

		// Execute the query
		final ResultSet resultSet = ps.executeQuery();
		logicalConnection.getResourceRegistry().register( resultSet, ps );

		try {
			int position = 1;

			final List<T> results = new ArrayList<T>();
			final int returnCount = returns.size();

			while ( resultSet.next() ) {
//			results.add(
//					rowTransformer.transformRow(
//							rowReader.read(.. )
//			)
//			);

				final Object[] row = new Object[returnCount];
				for ( int i = 0; i < returnCount; i++ ) {
					final ReturnReader reader = returns.get( i ).getReturnReader();
					row[i] = reader.readResult(
							resultSet,
							position,
							session,
							null
					);
					position += reader.getNumberOfColumnsRead( session );
				}
				results.add( rowTransformer.transformRow( row ) );
			}

			return results;
		}
		finally {
			logicalConnection.getResourceRegistry().release( resultSet, ps );
			logicalConnection.getResourceRegistry().release( ps );
		}
	}
}
