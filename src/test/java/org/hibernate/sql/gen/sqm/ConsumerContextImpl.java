/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.sql.gen.sqm;

import org.hibernate.boot.registry.classloading.spi.ClassLoaderService;
import org.hibernate.boot.registry.classloading.spi.ClassLoadingException;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.sql.orm.internal.sqm.model.DomainMetamodelImpl;
import org.hibernate.sqm.domain.DomainMetamodel;
import org.hibernate.sqm.parser.ConsumerContext;

/**
 * Ultimately ConsumerContext could be implemented by SessionFactoryImpl.  And persisters, etc
 * could implement the domain contracts.
 * <p/>
 * But for now, for this testing, develop a bridge...
 *
 * @author Steve Ebersole
 */
public class ConsumerContextImpl implements ConsumerContext {
	private final SessionFactoryImplementor sessionFactory;
	private final ClassLoaderService classLoaderService;
	private final DomainMetamodelImpl domainMetamodel;

	public ConsumerContextImpl(SessionFactoryImplementor sessionFactory) {
		this.sessionFactory = sessionFactory;
		this.classLoaderService = sessionFactory.getServiceRegistry().getService( ClassLoaderService.class );
		this.domainMetamodel = new DomainMetamodelImpl( sessionFactory );
	}

	@Override
	public DomainMetamodel getDomainMetamodel() {
		return domainMetamodel;
	}

	@Override
	public Class classByName(String name) throws ClassNotFoundException {
		try {
			return classLoaderService.classForName( name );
		}
		catch (ClassLoadingException e) {
			throw new ClassNotFoundException( "Could not locate class : " + name, e );
		}
	}

	@Override
	public boolean useStrictJpaCompliance() {
		return sessionFactory.getSessionFactoryOptions().isStrictJpaQueryLanguageCompliance();
	}
}
