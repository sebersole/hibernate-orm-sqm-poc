/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.hibernate.LockOptions;
import org.hibernate.sql.ast.InterpretationOptions;

/**
 * @author Steve Ebersole
 */
public class InterpretationOptionsImpl implements InterpretationOptions {
	private Integer firstRow;
	private Integer maxRows;
	private String comment;
	private List<String> sqlHints;

	private final LockOptions lockOptions = new LockOptions();

	@Override
	public Integer getFirstRow() {
		return firstRow;
	}

	public void setFirstRow(Integer firstRow) {
		this.firstRow = firstRow;
	}

	@Override
	public Integer getMaxRows() {
		return maxRows;
	}

	public void setMaxRows(Integer maxRows) {
		this.maxRows = maxRows;
	}

	@Override
	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	@Override
	public List<String> getSqlHints() {
		return sqlHints == null ? Collections.<String>emptyList() : sqlHints;
	}

	public void addSqlHint(String hint) {
		if ( sqlHints == null ) {
			sqlHints = new ArrayList<String>();
		}
		sqlHints.add( hint );
	}

	@Override
	public LockOptions getLockOptions() {
		return lockOptions;
	}

	public void setLockOptions(LockOptions lockOptions) {
		this.lockOptions.setLockMode( lockOptions.getLockMode() );
		this.lockOptions.setScope( lockOptions.getScope() );
		this.lockOptions.setTimeOut( lockOptions.getTimeOut() );
	}
}
