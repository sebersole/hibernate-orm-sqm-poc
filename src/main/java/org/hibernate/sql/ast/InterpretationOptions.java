/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.ast;

import java.util.List;

import org.hibernate.LockOptions;

/**
 * @author Steve Ebersole
 */
public interface InterpretationOptions {
	Integer getFirstRow();
	Integer getMaxRows();
	String getComment();
	List<String> getSqlHints();
	LockOptions getLockOptions();
}
