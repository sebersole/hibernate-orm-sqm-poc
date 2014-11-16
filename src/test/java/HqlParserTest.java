import org.hibernate.hql.antlr.HqlParseTreeBuilder;

import org.junit.Test;

/**
 * @author Steve Ebersole
 */
public class HqlParserTest {
	@Test
	public void justTestIt() throws Exception {
		HqlParseTreeBuilder.INSTANCE.parseHql( "select a.b from Something s" );
	}
}
