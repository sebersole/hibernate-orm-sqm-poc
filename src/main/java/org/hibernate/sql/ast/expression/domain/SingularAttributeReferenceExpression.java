/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.ast.expression.domain;

import java.util.List;

import org.hibernate.persister.common.spi.DomainDescriptor;
import org.hibernate.persister.common.spi.SingularAttributeDescriptor;
import org.hibernate.sql.ast.from.ColumnBinding;
import org.hibernate.sql.convert.results.internal.ReturnScalarImpl;
import org.hibernate.sql.convert.results.spi.Return;
import org.hibernate.sql.convert.spi.NotYetImplementedException;
import org.hibernate.sql.exec.spi.SqlAstSelectInterpreter;
import org.hibernate.type.Type;

/**
 * @author Steve Ebersole
 */
public class SingularAttributeReferenceExpression implements DomainReferenceExpression {
	private final ColumnBindingSource columnBindingSource;
	private final SingularAttributeDescriptor referencedAttribute;

	public SingularAttributeReferenceExpression(
			ColumnBindingSource columnBindingSource,
			SingularAttributeDescriptor referencedAttribute) {
		this.columnBindingSource = columnBindingSource;
		this.referencedAttribute = referencedAttribute;
	}

	public SingularAttributeDescriptor getReferencedAttribute() {
		return referencedAttribute;
	}

	@Override
	public Type getType() {
		return referencedAttribute.getOrmType();
	}

	@Override
	public void accept(SqlAstSelectInterpreter walker, boolean shallow) {
		walker.visitSingularAttributeReference( this, shallow );
	}

	@Override
	public Return toQueryReturn(String resultVariable) {
		switch ( referencedAttribute.getAttributeTypeClassification() ) {
			case BASIC: {
				return new ReturnScalarImpl( this, getType(), resultVariable );
			}
			case EMBEDDED: {
				return new ReturnScalarImpl( this, getType(), resultVariable );
			}
			case ANY: {
				// special reading for ANY types
				// although maybe this should be an exception, have to see what the old parse does
			}
			default: {
				// many-to-one / one-to-one
			}
		}
		throw new NotYetImplementedException(  );
	}

	@Override
	public DomainDescriptor getDomainReference() {
		return getReferencedAttribute();
	}

	@Override
	public List<ColumnBinding> resolveColumnBindings(boolean shallow) {
		return columnBindingSource.resolveColumnBindings( this, shallow );
	}
}
