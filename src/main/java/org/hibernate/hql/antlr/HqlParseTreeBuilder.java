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

import java.util.Collection;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.xpath.XPath;

/**
 * @author Steve Ebersole
 */
public class HqlParseTreeBuilder {
	/**
	 * Singleton access
	 */
	public static final HqlParseTreeBuilder INSTANCE = new HqlParseTreeBuilder();

	private boolean debugEnabled = true;

	public HqlParser parseHql(String hql) {
		// Build the lexer
		HqlLexer hqlLexer = new HqlLexer( new ANTLRInputStream( hql ) );

		// Build the parser...
		final HqlParser parser = new HqlParser( new CommonTokenStream( hqlLexer ) );

		// this part would be protected by logging most likely.  Print the parse tree structure
		if ( debugEnabled ) {
			ParseTreeWalker.DEFAULT.walk( new HqlParseTreePrinter( parser ), parser.statement() );
			hqlLexer.reset();
			parser.reset();
		}

		return parser;
	}
}
