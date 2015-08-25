/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.sql.gen.sqm;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.boot.registry.classloading.spi.ClassLoaderService;
import org.hibernate.boot.registry.classloading.spi.ClassLoadingException;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.query.parser.ConsumerContext;
import org.hibernate.sqm.domain.EntityTypeDescriptor;

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

	public ConsumerContextImpl(SessionFactoryImplementor sessionFactory) {
		this.sessionFactory = sessionFactory;
		this.classLoaderService = sessionFactory.getServiceRegistry().getService( ClassLoaderService.class );
	}

	@Override
	public EntityTypeDescriptor resolveEntityReference(String reference) {
		reference = sessionFactory.getImportedClassName( reference );

		final String[] implementors = sessionFactory.getImplementors( reference );

		if ( implementors == null || implementors.length == 0 ) {
			return null;
		}

		if ( implementors.length == 1 ) {
			return new EntityTypeDescriptorImpl( sessionFactory.getEntityPersister( implementors[0] ) );
		}

		List<EntityTypeDescriptor> descriptors = new ArrayList<EntityTypeDescriptor>();
		for ( String implementor : implementors ) {
			final EntityPersister persister = sessionFactory.getEntityPersister( implementor );
			descriptors.add( new EntityTypeDescriptorImpl( persister ) );
		}
		return new PolymorphicEntityTypeDescriptorImpl( reference, descriptors );
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
