/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.persister.common.spi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.hibernate.sql.ast.from.ColumnBinding;
import org.hibernate.sql.ast.from.TableGroup;
import org.hibernate.sql.convert.spi.NotYetImplementedException;
import org.hibernate.type.BasicType;

/**
 * @author Steve Ebersole
 */
public abstract class AbstractSingularAttributeDescriptor<O extends org.hibernate.type.Type>
		extends AbstractAttributeDescriptor
		implements SingularAttributeDescriptor {
	private final O ormType;

	public AbstractSingularAttributeDescriptor(
			AttributeContainer attributeContainer,
			String name,
			O ormType) {
		super( attributeContainer, name );
		this.ormType = ormType;
	}

	@Override
	public O getOrmType() {
		return ormType;
	}

	@Override
	public List<ColumnBinding> resolveColumnBindings(TableGroup tableGroup, boolean shallow) {
		final Column[] columns = collectColumns( shallow );

		if ( ormType instanceof BasicType ) {
			if ( columns.length != 1 ) {
				throw new NotYetImplementedException( "Support for BasicTypes having more than one column - support for that as a feature will likely go away anyway" );
			}
			return Collections.singletonList(
					new ColumnBinding(
							columns[0],
							(BasicType) ormType,
							tableGroup.locateTableBinding( columns[0].getSourceTable() )
					)
			);
		}
		else {
			final List<ColumnBinding> columnBindingList = new ArrayList<>();
			for ( Column column : collectColumns( shallow ) ) {
				columnBindingList.add(
						new ColumnBinding(
								column,
								// todo : would be nice for this to be the attribute's Type if that Type is single-column
								column.getJdbcType(),
								tableGroup.locateTableBinding( column.getSourceTable() )
						)
				);
			}

			return columnBindingList;
		}
	}

	protected Column[] collectColumns(boolean shallow) {
		return getColumns();
	}
}
