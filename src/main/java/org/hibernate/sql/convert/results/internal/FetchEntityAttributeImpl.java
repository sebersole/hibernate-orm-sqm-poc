/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.convert.results.internal;

import java.util.List;

import org.hibernate.engine.FetchStrategy;
import org.hibernate.loader.PropertyPath;
import org.hibernate.persister.common.internal.SingularAttributeEntity;
import org.hibernate.persister.entity.spi.ImprovedEntityPersister;
import org.hibernate.sql.NotYetImplementedException;
import org.hibernate.sql.ast.select.SqlSelectionDescriptor;
import org.hibernate.sql.convert.results.spi.EntityIdentifierReference;
import org.hibernate.sql.convert.results.spi.FetchEntityAttribute;
import org.hibernate.sql.convert.results.spi.FetchParent;
import org.hibernate.sql.exec.results.internal.ResolvedFetchEntityImpl;
import org.hibernate.sql.exec.results.spi.ResolvedFetch;
import org.hibernate.sql.exec.results.spi.ResolvedFetchParent;
import org.hibernate.type.Type;

/**
 * @author Steve Ebersole
 */
public class FetchEntityAttributeImpl extends AbstractFetchParent implements FetchEntityAttribute {
	private final FetchParent fetchParent;
	private final SingularAttributeEntity fetchedAttribute;
	private final ImprovedEntityPersister entityPersister;
	private final FetchStrategy fetchStrategy;

	public FetchEntityAttributeImpl(
			FetchParent fetchParent,
			PropertyPath propertyPath,
			String tableGroupUid,
			SingularAttributeEntity fetchedAttribute,
			ImprovedEntityPersister entityPersister, FetchStrategy fetchStrategy) {
		super( propertyPath, tableGroupUid );
		this.fetchParent = fetchParent;
		this.fetchedAttribute = fetchedAttribute;
		this.entityPersister = entityPersister;
		this.fetchStrategy = fetchStrategy;
	}

	@Override
	public FetchParent getFetchParent() {
		return fetchParent;
	}

	@Override
	public SingularAttributeEntity getFetchedAttributeDescriptor() {
		return fetchedAttribute;
	}

	@Override
	public FetchStrategy getFetchStrategy() {
		return fetchStrategy;
	}

	@Override
	public Type getFetchedType() {
		return fetchedAttribute.getOrmType();
	}

	@Override
	public boolean isNullable() {
		return fetchedAttribute.isNullable();
	}

	@Override
	public ResolvedFetch resolve(
			ResolvedFetchParent resolvedFetchParent,
			List<SqlSelectionDescriptor> sqlSelectionDescriptors,
			boolean shallow) {
		return new ResolvedFetchEntityImpl(
				this,
				resolvedFetchParent,
				fetchStrategy,
				sqlSelectionDescriptors,
				shallow
		);
	}

	@Override
	public ImprovedEntityPersister getEntityPersister() {
		return entityPersister;
	}

	@Override
	public EntityIdentifierReference getIdentifierReference() {
		throw new NotYetImplementedException(  );
	}
}
