/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.hql.antlr.normalization;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.hibernate.hql.ParsingContext;
import org.hibernate.hql.model.EntityTypeDescriptor;

import org.jboss.logging.Logger;

/**
 * An in-flight representation of a from-clause while performing normalization.
 * <p/>
 * The parent/child bit represents sub-queries.
 *
 * @author Steve Ebersole
 */
public class FromClause {
	private static final Logger log = Logger.getLogger( FromClause.class );

	private final ParsingContext parsingContext;
	private final FromClause parentFromClause;
	private List<FromClause> childFromClauses;

	private List<FromElementSpace> fromElementSpaces = new ArrayList<FromElementSpace>();
	private Map<String,FromElement> fromElementsByAlias = new HashMap<String, FromElement>();

	public FromClause(ParsingContext parsingContext) {
		this.parsingContext = parsingContext;
		this.parentFromClause = null;
	}

	public FromClause(FromClause parentFromClause) {
		this.parsingContext = parentFromClause.parsingContext;
		this.parentFromClause = parentFromClause;
	}

	public FromClause getParentFromClause() {
		return parentFromClause;
	}

	public List<FromClause> getChildFromClauses() {
		return childFromClauses == null ? Collections.<FromClause>emptyList() : childFromClauses;
	}

	public List<FromElementSpace> getFromElementSpaces() {
		return fromElementSpaces;
	}

	public FromElement findFromElementByAlias(String alias) {
		FromElement fromElement = fromElementsByAlias.get( alias );
		if ( fromElement == null ) {
			if ( parentFromClause != null ) {
				log.debugf( "Unable to resolve alias [%s] in local FromClause; checking parent" );
				fromElement = parentFromClause.findFromElementByAlias( alias );
			}
		}
		return fromElement;
	}

	public FromElement findFromElementWithAttribute(String name) {
		FromElement found = null;
		for ( FromElementSpace space : fromElementSpaces ) {
			final Collection<EntityTypeDescriptor> rootEntityTypes = parsingContext.getModelMetadata()
					.resolveEntityReference( space.getRoot().getEntityName() );
			if ( allHaveAttribute( rootEntityTypes, name ) ) {
				if ( found != null ) {
					throw new IllegalStateException( "Multiple from-elements expose unqualified attribute : " + name );
				}
				found = space.getRoot();
			}

			for ( FromElementJoined join : space.getJoins() ) {
				if ( join instanceof FromElementCrossJoinedImpl ) {
					final FromElementCrossJoinedImpl crossJoin = (FromElementCrossJoinedImpl) join;
					final Collection<EntityTypeDescriptor> entityTypes = parsingContext.getModelMetadata().resolveEntityReference( crossJoin.getEntityName() );
					if ( allHaveAttribute( entityTypes, name ) ) {
						if ( found != null ) {
							throw new IllegalStateException( "Multiple from-elements expose unqualified attribute : " + name );
						}
						found = space.getRoot();
					}
				}
				else if ( join instanceof FromElementQualifiedEntityJoinImpl ) {
					final FromElementQualifiedEntityJoinImpl entityJoin = (FromElementQualifiedEntityJoinImpl) join;
					final Collection<EntityTypeDescriptor> entityTypes = parsingContext.getModelMetadata().resolveEntityReference( entityJoin.getEntityName() );
					if ( allHaveAttribute( entityTypes, name ) ) {
						if ( found != null ) {
							throw new IllegalStateException( "Multiple from-elements expose unqualified attribute : " + name );
						}
						found = space.getRoot();
					}
				}
				else if ( join instanceof FromElementQualifiedAttributeJoinImpl ) {
					final FromElementQualifiedAttributeJoinImpl attributeJoin = (FromElementQualifiedAttributeJoinImpl) join;
					// todo : we need to match join to lhs in order to answer this.
				}
				else {
					throw new IllegalStateException( "Unexpected join type encountered: " + join );
				}
			}

		}

		if ( found == null ) {
			if ( parentFromClause != null ) {
				log.debugf( "Unable to resolve unqualified attribute [%s] in local FromClause; checking parent" );
				found = parentFromClause.findFromElementByAlias( name );
			}
		}

		return found;
	}

	private boolean allHaveAttribute(Collection<EntityTypeDescriptor> entityTypeDescriptors, String attributeName) {
		for ( EntityTypeDescriptor entityTypeDescriptor : entityTypeDescriptors ) {
			if ( entityTypeDescriptor.getAttributeType( attributeName ) == null ) {
				return false;
			}
		}
		return true;
	}

	void registerAlias(FromElement fromElement) {
		final FromElement old = fromElementsByAlias.put( fromElement.getAlias(), fromElement );
		if ( old != null ) {
			throw new IllegalStateException(
					String.format(
							Locale.ENGLISH,
							"Alias [%s] used for multiple from-clause-elements : %s, %s",
							fromElement.getAlias(),
							old,
							fromElement
					)
			);
		}
	}

	public ParsingContext getParsingContext() {
		return parsingContext;
	}

	public FromClause makeChildFromClause() {
		final FromClause child = new FromClause( this );
		if ( childFromClauses == null ) {
			childFromClauses = new ArrayList<FromClause>();
		}
		childFromClauses.add( child );

		return child;
	}

	public FromElementSpace makeFromElementSpace() {
		final FromElementSpace space = new FromElementSpace( this );
		fromElementSpaces.add( space );
		return space;
	}
}
