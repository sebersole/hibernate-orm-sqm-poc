/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.ast.expression.domain;

import java.util.List;

import org.hibernate.loader.PropertyPath;
import org.hibernate.persister.entity.spi.ImprovedEntityPersister;
import org.hibernate.sql.ast.from.ColumnBinding;
import org.hibernate.sql.convert.results.internal.ReturnEntityImpl;
import org.hibernate.sql.convert.results.spi.Return;
import org.hibernate.sql.exec.spi.SqlAstSelectInterpreter;
import org.hibernate.type.Type;

/**
 * @author Andrea Boriero
 * @author Steve Ebersole
 */
public class EntityReferenceExpression implements DomainReferenceExpression {
	private final ColumnBindingSource columnBindingSource;
	private final ImprovedEntityPersister improvedEntityPersister;
	private final PropertyPath propertyPath;

	public EntityReferenceExpression(
			ColumnBindingSource columnBindingSource,
			ImprovedEntityPersister improvedEntityPersister,
			PropertyPath propertyPath) {
		this.columnBindingSource = columnBindingSource;
		this.improvedEntityPersister = improvedEntityPersister;
		this.propertyPath = propertyPath;
	}

	public ImprovedEntityPersister getImprovedEntityPersister() {
		return improvedEntityPersister;
	}

	public ColumnBindingSource getColumnBindingSource() {
		return columnBindingSource;
	}

	@Override
	public Type getType() {
		return improvedEntityPersister.getOrmType();
	}

	@Override
	public Return toQueryReturn(String resultVariable) {
		return new ReturnEntityImpl(
				propertyPath,
				columnBindingSource.getTableGroup().getUid(),
				this,
				improvedEntityPersister,
				resultVariable
		);
	}

	@Override
	public List<ColumnBinding> resolveColumnBindings(boolean shallow) {
		return columnBindingSource.resolveColumnBindings( this, shallow );
	}

	@Override
	public PropertyPath getPropertyPath() {
		return propertyPath;
	}

	@Override
	public void accept(SqlAstSelectInterpreter walker, boolean shallow) {
		walker.visitEntityExpression( this, shallow );
	}

	@Override
	public ImprovedEntityPersister getDomainReference() {
		return getImprovedEntityPersister();
	}
}
