/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql;

import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.query.proposed.spi.QueryParameterBindingTypeResolver;
import org.hibernate.type.Type;

/**
 * @author Steve Ebersole
 */
public class QueryParameterBindingTypeResolverImpl implements QueryParameterBindingTypeResolver {
	private final SessionFactoryImplementor factory;

	public QueryParameterBindingTypeResolverImpl(SessionFactoryImplementor factory) {
		this.factory = factory;
	}

	@Override
	public Type resolveParameterBindType(Object bindValue) {
		throw new NotYetImplementedException();
	}

	@Override
	public Type resolveParameterBindType(Class clazz) {
		throw new NotYetImplementedException();
	}
}
