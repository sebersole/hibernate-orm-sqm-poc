/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.hql.parser;

import org.hibernate.hql.parser.process.ImplicitAliasGenerator;
import org.hibernate.hql.parser.process.ParsingContext;

/**
 * @author Steve Ebersole
 */
class ParsingContextTestingImpl implements ParsingContext {
	private final ConsumerContextTestingImpl consumerContext = new ConsumerContextTestingImpl();
	private final ImplicitAliasGenerator implicitAliasGenerator = new ImplicitAliasGenerator();

	@Override
	public ConsumerContext getConsumerContext() {
		return consumerContext;
	}

	@Override
	public ImplicitAliasGenerator getImplicitAliasGenerator() {
		return implicitAliasGenerator;
	}
}
