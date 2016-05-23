/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.proposed.internal;

import java.util.Set;

import org.hibernate.query.proposed.QueryParameter;
import org.hibernate.query.proposed.spi.QueryPlan;
import org.hibernate.sqm.ConsumerContext;
import org.hibernate.sqm.SemanticQueryInterpreter;
import org.hibernate.sqm.query.Statement;

/**
 * @author Steve Ebersole
 */
public class QueryPlanImpl implements QueryPlan {
	static QueryPlanImpl prepare(String queryString, ConsumerContext consumerContext) {
		Statement sqm = SemanticQueryInterpreter.interpret(
				queryString,
				consumerContext
		);

		// todo : reconsider this aspect of building QueryParameterBindings
		//		the concern here is performance only.  What happens is that we end up walking the
		//		SQM specifically to extract query parameter information to prepare the bindings
		//		container.  The more times we walk the tree, the worse the performance.  So while
		//		this approach for collecting the query parameters is natural, we do need to keep an
		//		eye on the performance.
		//
		//		An alternative would be to define an `ExpressionListener` contract in SQM that
		//		we could pass into the semantic analysis.  The idea being that as the SQM is built
		//		we would call out the the `ExpressionListener` for any Expression objects built.
		//		We could also generalize this to apply as a specialized case of `SemanticQueryWalker`
		//		for consumers of the SQM
		Set<QueryParameter> queryParameters = QueryParameterExtractor.getQueryParameters( sqm );

		return new QueryPlanImpl( sqm, queryParameters );
	}

	private final Statement sqm;
	private final Set<QueryParameter> queryParameters;

	public QueryPlanImpl(Statement sqm, Set<QueryParameter> queryParameters) {
		this.sqm = sqm;
		this.queryParameters = queryParameters;
	}

	@Override
	public Statement getSqm() {
		return sqm;
	}

	@Override
	public Set<QueryParameter> getQueryParameters() {
		return queryParameters;
	}
}
