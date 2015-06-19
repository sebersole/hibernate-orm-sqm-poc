/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.hql.parser.util;

import org.hibernate.hql.parser.model.BasicTypeDescriptor;
import org.hibernate.hql.parser.model.TypeDescriptor;

/**
 * @author Steve Ebersole
 */
public class BasicTypeDescriptorSingleton implements BasicTypeDescriptor {
	/**
	 * Singleton access
	 */
	public static final BasicTypeDescriptorSingleton INSTANCE = new BasicTypeDescriptorSingleton();

	private BasicTypeDescriptorSingleton() {
	}

	@Override
	public TypeDescriptor getAttributeType(String attributeName) {
		// a basic type (by definition) would not have queryable attributes
		return null;
	}
}
