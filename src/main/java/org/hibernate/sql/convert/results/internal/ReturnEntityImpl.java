/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.convert.results.internal;

import java.util.Map;

import org.hibernate.loader.PropertyPath;
import org.hibernate.persister.common.spi.AttributeDescriptor;
import org.hibernate.persister.entity.spi.ImprovedEntityPersister;
import org.hibernate.sql.NotYetImplementedException;
import org.hibernate.sql.ast.expression.Expression;
import org.hibernate.sql.convert.results.spi.EntityIdentifierReference;
import org.hibernate.sql.convert.results.spi.ReturnEntity;
import org.hibernate.sql.exec.results.process.internal.EntityReturnInitializerImpl;
import org.hibernate.sql.exec.results.process.internal.ReturnAssemblerEntity;
import org.hibernate.sql.exec.results.process.spi2.EntityReferenceInitializer;
import org.hibernate.sql.exec.results.process.spi2.InitializerCollector;
import org.hibernate.sql.exec.results.process.spi2.InitializerParent;
import org.hibernate.sql.exec.results.process.spi2.ReturnAssembler;
import org.hibernate.sql.exec.results.process.spi2.SqlSelectionGroup;

/**
 * @author Steve Ebersole
 */
public class ReturnEntityImpl extends AbstractFetchParent implements ReturnEntity {
	private final Expression expression;
	private final ImprovedEntityPersister entityPersister;
	private final String resultVariable;
	private final Map<AttributeDescriptor, SqlSelectionGroup> sqlSelectionGroupMap;

	private final ReturnAssemblerEntity assembler;
	private final EntityReturnInitializerImpl initializer;

	public ReturnEntityImpl(
			Expression expression,
			ImprovedEntityPersister improvedEntityPersister,
			String resultVariable,
			boolean isShallow,
			Map<AttributeDescriptor, SqlSelectionGroup> sqlSelectionGroupMap,
			PropertyPath propertyPath,
			String tableGroupUid) {
		super( propertyPath, tableGroupUid );
		this.expression = expression;
		this.entityPersister = improvedEntityPersister;
		this.resultVariable = resultVariable;
		this.sqlSelectionGroupMap = sqlSelectionGroupMap;

		this.initializer = new EntityReturnInitializerImpl(
				this,
				sqlSelectionGroupMap,
				isShallow
		);
		assembler = new ReturnAssemblerEntity( this );
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
	public Expression getSelectedExpression() {
		return expression;
	}

	@Override
	public String getResultVariable() {
		return resultVariable;
	}

	@Override
	public Class getReturnedJavaType() {
		return entityPersister.getOrmType().getReturnedClass();
	}

	@Override
	public ReturnAssembler getReturnAssembler() {
		return assembler;
	}

	@Override
	public void registerInitializers(InitializerCollector collector) {
		collector.addInitializer( initializer );
	}

	@Override
	public EntityReferenceInitializer getInitializer() {
		return initializer;
	}

	@Override
	public InitializerParent getInitializerParentForFetchInitializers() {
		return initializer;
	}
}
