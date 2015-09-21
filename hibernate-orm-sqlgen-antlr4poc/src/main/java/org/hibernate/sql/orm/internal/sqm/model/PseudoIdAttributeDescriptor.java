/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.sql.orm.internal.sqm.model;

import org.hibernate.sqm.domain.TypeDescriptor;

/**
 * @author Steve Ebersole
 */
class PseudoIdAttributeDescriptor extends AttributeDescriptorImpl {
	public PseudoIdAttributeDescriptor(TypeDescriptor declaringType, TypeDescriptor type) {
		super( declaringType, "id", type );
	}
}
