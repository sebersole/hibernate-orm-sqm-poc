/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.sql.orm.internal.sqm.model;

import org.hibernate.sqm.domain.AttributeDescriptor;
import org.hibernate.sqm.domain.TypeDescriptor;

/**
 * @author Steve Ebersole
 */
public class AttributeDescriptorImpl implements AttributeDescriptor {
	private final TypeDescriptor declaringType;
	private final String name;
	private final TypeDescriptor type;

	public AttributeDescriptorImpl(TypeDescriptor declaringType, String name, TypeDescriptor type) {
		this.declaringType = declaringType;
		this.name = name;
		this.type = type;
	}

	public TypeDescriptor getDeclaringType() {
		return declaringType;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public TypeDescriptor getType() {
		return type;
	}
}
