/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.convert.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.hibernate.sql.ast.from.FromClause;
import org.hibernate.sql.ast.from.TableGroup;
import org.hibernate.sql.convert.ConversionException;
import org.hibernate.sqm.query.expression.domain.DomainReferenceBinding;
import org.hibernate.sqm.query.from.SqmAttributeJoin;
import org.hibernate.sqm.query.from.SqmFrom;

import org.jboss.logging.Logger;

/**
 * An index of various FROM CLAUSE resolutions.
 *
 * @author Steve Ebersole
 */
public class FromClauseIndex {
	private static final Logger log = Logger.getLogger( FromClauseIndex.class );

	private final Stack<FromClauseStackNode> fromClauseStackNodes = new Stack<>();

	private final Map<String,SqmFrom> sqmFromByUid = new HashMap<>();
	private final Map<SqmFrom, TableGroup> tableGroupBySqmFromXref = new HashMap<>();
	private Map<String, List<SqmAttributeJoin>> sqmFetchesByParentUid = new HashMap<>();


	public void pushFromClause(FromClause fromClause) {
		FromClauseStackNode parent = null;
		if ( fromClauseStackNodes.size() > 0 ) {
			parent = fromClauseStackNodes.peek();
		}
		FromClauseStackNode node = new FromClauseStackNode( parent, fromClause );
		fromClauseStackNodes.push( node );
	}

	public FromClause popFromClause() {
		final FromClauseStackNode node = fromClauseStackNodes.pop();
		if ( node == null ) {
			return null;
		}
		else {
			return node.getCurrentFromClause();
		}
	}

	public FromClause currentFromClause() {
		final FromClauseStackNode currentNode = fromClauseStackNodes.peek();
		if ( currentNode == null ) {
			return null;
		}
		else {
			return currentNode.getCurrentFromClause();
		}
	}

	public void crossReference(SqmFrom fromElement, TableGroup tableGroup) {
		TableGroup old = tableGroupBySqmFromXref.put( fromElement, tableGroup );
		if ( old != null ) {
			log.debugf(
					"FromElement [%s] was already cross-referenced to TableSpecificationGroup - old : [%s]; new : [%s]",
					fromElement,
					old,
					tableGroup
			);
		}

		SqmFrom existing = sqmFromByUid.put( fromElement.getUniqueIdentifier(), fromElement );
		if ( existing != null ) {
			if ( existing != fromElement ) {
				log.debugf(
						"Encountered duplicate SqmFrom#getUniqueIdentifier values [%s -> (%s, %s)]",
						fromElement.getUniqueIdentifier(),
						fromElement,
						existing
				);
			}
		}

		if ( fromElement instanceof SqmAttributeJoin ) {
			final SqmAttributeJoin sqmAttributeJoin = (SqmAttributeJoin) fromElement;
			final String fetchParentUid = sqmAttributeJoin.getFetchParentUniqueIdentifier();

			if ( fetchParentUid != null ) {
				// otherwise, not fetched

				if ( sqmFetchesByParentUid == null ) {
					sqmFetchesByParentUid = new HashMap<>();
				}

				List<SqmAttributeJoin> fetches = sqmFetchesByParentUid.computeIfAbsent(
						fetchParentUid,
						k -> new ArrayList<>()
				);
				fetches.add( sqmAttributeJoin );
			}
		}

		crossReference( fromElement.getDomainReferenceBinding(), tableGroup );
	}

	public TableGroup findResolvedTableGroup(SqmFrom fromElement) {
		return tableGroupBySqmFromXref.get( fromElement );
	}

	public SqmFrom findSqmFromByUniqueIdentifier(String uniqueIdentifier) {
		return sqmFromByUid.get( uniqueIdentifier );
	}

	public TableGroup findResolvedTableGroupByUniqueIdentifier(String uniqueIdentifier) {
		final SqmFrom sqmFrom = findSqmFromByUniqueIdentifier( uniqueIdentifier );
		if ( sqmFrom == null ) {
			throw new ConversionException( "Could not resolve unique-identifier to SqmFrom" );
		}

		return tableGroupBySqmFromXref.get( sqmFrom );
	}

	public void crossReference(DomainReferenceBinding binding, TableGroup group) {
		tableGroupBySqmFromXref.put( binding.getFromElement(), group );
	}

	public TableGroup findResolvedTableGroup(DomainReferenceBinding binding) {
		return findResolvedTableGroup( binding.getFromElement() );
	}

	public boolean isResolved(DomainReferenceBinding binding) {
		return isResolved( binding.getFromElement() );
	}

	public boolean isResolved(SqmFrom fromElement) {
		return tableGroupBySqmFromXref.containsKey( fromElement );
	}

	public List<SqmAttributeJoin> findFetchesByUniqueIdentifier(String uniqueIdentifier) {
		final List<SqmAttributeJoin> fetches = sqmFetchesByParentUid.get( uniqueIdentifier );
		return fetches == null ? Collections.emptyList() : Collections.unmodifiableList( fetches );
	}

	public static class FromClauseStackNode {
		private final FromClauseStackNode parentNode;
		private final FromClause currentFromClause;

		public FromClauseStackNode(FromClause currentFromClause) {
			this( null, currentFromClause );
		}

		public FromClauseStackNode(FromClauseStackNode parentNode, FromClause currentFromClause) {
			this.parentNode = parentNode;
			this.currentFromClause = currentFromClause;
		}

		public FromClauseStackNode getParentNode() {
			return parentNode;
		}

		public FromClause getCurrentFromClause() {
			return currentFromClause;
		}
	}

}
