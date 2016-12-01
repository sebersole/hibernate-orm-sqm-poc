/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.convert.results.internal;

import java.util.List;

import org.hibernate.sql.ast.expression.Expression;
import org.hibernate.sql.ast.select.SqlSelectionDescriptor;
import org.hibernate.sql.convert.results.spi.ReturnScalar;
import org.hibernate.sql.exec.results.internal.ResolvedReturnScalarImpl;
import org.hibernate.sql.exec.results.spi.ResolvedReturn;
import org.hibernate.type.Type;

/**
 * @author Steve Ebersole
 */
public class ReturnScalarImpl implements ReturnScalar {
	private final Expression selectExpression;
	private final Type type;
	private final String resultVariableName;

	public ReturnScalarImpl(Expression selectExpression, Type type, String resultVariableName) {
		this.selectExpression = selectExpression;
		this.type = type;
		this.resultVariableName = resultVariableName;
	}

	@Override
	public Expression getSelectExpression() {
		return selectExpression;
	}

	@Override
	public String getResultVariableName() {
		return resultVariableName;
	}

	@Override
	public ResolvedReturn resolve(List<SqlSelectionDescriptor> sqlSelectionDescriptors, boolean shallow) {
		return new ResolvedReturnScalarImpl( sqlSelectionDescriptors, getType() );
	}

	@Override
	public Type getType() {
		return type;
	}

}
