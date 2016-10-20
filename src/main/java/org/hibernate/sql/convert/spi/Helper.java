/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.convert.spi;

import org.hibernate.QueryException;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.persister.common.internal.DomainMetamodelImpl;
import org.hibernate.persister.common.spi.SingularAttributeImplementor;
import org.hibernate.persister.entity.spi.ImprovedEntityPersister;
import org.hibernate.query.proposed.spi.QueryParameterBinding;
import org.hibernate.query.proposed.spi.QueryParameterBindings;
import org.hibernate.sql.ast.expression.NamedParameter;
import org.hibernate.sql.ast.expression.PositionalParameter;
import org.hibernate.sqm.domain.DomainMetamodel;
import org.hibernate.sqm.query.from.SqmAttributeJoin;
import org.hibernate.type.EntityType;
import org.hibernate.type.Type;

/**
 * @author Steve Ebersole
 */
public class Helper {
	public static Type resolveType(NamedParameter parameter, QueryParameterBindings bindings) {
		final QueryParameterBinding binding = bindings.getBinding( parameter.getName() );
		if ( binding != null ) {
			if ( binding.getBindType() != null ) {
				return binding.getBindType();
			}
		}

		if ( parameter.getType() != null ) {
			return parameter.getType();
		}

		throw new QueryException( "Unable to determine Type for named parameter [:" + parameter.getName() + "]" );
	}

	public static Type resolveType(PositionalParameter parameter, QueryParameterBindings bindings) {
		final QueryParameterBinding binding = bindings.getBinding( parameter.getPosition() );
		if ( binding != null ) {
			if ( binding.getBindType() != null ) {
				return binding.getBindType();
			}
		}

		if ( parameter.getType() != null ) {
			return parameter.getType();
		}

		throw new QueryException( "Unable to determine Type for positional parameter [?" + parameter.getPosition() + "]" );
	}

	private Helper() {
	}

	public static ImprovedEntityPersister extractEntityPersister(
			SqmAttributeJoin joinedFromElement,
			SessionFactoryImplementor factory,
			DomainMetamodel sqmDomainMetamodel) {
		if ( joinedFromElement.getIntrinsicSubclassIndicator() != null ) {
			return (ImprovedEntityPersister) joinedFromElement.getIntrinsicSubclassIndicator();
		}

		// assume the fact that the attribute/type are entity has already been validated
		final EntityType entityType = (EntityType) ( (SingularAttributeImplementor) joinedFromElement.getAttributeBinding().getAttribute() ).getOrmType();
		final String entityName = entityType.getAssociatedEntityName( factory );
		return (ImprovedEntityPersister) sqmDomainMetamodel.resolveEntityReference( entityName );
	}
}
