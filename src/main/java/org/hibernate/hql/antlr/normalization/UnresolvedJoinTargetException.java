/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.hql.antlr.normalization;

import org.hibernate.hql.SemanticException;

/**
 * @author Steve Ebersole
 */
public class UnresolvedJoinTargetException extends SemanticException {
	private final String joinTarget;

	public static UnresolvedJoinTargetException forJoinTarget(String joinTarget) {
		return new UnresolvedJoinTargetException(
				"Could not interpret join target [" + joinTarget + "]" ,
				joinTarget
		);
	}

	public UnresolvedJoinTargetException(String message, String joinTarget) {
		super( message );
		this.joinTarget = joinTarget;
	}

	public UnresolvedJoinTargetException(String message, Throwable cause, String joinTarget) {
		super( message, cause );
		this.joinTarget = joinTarget;
	}

	public String getJoinTarget() {
		return joinTarget;
	}
}
