/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.exec.results.internal;

import org.hibernate.engine.FetchStrategy;
import org.hibernate.engine.FetchStyle;
import org.hibernate.engine.FetchTiming;
import org.hibernate.loader.PropertyPath;
import org.hibernate.sql.exec.results.process.internal.CompositeReferenceInitializerImpl;
import org.hibernate.sql.exec.results.process.spi2.CompositeReferenceInitializer;
import org.hibernate.sql.exec.results.process.spi2.InitializerParent;
import org.hibernate.sql.exec.results.spi.ResolvedFetchComposite;
import org.hibernate.sql.exec.results.spi.ResolvedFetchParent;
import org.hibernate.type.CompositeType;
import org.hibernate.type.Type;

/**
 * @author Steve Ebersole
 */
public class ResolvedFetchCompositeImpl
		extends AbstractResolvedFetchParent
		implements ResolvedFetchComposite {
	private static final FetchStrategy FETCH_STRATEGY = new FetchStrategy(
			FetchTiming.IMMEDIATE,
			FetchStyle.JOIN
	);

	private final ResolvedFetchParent resolvedFetchParent;
	private final PropertyPath propertyPath;
	private final CompositeType compositeType;
	private final String tableGroupUid;

	private final CompositeReferenceInitializer initializer;

	public ResolvedFetchCompositeImpl(
			ResolvedFetchParent resolvedFetchParent,
			PropertyPath propertyPath,
			CompositeType compositeType, String tableGroupUid) {
		this.resolvedFetchParent = resolvedFetchParent;
		this.propertyPath = propertyPath;
		this.compositeType = compositeType;
		this.tableGroupUid = tableGroupUid;

		this.initializer = new CompositeReferenceInitializerImpl(
				resolvedFetchParent.getInitializerParentForFetchInitializers()
		);
	}

	@Override
	public String getTableGroupUniqueIdentifier() {
		return tableGroupUid;
	}

	@Override
	public ResolvedFetchParent getFetchParent() {
		return resolvedFetchParent;
	}

	@Override
	public PropertyPath getPropertyPath() {
		return propertyPath;
	}

	@Override
	public FetchStrategy getFetchStrategy() {
		return FETCH_STRATEGY;
	}

	@Override
	public Type getFetchedType() {
		return compositeType;
	}

	@Override
	public boolean isNullable() {
		return false;
	}

	@Override
	public CompositeReferenceInitializer getFetchInitializer(InitializerParent parent) {
		return new CompositeReferenceInitializerImpl( parent );
	}

	@Override
	public InitializerParent getInitializerParentForFetchInitializers() {
		return initializer;
	}
}
