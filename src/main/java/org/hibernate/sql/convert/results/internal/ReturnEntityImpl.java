/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.convert.results.internal;

import java.util.List;

import org.hibernate.loader.PropertyPath;
import org.hibernate.persister.entity.spi.ImprovedEntityPersister;
import org.hibernate.sql.NotYetImplementedException;
import org.hibernate.sql.ast.expression.Expression;
import org.hibernate.sql.ast.select.SqlSelectionDescriptor;
import org.hibernate.sql.convert.results.spi.EntityIdentifierReference;
import org.hibernate.sql.convert.results.spi.ReturnEntity;
import org.hibernate.sql.exec.results.internal.ResolvedReturnEntityImpl;
import org.hibernate.sql.exec.results.spi.ResolvedReturn;

/**
 * @author Steve Ebersole
 */
public class ReturnEntityImpl extends AbstractFetchParent implements ReturnEntity {
	private final Expression selectExpression;
	private final ImprovedEntityPersister entityPersister;
	private final String alias;

	public ReturnEntityImpl(
			PropertyPath propertyPath,
			String tableGroupUid,
			Expression selectExpression,
			ImprovedEntityPersister entityPersister,
			String alias) {
		super( propertyPath, tableGroupUid );
		this.selectExpression = selectExpression;
		this.entityPersister = entityPersister;
		this.alias = alias;
	}

	@Override
	public ImprovedEntityPersister getEntityPersister() {
		return entityPersister;
	}

	@Override
	public EntityIdentifierReference getIdentifierReference() {
		throw new NotYetImplementedException();
	}

	@Override
	public Expression getSelectExpression() {
		return selectExpression;
	}

	@Override
	public String getResultVariableName() {
		return alias;
	}

	@Override
	public ResolvedReturn resolve(
			List<SqlSelectionDescriptor> sqlSelectionDescriptors,
			boolean shallow) {
		// todo : who is responsible for adding the SqlSelectionDescriptor for fetches?

		return new ResolvedReturnEntityImpl(
				getEntityPersister(),
				getPropertyPath(),
				sqlSelectionDescriptors,
				shallow
		);
	}
}
