/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.sql.gen;

import java.util.List;

import org.hibernate.LockOptions;
import org.hibernate.ScrollMode;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cache.spi.QueryCache;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.engine.spi.RowSelection;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.sql.ast.SelectQuery;
import org.hibernate.sql.ast.SelectStatementInterpreter;
import org.hibernate.sql.gen.sqm.ConsumerContextImpl;
import org.hibernate.sql.orm.QueryOptions;
import org.hibernate.sql.orm.QueryParameterBindings;
import org.hibernate.sqm.SemanticQueryInterpreter;
import org.hibernate.sqm.query.SelectStatement;
import org.hibernate.sqm.query.Statement;

import org.junit.After;
import org.junit.Before;

/**
 * Test for asserting structures, etc.  Does not export the schema
 *
 * @author Steve Ebersole
 */
public class BaseUnitTest {
	private SessionFactoryImplementor sessionFactory;
	private ConsumerContextImpl consumerContext;

	@Before
	public void before() throws Exception {
		final StandardServiceRegistry ssr = new StandardServiceRegistryBuilder()
				.applySetting( AvailableSettings.JPAQL_STRICT_COMPLIANCE, strictJpaCompliance() )
				.build();

		try {
			MetadataSources metadataSources = new MetadataSources( ssr );
			applyMetadataSources( metadataSources );

			this.sessionFactory = (SessionFactoryImplementor) metadataSources.buildMetadata().buildSessionFactory();
		}
		catch (Exception e) {
			StandardServiceRegistryBuilder.destroy( ssr );
			throw e;
		}

		consumerContext = new ConsumerContextImpl( sessionFactory );
	}

	@After
	public void after() {
		if ( sessionFactory != null ) {
			sessionFactory.close();
		}
	}

	protected boolean strictJpaCompliance() {
		return false;
	}

	protected void applyMetadataSources(MetadataSources metadataSources) {
	}

	protected final SessionFactoryImplementor getSessionFactory() {
		return sessionFactory;
	}

	protected final ConsumerContextImpl getConsumerContext() {
		return consumerContext;
	}

	protected Statement interpret(String query) {
		return SemanticQueryInterpreter.interpret( query, getConsumerContext() );
	}

	protected SelectQuery interpretSelectQuery(String query) {
		final SelectStatement statement = (SelectStatement) interpret( query );

		final SelectStatementInterpreter interpreter = new SelectStatementInterpreter( queryOption(), callBack() );
		interpreter.interpret( statement );

		return interpreter.getSelectQuery();
	}

	protected Callback callBack() {
		return new Callback() {
		};
	}

	protected QueryOptions queryOption() {
		return new QueryOptions() {
			@Override
			public QueryParameterBindings getParameterBindings() {
				return null;
			}

			@Override
			public Integer getTimeout() {
				return null;
			}

			@Override
			public Integer getFetchSize() {
				return null;
			}

			@Override
			public LockOptions getLockOptions() {
				return null;
			}

			@Override
			public ScrollMode getScrollMode() {
				return null;
			}

			@Override
			public QueryCache getQueryResultCache() {
				return null;
			}

			@Override
			public Integer getFirstRow() {
				return null;
			}

			@Override
			public Integer getMaxRows() {
				return null;
			}

			@Override
			public String getComment() {
				return null;
			}

			@Override
			public List<String> getSqlHints() {
				return null;
			}
		};
	}
}
