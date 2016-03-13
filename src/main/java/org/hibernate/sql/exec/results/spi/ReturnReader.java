/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.exec.results.spi;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.hibernate.Incubating;
import org.hibernate.engine.spi.SessionImplementor;

/**
 * Contract for reading an individual return (selection) from the underlying ResultSet
 *
 * @author Steve Ebersole
 */
@Incubating
public interface ReturnReader {
	// proposal for the contract for reading back values
	// todo : maybe a 2-phase approach too
	Object readResult(ResultSet resultSet, int startPosition, SessionImplementor session, Object owner) throws SQLException;
	int getNumberOfColumnsRead(SessionImplementor session);
}
