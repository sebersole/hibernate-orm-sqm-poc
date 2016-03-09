/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.gen;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.hibernate.engine.spi.SessionImplementor;

/**
 * @author Steve Ebersole
 */
public interface Return {
	// proposal for the contract for reading back values
	// todo : maybe a 2-phase approach too
	Object readResult(ResultSet resultSet, int startPosition, SessionImplementor session, Object owner) throws SQLException;
}
