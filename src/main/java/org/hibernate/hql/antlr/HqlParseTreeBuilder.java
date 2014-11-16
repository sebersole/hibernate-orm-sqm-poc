/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * Copyright (c) 2008-2012, Red Hat Inc. or third-party contributors as
 * indicated by the @author tags or express copyright attribution
 * statements applied by the authors.  All third-party contributions are
 * distributed under license by Red Hat Inc.
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
package org.hibernate.hql.antlr;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

/**
 * @author Steve Ebersole
 */
public class HqlParseTreeBuilder {
	/**
	 * Singleton access
	 */
	public static final HqlParseTreeBuilder INSTANCE = new HqlParseTreeBuilder();

	public HqlParser.StatementContext parseHql(String hql) {
		// Build the parser...
		final HqlParser parser = new HqlParser(
				new CommonTokenStream( new HqlLexer( new ANTLRInputStream( hql ) ) )
		);

		// Obtain the parse tree from the parser using the main `statement` rule
		final HqlParser.StatementContext statementTree = parser.statement();

		// this part would be protected by logging most likely.  Print the parse tree structure
		ParseTreeWalker.DEFAULT.walk( new HqlParseTreePrinter( parser ), statementTree );

		return statementTree;
	}
}
