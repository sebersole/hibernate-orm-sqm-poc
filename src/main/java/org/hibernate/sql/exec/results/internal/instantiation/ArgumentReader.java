/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.exec.results.internal.instantiation;

import java.sql.SQLException;

import org.hibernate.sql.exec.results.process.spi.ResultSetProcessingOptions;
import org.hibernate.sql.exec.results.process.spi.RowProcessingState;
import org.hibernate.sql.exec.results.process.spi2.ReturnAssembler;

/**
 * @author Steve Ebersole
 */
class ArgumentReader implements ReturnAssembler {
	private final String alias;

	private final ReturnAssembler returnAssembler;

	public ArgumentReader(String alias, ReturnAssembler returnAssembler) {
		this.alias = alias;
		this.returnAssembler = returnAssembler;
	}

	public String getAlias() {
		return alias;
	}

	@Override
	public Object assemble(RowProcessingState rowProcessingState, ResultSetProcessingOptions options) throws SQLException {
		return returnAssembler.assemble( rowProcessingState, options );
	}

	public Class getReturnedJavaType() {
		return returnAssembler.getReturnedJavaType();
	}
}
