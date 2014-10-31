import java.io.StringReader;

import org.hibernate.hql.antlr.HqlBaseListener;
import org.hibernate.hql.antlr.HqlLexer;
import org.hibernate.hql.antlr.HqlParser;

import org.junit.Test;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

/**
 * @author Steve Ebersole
 */
public class HqlParserTest {
	@Test
	public void justTestIt() throws Exception {
		HqlLexer lexer = new HqlLexer(
				new ANTLRInputStream(
						new StringReader( "select a.b from Something s" )
				)
		);
		CommonTokenStream tokens = new CommonTokenStream( lexer );
		HqlParser parser = new HqlParser( tokens ) {
			@Override
			public void enterRule(
					@NotNull ParserRuleContext localctx, int state, int ruleIndex) {

				System.out.println( "Entering rule : " + getRuleNames()[ruleIndex] + " -> " + localctx.getText() );
				super.enterRule( localctx, state, ruleIndex );
			}
		};

		HqlParser.StatementContext statementTree = parser.statement();

		ParseTreeWalker walker = new ParseTreeWalker();
		Printer printer = new Printer( parser );
		walker.walk( printer, statementTree );
	}

	class Printer extends HqlBaseListener {
		private final HqlParser parser;

		Printer(HqlParser parser) {
			this.parser = parser;
		}

		@Override
		public void enterEveryRule(@NotNull ParserRuleContext ctx) {
			System.out.println( "Entering rule : " + parser.getRuleNames()[ctx.getRuleIndex()] + " -> " + ctx.getText() );
			super.enterEveryRule( ctx );
		}
	}
}
