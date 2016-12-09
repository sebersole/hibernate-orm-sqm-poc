/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.exec.results.internal;

import java.util.List;

import org.hibernate.engine.FetchStrategy;
import org.hibernate.loader.PropertyPath;
import org.hibernate.persister.common.internal.SingularAttributeEntity;
import org.hibernate.persister.entity.spi.ImprovedEntityPersister;
import org.hibernate.sql.NotYetImplementedException;
import org.hibernate.sql.ast.select.SqlSelectionDescriptor;
import org.hibernate.sql.convert.results.spi.FetchEntityAttribute;
import org.hibernate.sql.exec.results.process.internal.EntityFetchInitializerImpl;
import org.hibernate.sql.exec.results.process.spi2.FetchInitializer;
import org.hibernate.sql.exec.results.process.spi2.InitializerParent;
import org.hibernate.sql.exec.results.spi.ResolvedEntityIdentifierReference;
import org.hibernate.sql.exec.results.spi.ResolvedFetchEntity;
import org.hibernate.sql.exec.results.spi.ResolvedFetchParent;
import org.hibernate.type.Type;

/**
 * @author Steve Ebersole
 */
public class ResolvedFetchEntityImpl
		extends AbstractResolvedFetchParent
		implements ResolvedFetchEntity {
	private final FetchEntityAttribute fetch;
	private final ResolvedFetchParent fetchParent;
	private final FetchStrategy fetchStrategy;

	private final EntityFetchInitializerImpl initializer;

	public ResolvedFetchEntityImpl(
			FetchEntityAttribute fetch,
			ResolvedFetchParent fetchParent,
			FetchStrategy fetchStrategy,
			List<SqlSelectionDescriptor> sqlSelectionDescriptors,
			boolean shallow) {
		this.fetch = fetch;
		this.fetchParent = fetchParent;
		this.fetchStrategy = fetchStrategy;

		this.initializer = new EntityFetchInitializerImpl(
				fetchParent.getInitializerParentForFetchInitializers(),
				this,
				sqlSelectionDescriptors,
				shallow
		);
	}

	@Override
	public SingularAttributeEntity getFetchedAttributeDescriptor() {
		return fetch.getFetchedAttributeDescriptor();
	}

	@Override
	public ImprovedEntityPersister getEntityPersister() {
		return fetch.getEntityPersister();
	}

	@Override
	public ResolvedEntityIdentifierReference getIdentifierReference() {
		throw new NotYetImplementedException(  );
	}

	@Override
	public ResolvedFetchParent getFetchParent() {
		return fetchParent;
	}

	@Override
	public PropertyPath getPropertyPath() {
		return fetch.getPropertyPath();
	}

	@Override
	public FetchStrategy getFetchStrategy() {
		return fetchStrategy;
	}

	@Override
	public Type getFetchedType() {
		return fetch.getFetchedType();
	}

	@Override
	public boolean isNullable() {
		throw new NotYetImplementedException(  );
	}

	@Override
	public FetchInitializer getFetchInitializer(InitializerParent parent) {
		return initializer;
	}

	@Override
	public String getTableGroupUniqueIdentifier() {
		return fetch.getTableGroupUniqueIdentifier();
	}

	@Override
	public InitializerParent getInitializerParentForFetchInitializers() {
		return initializer;
	}
}
