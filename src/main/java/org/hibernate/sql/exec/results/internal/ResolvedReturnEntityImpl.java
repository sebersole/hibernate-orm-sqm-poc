/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.exec.results.internal;

import java.util.List;

import org.hibernate.loader.PropertyPath;
import org.hibernate.persister.entity.spi.ImprovedEntityPersister;
import org.hibernate.sql.NotYetImplementedException;
import org.hibernate.sql.ast.select.SqlSelectionDescriptor;
import org.hibernate.sql.exec.results.process.internal.EntityReturnInitializerImpl;
import org.hibernate.sql.exec.results.process.internal.ReturnAssemblerEntity;
import org.hibernate.sql.exec.results.process.spi2.EntityReferenceInitializer;
import org.hibernate.sql.exec.results.process.spi2.InitializerParent;
import org.hibernate.sql.exec.results.process.spi2.ReturnAssembler;
import org.hibernate.sql.exec.results.spi.ResolvedEntityIdentifierReference;
import org.hibernate.sql.exec.results.spi.ResolvedReturnEntity;

/**
 * @author Steve Ebersole
 */
public class ResolvedReturnEntityImpl extends AbstractResolvedFetchParent implements ResolvedReturnEntity {
	private final ImprovedEntityPersister entityPersister;
	private final PropertyPath propertyPath;
	private final List<SqlSelectionDescriptor> sqlSelectionDescriptors;
	private final boolean shallow;

	private final EntityReturnInitializerImpl initializer;

	public ResolvedReturnEntityImpl(
			ImprovedEntityPersister entityPersister,
			PropertyPath propertyPath,
			List<SqlSelectionDescriptor> sqlSelectionDescriptors,
			boolean shallow) {
		this.entityPersister = entityPersister;
		this.propertyPath = propertyPath;
		this.sqlSelectionDescriptors = sqlSelectionDescriptors;
		this.shallow = shallow;

		this.initializer = buildInitializer();
	}

	@Override
	public EntityReferenceInitializer getInitializer() {
		if ( initializer == null ) {
		}
		return initializer;
	}

	private EntityReturnInitializerImpl buildInitializer() {
		return new EntityReturnInitializerImpl(
				this,
				isShallow()
		);
	}

	public boolean isShallow() {
		return shallow;
	}

	@Override
	public int getNumberOfSelectablesConsumed() {
		return sqlSelectionDescriptors.size();
	}

	@Override
	public List<SqlSelectionDescriptor> getSqlSelectionDescriptors() {
		return sqlSelectionDescriptors;
	}

	@Override
	public ReturnAssembler getReturnAssembler() {
		return new ReturnAssemblerEntity( this );
	}

	@Override
	public Class getReturnedJavaType() {
		return entityPersister.getOrmType().getReturnedClass();
	}

	@Override
	public PropertyPath getPropertyPath() {
		return propertyPath;
	}

	@Override
	public String getTableGroupUniqueIdentifier() {
		// we'd have to pass in the TableGroup or some way to locate it, but
		// 		not sure this is needed anymore based on the collection of
		// 		Initializers as an spi item - #collectInitializers
		throw new NotYetImplementedException(  );
	}

	@Override
	public InitializerParent getInitializerParentForFetchInitializers() {
		return initializer;
	}

	@Override
	public ImprovedEntityPersister getEntityPersister() {
		return entityPersister;
	}

	@Override
	public ResolvedEntityIdentifierReference getIdentifierReference() {
		throw new NotYetImplementedException();
	}
}
