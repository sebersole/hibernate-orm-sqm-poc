/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.internal;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.sql.exec.spi.RowTransformer;
import org.hibernate.sql.gen.Return;

/**
 * @author Steve Ebersole
 */
public interface ResultSetConsumer<T,R> {
	T consume(
			PreparedStatement ps,
			ResultSet resultSet,
			List<Return> returns,
			RowTransformer<R> rowTransformer,
			SessionImplementor session) throws SQLException;
}
