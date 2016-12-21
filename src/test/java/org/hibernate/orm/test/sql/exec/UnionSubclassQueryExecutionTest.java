/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.orm.test.sql.exec;

import java.util.List;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;
import javax.persistence.Version;

import org.hibernate.Session;
import org.hibernate.boot.MetadataSources;
import org.hibernate.orm.test.sql.BaseExecutionTest;
import org.hibernate.query.proposed.internal.sqm.QuerySqmImpl;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author Steve Ebersole
 */
public class UnionSubclassQueryExecutionTest extends BaseExecutionTest {

	@Test
	public void queryRootEntity() {
		doInSession(
				session -> {
					final QuerySqmImpl<UnionSubclassRoot> query = generateQueryImpl(
							session,
							"select root from UnionSubclassRoot root where root.description like ?1",
							UnionSubclassRoot.class
					);
					query.setParameter( 1, "%bold adventure%" );

					final List<UnionSubclassRoot> results = query.list();

					assertThat( results.size(), is( 1 ) );
					UnionSubclassRoot entity = results.get( 0 );
				}
		);
	}

	// todo : add tests asserting the o.h.persister.common Table/Columns definitions for this hierarchy


	@Override
	public void before() throws Exception {
		super.before();

		final UnionSubclassBranch entity = new UnionSubclassBranch();
		entity.id = 1;
		entity.description = "A bold adventure unlike any ever seen!";
		entity.texture = "silky";
		entity.numberOfLeaves = 100;

		Session session = getSessionFactory().openSession();
		session.beginTransaction();
		session.persist( entity );
		session.getTransaction().commit();
		session.close();
	}

	@Override
	protected void applyMetadataSources(MetadataSources metadataSources) {
		super.applyMetadataSources( metadataSources );
		metadataSources.addAnnotatedClass( UnionSubclassRoot.class );
		metadataSources.addAnnotatedClass( UnionSubclassTrunk.class );
		metadataSources.addAnnotatedClass( UnionSubclassBranch.class );
	}


	@Entity( name = "UnionSubclassRoot" )
	@Table( name = "UnionSubclassRoot" )
	@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
	public static class UnionSubclassRoot {
		@Id
		public Integer id;
		@Version
		public Integer version;

		public String description;
	}

	@Entity( name = "UnionSubclassBranch" )
	@Table( name = "UnionSubclassBranch" )
	public static class UnionSubclassTrunk extends UnionSubclassRoot {
		public String texture;
	}

	@Entity( name = "UnionSubclassTrunk" )
	@Table( name = "UnionSubclassTrunk" )
	public static class UnionSubclassBranch extends UnionSubclassTrunk {
		public int numberOfLeaves;
	}

}
