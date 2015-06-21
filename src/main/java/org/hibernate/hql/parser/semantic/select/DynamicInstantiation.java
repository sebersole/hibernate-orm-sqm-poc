/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.hql.parser.semantic.select;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.hibernate.hql.parser.ParsingContext;
import org.hibernate.hql.parser.model.AttributeDescriptor;
import org.hibernate.hql.parser.model.BasicTypeDescriptor;
import org.hibernate.hql.parser.model.TypeDescriptor;
import org.hibernate.hql.parser.semantic.expression.Expression;

/**
 * @author Steve Ebersole
 */
public class DynamicInstantiation implements Selection, Expression {
	private final ParsingContext parsingContext;
	private final String instantiationTarget;
	private final BasicTypeDescriptor typeDescriptor;

	private List<AliasedDynamicInstantiationArgument> aliasedArguments;

	public DynamicInstantiation(ParsingContext parsingContext, final String instantiationTarget) {
		this.parsingContext = parsingContext;
		this.instantiationTarget = instantiationTarget;

		this.typeDescriptor = new BasicTypeDescriptor() {

			@Override
			public String getTypeName() {
				return instantiationTarget;
			}

			@Override
			public AttributeDescriptor getAttributeDescriptor(String attributeName) {
				return null;
			}
		};
	}

	public DynamicInstantiation(
			ParsingContext parsingContext,
			final String instantiationTarget,
			List<AliasedDynamicInstantiationArgument> aliasedArguments) {
		this.parsingContext = parsingContext;
		this.instantiationTarget = instantiationTarget;
		this.aliasedArguments = aliasedArguments;

		this.typeDescriptor = new BasicTypeDescriptor() {

			@Override
			public String getTypeName() {
				return instantiationTarget;
			}

			@Override
			public AttributeDescriptor getAttributeDescriptor(String attributeName) {
				return null;
			}
		};
	}

	public DynamicInstantiation(
			ParsingContext parsingContext,
			String instantiationTarget,
			AliasedDynamicInstantiationArgument... aliasedArguments) {
		this(
				parsingContext,
				instantiationTarget,
				Arrays.asList( aliasedArguments )
		);
	}

	@Override
	public TypeDescriptor getTypeDescriptor() {
		return typeDescriptor;
	}

	public String getInstantiationTarget() {
		return instantiationTarget;
	}

	public List<AliasedDynamicInstantiationArgument> getAliasedArguments() {
		return aliasedArguments;
	}

	public void addArgument(AliasedDynamicInstantiationArgument argument) {
		if ( aliasedArguments == null ) {
			aliasedArguments = new ArrayList<AliasedDynamicInstantiationArgument>();
		}
		aliasedArguments.add( argument );
	}

	public void addArgument(Expression argument, String alias) {
		addArgument( new AliasedDynamicInstantiationArgument( argument, alias ) );
	}

	public void addArgument(Expression argument) {
		addArgument( argument, null );
	}
}
