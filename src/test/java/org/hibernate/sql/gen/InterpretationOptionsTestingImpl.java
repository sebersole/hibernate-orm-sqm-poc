/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.gen;

import java.util.List;

import org.hibernate.LockOptions;
import org.hibernate.sql.ast.InterpretationOptions;

/**
 * @author Steve Ebersole
 */
class InterpretationOptionsTestingImpl implements InterpretationOptions {
	@Override
	public Integer getFirstRow() {
		return null;
	}

	@Override
	public Integer getMaxRows() {
		return null;
	}

	@Override
	public String getComment() {
		return null;
	}

	@Override
	public List<String> getSqlHints() {
		return null;
	}

	@Override
	public LockOptions getLockOptions() {
		return null;
	}
}
