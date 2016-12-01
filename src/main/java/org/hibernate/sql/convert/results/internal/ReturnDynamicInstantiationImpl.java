/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.convert.results.internal;

import java.util.List;

import org.hibernate.sql.ast.expression.instantiation.DynamicInstantiation;
import org.hibernate.sql.ast.select.SqlSelectionDescriptor;
import org.hibernate.sql.convert.results.spi.ReturnDynamicInstantiation;
import org.hibernate.sql.exec.results.internal.instantiation.ResolvedReturnDynamicInstantiationImpl;
import org.hibernate.sql.exec.results.spi.ResolvedReturn;

/**
 * @author Steve Ebersole
 */
public class ReturnDynamicInstantiationImpl implements ReturnDynamicInstantiation {
	private final DynamicInstantiation selectExpression;
	private final Class target;
	private final String resultVariableName;

	public ReturnDynamicInstantiationImpl(DynamicInstantiation selectExpression, String resultVariableName) {
		this.selectExpression = selectExpression;
		this.target = selectExpression.getTarget();
		this.resultVariableName = resultVariableName;
	}

	@Override
	public DynamicInstantiation getSelectExpression() {
		return selectExpression;
	}

	@Override
	public Class getInstantiationTarget() {
		return target;
	}

	@Override
	public String getResultVariableName() {
		return resultVariableName;
	}

	@Override
	public ResolvedReturn resolve(List<SqlSelectionDescriptor> sqlSelectionDescriptors, boolean shallow) {
		return new ResolvedReturnDynamicInstantiationImpl( target );
	}
}
