/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.exec.spi;

import java.util.List;

import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.query.QueryParameterBindings;
import org.hibernate.sql.ast.InterpretationOptions;
import org.hibernate.sql.ast.SelectQuery;
import org.hibernate.sql.gen.Callback;
import org.hibernate.sqm.query.NonSelectStatement;
import org.hibernate.sqm.query.SelectStatement;

/**
 * @author Steve Ebersole
 */
public interface SemanticQueryExecutor {
	<T> List<T> executeSelect(
			SelectStatement sqm,
			InterpretationOptions interpretationOptions,
			ExecutionOptions executionOptions,
			QueryParameterBindings queryParameterBindings,
			RowTransformer<T> rowTransformer,
			Callback callback,
			SessionImplementor session);

	<T> ScrollableResults executeSelect(
			SelectStatement sqm,
			ScrollMode scrollMode,
			InterpretationOptions interpretationOptions,
			ExecutionOptions executionOptions,
			QueryParameterBindings queryParameterBindings,
			RowTransformer<T> rowTransformer,
			Callback callback,
			SessionImplementor session);

	<T> List<T> executeSelect(
			SelectQuery sqlTree,
			ExecutionOptions executionOptions,
			QueryParameterBindings queryParameterBindings,
			RowTransformer<T> rowTransformer,
			SessionImplementor session);

	int executeDml(
			NonSelectStatement sqm,
			InterpretationOptions interpretationOptions,
			ExecutionOptions executionOptions,
			SessionImplementor session);
}
