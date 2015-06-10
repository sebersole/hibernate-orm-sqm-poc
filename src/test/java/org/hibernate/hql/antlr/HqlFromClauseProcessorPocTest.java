/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.hql.antlr;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.xpath.XPath;

import static org.junit.Assert.assertEquals;

/**
 * Initial work on a "from clause processor"
 *
 * @author Steve Ebersole
 */
public class HqlFromClauseProcessorPocTest {
	@Test
	public void justTestIt() throws Exception {
		final HqlParser parser = HqlParseTreeBuilder.INSTANCE.parseHql( "select a.b from Something a where a.c = '1'" );

		final FromClauseProcessor fromClauseProcessor = new FromClauseProcessor();

		ParseTreeWalker.DEFAULT.walk( fromClauseProcessor, parser.statement() );
	}

	static interface FromElement {
		FromElementContainer getContainer();

		String getAlias();
	}

	static interface FromElementContainer {
		List<FromElement> getFromElements();
		void addFromElement(FromElement fromElement);
	}

	static interface NestedFromElementContainer extends FromElementContainer {
		FromElementContainer getParentFromElementContainer();
	}

	static interface JoinableFromElement extends FromElement, FromElementContainer {
	}

	static interface Statement {
		FromElementContainer getFromElementContainer();
	}

	static interface SelectStatement extends Statement {

	}

	static interface UpdateStatement extends Statement {

	}

	static interface DeleteStatement extends Statement {

	}


	private Statement statement;

	class FromClauseProcessor extends HqlParserBaseListener {
		// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// enterFromClause and exitFromClause are used to manage hierarchical stack of FromClauses.
		// todo : maybe its better to maintain that at the query level... a sort-of "from element context"

		@Override
		public void enterFromClause(HqlParser.FromClauseContext ctx) {
//			if ( currentFromClause == null ) {
//				currentFromClause = FromClause.root();
//			}
//			else {
//				currentFromClause = currentFromClause.addChildFromClause();
//			}
			super.enterFromClause( ctx );
		}

		@Override
		public void exitFromClause(HqlParser.FromClauseContext ctx) {
			super.exitFromClause( ctx );
//			if ( currentFromClause == null ) {
//				throw new RuntimeException( "Mismatch currentFromClause handling" );
//			}
//
//			currentFromClause = currentFromClause.parentFromClause;
		}
	}
}
