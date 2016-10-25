/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.sql.ast.expression;

import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.persister.entity.spi.ImprovedEntityPersister;
import org.hibernate.sql.ast.from.AbstractTableGroup;
import org.hibernate.sql.ast.from.ColumnBinding;
import org.hibernate.sql.ast.from.TableBinding;
import org.hibernate.sql.convert.spi.SqlTreeWalker;
import org.hibernate.sql.exec.results.internal.ReturnReaderScalarImpl;
import org.hibernate.sql.exec.results.spi.ReturnReader;
import org.hibernate.type.Type;

/**
 * @author Andrea Boriero
 * @author Steve Ebersole
 */
public class EntityReference implements DomainReference {
	private final AbstractTableGroup tableGroup;
	private final ImprovedEntityPersister improvedEntityPersister;
	private final TableBinding tableBinding;

	public EntityReference(
			AbstractTableGroup tableGroup,
			ImprovedEntityPersister improvedEntityPersister,
			TableBinding tableBinding) {

		this.tableGroup = tableGroup;
		this.improvedEntityPersister = improvedEntityPersister;
		this.tableBinding = tableBinding;
	}

	public ImprovedEntityPersister getImprovedEntityPersister() {
		return improvedEntityPersister;
	}

	public AbstractTableGroup getTableGroup() {
		return tableGroup;
	}

	public TableBinding getTableBinding() {
		return tableBinding;
	}

	@Override
	public Type getType() {
		return improvedEntityPersister.getOrmType();
	}

	@Override
	public ReturnReader getReturnReader(
			int startPosition,
			boolean shallow,
			SessionFactoryImplementor sessionFactory) {
		if ( shallow ) {
			return new ReturnReaderScalarImpl( startPosition, getType() );
		}

		return new ReturnReaderScalarImpl( startPosition, getType() );
	}

	@Override
	public void accept(SqlTreeWalker sqlTreeWalker) {
		sqlTreeWalker.visitEntityExpression( this );
	}
}
