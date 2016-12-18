/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.orm.test.sql.support;

import java.util.HashMap;
import java.util.Map;

import org.hibernate.query.proposed.spi.NonSelectQueryPlan;
import org.hibernate.query.proposed.spi.QueryInterpretations;
import org.hibernate.query.proposed.spi.SelectQueryPlan;

/**
 * @author Steve Ebersole
 */
public class QueryInterpretationsTestingImpl implements QueryInterpretations {
	/**
	 * Singleton access
	 */
	public static final QueryInterpretationsTestingImpl INSTANCE = new QueryInterpretationsTestingImpl();

	private final Map<Key, SelectQueryPlan> selectQueryPlanMap = new HashMap<>();
	private final Map<Key, NonSelectQueryPlan> nonSelectQueryPlanMap = new HashMap<>();

	@Override
	public SelectQueryPlan getSelectQueryPlan(Key key) {
		return selectQueryPlanMap.get( key );
	}

	@Override
	public void cacheSelectQueryPlan(Key key, SelectQueryPlan plan) {
		selectQueryPlanMap.put( key, plan );
	}

	@Override
	public NonSelectQueryPlan getNonSelectQueryPlan(Key key) {
		return nonSelectQueryPlanMap.get( key );
	}

	@Override
	public void cacheNonSelectQueryPlan(Key key, NonSelectQueryPlan plan) {
		nonSelectQueryPlanMap.put( key, plan );
	}
}
