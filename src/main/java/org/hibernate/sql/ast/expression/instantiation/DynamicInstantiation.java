/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.ast.expression.instantiation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.hibernate.sql.ast.expression.Expression;
import org.hibernate.sql.exec.results.spi.ReturnReader;
import org.hibernate.sql.gen.SqlTreeWalker;

import org.jboss.logging.Logger;

/**
 * @author Steve Ebersole
 */
public class DynamicInstantiation<T> implements Expression {
	private static final Logger log = Logger.getLogger( DynamicInstantiation.class );

	private final Class<T> target;
	private List<DynamicInstantiationArgument> arguments;
	boolean areAllArgumentsAliased = true;

	public DynamicInstantiation(Class<T> target) {
		this.target = target;
	}

	public void addArgument(String alias, Expression expression) {
		if ( List.class.equals( target ) ) {
			// really should not have an alias...
			if ( alias != null ) {
				log.debugf(
						"Argument [%s] for dynamic List instantiation declared an 'injection alias' [%s] " +
								"but such aliases are ignored for dynamic List instantiations",
						expression.toString(),
						alias
				);
			}
		}
		else if ( Map.class.equals( target ) ) {
			// must have an alias...
			log.warnf(
					"Argument [%s] for dynamic Map instantiation did not declare an 'injection alias' [%s] " +
							"but such aliases are needed for dynamic Map instantiations; " +
							"will likely cause problems later translating query",
					expression.toString(),
					alias
			);
		}

		areAllArgumentsAliased = areAllArgumentsAliased && alias != null;

		if ( arguments == null ) {
			arguments = new ArrayList<DynamicInstantiationArgument>();
		}
		arguments.add( new DynamicInstantiationArgument( alias, expression ) );
	}

	public List<DynamicInstantiationArgument> getArguments() {
		return arguments;
	}

	@Override
	public String toString() {
		return "DynamicInstantiation(" + target.getName() + ")";
	}

	@Override
	public org.hibernate.type.Type getType() {
		return null;
	}

	@Override
	public void accept(SqlTreeWalker sqlTreeWalker) {
		sqlTreeWalker.visitDynamicInstantiation( this );
	}

	@Override
	@SuppressWarnings("unchecked")
	public ReturnReader getReturnReader() {
		if ( List.class.equals( target ) ) {
			return new ReturnReaderDynamicInstantiationListImpl( arguments );
		}
		else if ( Map.class.equals( target ) ) {
			return new ReturnReaderDynamicInstantiationMapImpl( arguments );
		}
		else {
			return new ReturnReaderDynamicInstantiationClassImpl( target, areAllArgumentsAliased, arguments );
		}
	}
}
