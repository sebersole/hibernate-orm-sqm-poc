package org.hibernate.hql.antlr;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.NotNull;

/**
 * @author Steve Ebersole
 */
public class HqlParseTreePrinter extends HqlParserBaseListener {
	private final HqlParser parser;

	private int depth = 0;

	public HqlParseTreePrinter(HqlParser parser) {
		this.parser = parser;
	}

	@Override
	public void enterEveryRule(@NotNull ParserRuleContext ctx) {
		System.out.println(
				String.format(
						"%s %s ['%s'] (%s -> %s)",
						enterRulePadding(),
						parser.getRuleNames()[ctx.getRuleIndex()],
						ctx.getText(),
						ctx.getStart(),
						ctx.getStop()
				)
		);
		super.enterEveryRule( ctx );
	}

	private String enterRulePadding() {
		return pad( depth++ ) + "->";
	}

	private String pad(int depth) {
		StringBuilder buf = new StringBuilder( 2 * depth );
		for ( int i = 0; i < depth; i++ ) {
			buf.append( "  " );
		}
		return buf.toString();
	}

	@Override
	public void exitEveryRule(@NotNull ParserRuleContext ctx) {
		super.exitEveryRule( ctx );
		System.out.println(
				String.format(
						"%s %s [%s]",
						exitRulePadding(),
						parser.getRuleNames()[ctx.getRuleIndex()],
						ctx.getText()
				)
		);
	}

	private String exitRulePadding() {
		return pad( --depth ) + "<-";
	}
}
