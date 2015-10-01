/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.sql.orm.internal.sqm.model;

import org.hibernate.sqm.domain.AttributeDescriptor;
import org.hibernate.sqm.domain.BasicTypeDescriptor;

/**
 * @author Steve Ebersole
 */
public class BasicTypeDescriptorImpl implements BasicTypeDescriptor {
	private final Class javaType;

	public BasicTypeDescriptorImpl(Class javaType) {
		this.javaType = javaType;
	}

	@Override
	public Class getCorrespondingJavaType() {
		return javaType;
	}

	@Override
	public String getTypeName() {
		return javaType.getName();
	}

	@Override
	public AttributeDescriptor getAttributeDescriptor(String attributeName) {
		// basic types cannot have persistent attributes
		return null;
	}
}
