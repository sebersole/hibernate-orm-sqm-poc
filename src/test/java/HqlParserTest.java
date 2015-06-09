import java.util.Collection;

import org.hibernate.hql.antlr.HqlParseTreeBuilder;
import org.hibernate.hql.antlr.HqlParser;

import org.junit.Test;

import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.xpath.XPath;

import static org.junit.Assert.assertEquals;

/**
 * Simple tests to make sure the basics are working and to see a visual of the parse tree.
 *
 * @author Steve Ebersole
 */
public class HqlParserTest {
	@Test
	public void justTestIt() throws Exception {
		HqlParser parser = HqlParseTreeBuilder.INSTANCE.parseHql( "select a.b from Something a where a.c = '1'" );

		Collection<ParseTree> fromClauses = XPath.findAll( parser.statement(), "//fromClause", parser );
		assertEquals( 1, fromClauses.size() );
	}
}
