/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.hql.antlr.normalization;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.hibernate.hql.antlr.HqlSemantic;

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

	private final NormalizationContext normalizationContext;
	private final FromClause parentFromClause;
	private List<FromClause> childFromClauses;

	private List<FromElementSpace> fromElementSpaces = new ArrayList<FromElementSpace>();
	private Map<String,FromElement> fromElementsByAlias = new HashMap<String, FromElement>();

	public FromClause(NormalizationContext normalizationContext) {
		this.normalizationContext = normalizationContext;
		this.parentFromClause = null;
	}

	public FromClause(FromClause parentFromClause) {
		this.normalizationContext = parentFromClause.normalizationContext;
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

	public NormalizationContext getNormalizationContext() {
		return normalizationContext;
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
