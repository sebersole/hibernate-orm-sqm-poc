/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.persister.common.spi;

/**
 * @author Steve Ebersole
 */
public abstract class AbstractSingularAttribute<O extends org.hibernate.type.Type>
		extends AbstractAttributeImpl
		implements SingularAttributeImplementor {
	private final O ormType;

	public AbstractSingularAttribute(
			DomainReferenceImplementor declaringType,
			String name,
			O ormType) {
		super( declaringType, name );
		this.ormType = ormType;
	}

	@Override
	public O getOrmType() {
		return ormType;
	}
}
