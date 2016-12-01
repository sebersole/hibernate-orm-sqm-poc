/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.exec.results.process.internal;

import java.sql.SQLException;

import org.hibernate.EntityMode;
import org.hibernate.sql.exec.results.process.spi.ResultSetProcessingOptions;
import org.hibernate.sql.exec.results.process.spi.RowProcessingState;
import org.hibernate.sql.exec.results.process.spi2.ReturnAssembler;
import org.hibernate.sql.exec.results.spi.ResolvedReturnScalar;
import org.hibernate.type.CompositeType;

/**
 * @author Steve Ebersole
 */
public class ReturnAssemblerScalar implements ReturnAssembler {
	private final ResolvedReturnScalar resolvedReturn;

	public ReturnAssemblerScalar(ResolvedReturnScalar resolvedReturn) {
		this.resolvedReturn = resolvedReturn;
	}

	@Override
	public Class getReturnedJavaType() {
		return resolvedReturn.getReturnedJavaType();
	}

	@Override
	public Object assemble(
			RowProcessingState rowProcessingState,
			ResultSetProcessingOptions options) throws SQLException {

		// NOTE : atm we only support reading scalar values.  Further we assume that the
		//		jdbcValue is already the correct type.

		final int selectionSpan = resolvedReturn.getNumberOfSelectablesConsumed();


		if ( selectionSpan > 1 ) {
			// has to be a CompositeType for now (and a very basic, one-level one)...
			final CompositeType ctype = (CompositeType) resolvedReturn.getType();
			final Object[] values = new Object[ selectionSpan ];
			for ( int i = 0; i < selectionSpan; i++ ) {
				values[i] = rowProcessingState.getJdbcValues()[
						resolvedReturn.getSqlSelectionDescriptors().get( i ).getPosition()
				];
			}
			try {
				final Object result = ctype.getReturnedClass().newInstance();
				ctype.setPropertyValues( result, values, EntityMode.POJO );
				return result;
			}
			catch (Exception e) {
				throw new RuntimeException( "Unable to instantiate composite : " +  ctype.getReturnedClass().getName(), e );
			}
		}
		else {
			return rowProcessingState.getJdbcValues()[
					resolvedReturn.getSqlSelectionDescriptors().get( 0 ).getPosition()
			];
		}
	}
}
