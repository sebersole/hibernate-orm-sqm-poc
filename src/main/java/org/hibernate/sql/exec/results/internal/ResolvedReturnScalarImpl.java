/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.exec.results.internal;

import java.util.List;

import org.hibernate.sql.ast.select.SqlSelectionDescriptor;
import org.hibernate.sql.exec.results.process.internal.ReturnAssemblerScalar;
import org.hibernate.sql.exec.results.process.spi2.ReturnAssembler;
import org.hibernate.sql.exec.results.spi.ResolvedReturnScalar;
import org.hibernate.type.Type;

/**
 * @author Steve Ebersole
 * @author Gail Badner
 */
public class ResolvedReturnScalarImpl implements ResolvedReturnScalar {
	private final List<SqlSelectionDescriptor> sqlSelectionDescriptors;
	private final Type returnType;

	private final ReturnAssemblerScalar assembler;

	public ResolvedReturnScalarImpl(List<SqlSelectionDescriptor> sqlSelectionDescriptors, Type returnType) {
		this.sqlSelectionDescriptors = sqlSelectionDescriptors;
		this.returnType = returnType;

		if ( returnType == null ) {
			throw new IllegalArgumentException( "Passed `returnType` cannot be null" );
		}

		this.assembler = new ReturnAssemblerScalar( this );
	}

	@Override
	public Type getType() {
		return returnType;
	}

	@Override
	public int getNumberOfSelectablesConsumed() {
		return getSqlSelectionDescriptors().size();
	}

	@Override
	public List<SqlSelectionDescriptor> getSqlSelectionDescriptors() {
		return sqlSelectionDescriptors;
	}

	@Override
	public ReturnAssembler getReturnAssembler() {
		return assembler;
	}

	@Override
	public Class getReturnedJavaType() {
		return getType().getReturnedClass();
	}
}
