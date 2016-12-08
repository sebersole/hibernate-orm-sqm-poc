/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.exec.results.process.internal;

import java.sql.SQLException;

import org.hibernate.sql.NotYetImplementedException;
import org.hibernate.sql.exec.results.internal.ResolvedReturnEntityImpl;
import org.hibernate.sql.exec.results.process.spi.ResultSetProcessingOptions;
import org.hibernate.sql.exec.results.process.spi.RowProcessingState;
import org.hibernate.sql.exec.results.process.spi2.ReturnAssembler;

/**
 * @author Steve Ebersole
 */
public class ReturnAssemblerEntity implements ReturnAssembler {
	private final ResolvedReturnEntityImpl resolvedReturnEntity;

	public ReturnAssemblerEntity(ResolvedReturnEntityImpl resolvedReturnEntity) {
		this.resolvedReturnEntity = resolvedReturnEntity;
	}

	@Override
	public Class getReturnedJavaType() {
		return resolvedReturnEntity.getEntityPersister().getOrmType().getReturnedClass();
	}

	@Override
	public Object assemble(RowProcessingState rowProcessingState, ResultSetProcessingOptions options) throws SQLException {
		throw new NotYetImplementedException(  );
	}
}
