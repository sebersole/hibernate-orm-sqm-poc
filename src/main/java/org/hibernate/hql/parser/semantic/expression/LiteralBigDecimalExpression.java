/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.hql.parser.semantic.expression;

import java.math.BigDecimal;

import org.hibernate.hql.parser.model.StandardBasicTypeDescriptors;
import org.hibernate.hql.parser.model.TypeDescriptor;

/**
 * @author Steve Ebersole
 */
public class LiteralBigDecimalExpression extends AbstractLiteralExpressionImpl<BigDecimal> {
	public LiteralBigDecimalExpression(BigDecimal value) {
		super( value );
	}

	@Override
	public TypeDescriptor getTypeDescriptor() {
		return StandardBasicTypeDescriptors.INSTANCE.BIG_DECIMAL;
	}
}
