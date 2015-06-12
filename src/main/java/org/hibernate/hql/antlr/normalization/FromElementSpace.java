/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.hql.antlr.normalization;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.hibernate.hql.antlr.HqlParser;

/**
 * @author Steve Ebersole
 */
public class FromElementSpace {
	private final FromClause fromClause;

	private FromElementRootEntity root;
	private List<FromElementJoined> joins;


	public FromElementSpace(FromClause fromClause) {
		this.fromClause = fromClause;
	}

	public FromElementRootEntity getRoot() {
		return root;
	}

	public List<FromElementJoined> getJoins() {
		return joins == null ? Collections.<FromElementJoined>emptyList() : joins;
	}

	public FromElementRootEntity makeFromElement(HqlParser.RootEntityReferenceContext ctx) {
		final String entityName = ctx.mainEntityPersisterReference().dotIdentifierPath().getText();

		String alias = null;
		if ( ctx.mainEntityPersisterReference().IDENTIFIER() != null ) {
			alias = ctx.mainEntityPersisterReference().IDENTIFIER().getText();
		}
		if ( alias == null ) {
			alias = fromClause.getNormalizationContext().getImplicitAliasGenerator().buildUniqueImplicitAlias();
		}

		root = new FromElementRootEntity( this, alias, entityName );
		registerAlias( root );
		return root;
	}

	private void registerAlias(FromElement fromElement) {
		fromClause.registerAlias( fromElement );
	}

	public FromElementJoined makeFromElement(HqlParser.CrossJoinContext ctx) {
		final String entityName = ctx.mainEntityPersisterReference().dotIdentifierPath().getText();

		String alias = null;
		if ( ctx.mainEntityPersisterReference().IDENTIFIER() != null ) {
			alias = ctx.mainEntityPersisterReference().IDENTIFIER().getText();
		}
		if ( alias == null ) {
			alias = fromClause.getNormalizationContext().getImplicitAliasGenerator().buildUniqueImplicitAlias();
		}

		return addJoin( new FromElementCrossJoinedImpl( this, alias, entityName ) );
	}

	private FromElementJoined addJoin(FromElementJoined fromElement) {
		if ( joins == null ) {
			joins = new ArrayList<FromElementJoined>();
		}
		joins.add( fromElement );
		registerAlias( fromElement );
		return fromElement;
	}

	public void complete() {

	}
}
