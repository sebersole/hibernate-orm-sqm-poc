/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.hql.parser.semantic.from;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.hibernate.hql.parser.JoinType;
import org.hibernate.hql.parser.ParsingException;
import org.hibernate.hql.parser.model.AttributeDescriptor;
import org.hibernate.hql.parser.model.EntityTypeDescriptor;

/**
 * @author Steve Ebersole
 */
public class FromElementSpace {
	private final FromClause fromClause;

	private RootEntityFromElement root;
	private List<JoinedFromElement> joins;


	public FromElementSpace(FromClause fromClause) {
		this.fromClause = fromClause;
	}

	public FromClause getFromClause() {
		return fromClause;
	}

	public RootEntityFromElement getRoot() {
		return root;
	}

	public List<JoinedFromElement> getJoins() {
		return joins == null ? Collections.<JoinedFromElement>emptyList() : joins;
	}

	public RootEntityFromElement makeRootEntityFromElement(EntityTypeDescriptor entityTypeDescriptor, String alias) {
		root = new RootEntityFromElement( this, alias, entityTypeDescriptor );
		registerAlias( root );
		return root;
	}

	private void registerAlias(FromElement fromElement) {
		fromClause.registerAlias( fromElement );
	}

	public CrossJoinedFromElement makeCrossJoinedFromElement(EntityTypeDescriptor entityTypeDescriptor, String alias) {
		return addJoin( new CrossJoinedFromElement( this, alias, entityTypeDescriptor ) );
	}

	public <T extends JoinedFromElement> T addJoin(T fromElement) {
		if ( joins == null ) {
			joins = new ArrayList<JoinedFromElement>();
		}
		joins.add( fromElement );
		registerAlias( fromElement );
		return fromElement;
	}

	public QualifiedAttributeJoinFromElement buildAttributeJoin(
			FromElement lhs,
			AttributeDescriptor attributeDescriptor,
			String alias,
			JoinType joinType,
			boolean fetched) {
		final String aliasToUse = alias != null
				? alias
				: fromClause.getParsingContext().getImplicitAliasGenerator().buildUniqueImplicitAlias();
		if ( attributeDescriptor == null ) {
			throw new ParsingException(
					"AttributeDescriptor was null [name unknown] in relation to from-element [" +
							lhs.getTypeDescriptor().getTypeName() + "]"
			);
		}
		final QualifiedAttributeJoinFromElement join = new QualifiedAttributeJoinFromElement(
				this,
				aliasToUse,
				attributeDescriptor,
				joinType,
				fetched
		);
		addJoin( join );
		return join;
	}

	public void complete() {

	}
}
