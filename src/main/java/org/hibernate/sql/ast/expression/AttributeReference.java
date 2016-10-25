/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.ast.expression;

import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.sql.ast.from.ColumnBinding;
import org.hibernate.persister.common.spi.SingularAttributeImplementor;
import org.hibernate.sql.convert.spi.SqlTreeWalker;
import org.hibernate.sql.exec.results.internal.ReturnReaderScalarImpl;
import org.hibernate.sql.exec.results.spi.ReturnReader;
import org.hibernate.type.Type;

/**
 * @author Steve Ebersole
 */
public class AttributeReference implements DomainReference {
	private final SingularAttributeImplementor referencedAttribute;
	private final ColumnBinding[] columnBindings;

	public AttributeReference(
			SingularAttributeImplementor referencedAttribute,
			ColumnBinding[] columnBindings) {
		this.referencedAttribute = referencedAttribute;
		this.columnBindings = columnBindings;
	}

	public SingularAttributeImplementor getReferencedAttribute() {
		return referencedAttribute;
	}

	@Override
	public Type getType() {
		return referencedAttribute.getOrmType();
	}

	@Override
	public void accept(SqlTreeWalker sqlTreeWalker) {
		sqlTreeWalker.visitAttributeReference( this );
	}

	public ColumnBinding[] getColumnBindings() {
		return columnBindings;
	}

	@Override
	public ReturnReader getReturnReader(int startPosition, boolean shallow, SessionFactoryImplementor sessionFactory) {
		return new ReturnReaderScalarImpl( startPosition, getType() );
	}
}
