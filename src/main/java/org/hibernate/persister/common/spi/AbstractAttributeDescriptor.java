/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.persister.common.spi;

/**
 * Base class for Attribute implementations
 *
 * @author Steve Ebersole
 */
public abstract class AbstractAttributeDescriptor implements AttributeDescriptor {
	private final AttributeContainer container;
	private final String name;

	public AbstractAttributeDescriptor(AttributeContainer container, String name) {
		this.container = container;
		this.name = name;
	}

	@Override
	public AttributeContainer getAttributeContainer() {
		return container;
	}

	@Override
	public String getAttributeName() {
		return name;
	}

}
