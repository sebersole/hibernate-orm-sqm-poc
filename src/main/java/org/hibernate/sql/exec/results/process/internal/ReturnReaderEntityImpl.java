/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.exec.results.process.internal;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.persister.entity.spi.EntityReference;
import org.hibernate.sql.ast.select.SqlSelectionDescriptor;
import org.hibernate.sql.exec.results.process.spi.ResultSetProcessingOptions;
import org.hibernate.sql.exec.results.process.spi.ReturnReader;
import org.hibernate.sql.exec.results.process.spi.RowProcessingState;

/**
 * @author Steve Ebersole
 */
public class ReturnReaderEntityImpl implements ReturnReader {
	private final List<SqlSelectionDescriptor> selectionDescriptorList;
	private final boolean shallow;
	private final EntityReference expression;

	public ReturnReaderEntityImpl(
			List<SqlSelectionDescriptor> selectionDescriptorList,
			boolean shallow,
			EntityReference expression) {
		this.selectionDescriptorList = new ArrayList<>( selectionDescriptorList );
		this.shallow = shallow;
		this.expression = expression;
	}

	// todo : keep a per-row Map keyed by ColumnBinding/SqlSelectionDescriptor?

	@Override
	public void readBasicValues(
			RowProcessingState rowProcessingState,
			ResultSetProcessingOptions options) throws SQLException {
//		final SharedSessionContractImplementor session = rowProcessingState.getResultSetProcessingState().getSession();
//		final ResultSet resultSet = rowProcessingState.getResultSetProcessingState().getResultSet();
//
//		final EntityReferenceProcessingState entityProcessingState = rowProcessingState.getProcessingState( expression );
//
//
//		// get any previously registered identifier hydrated-state
//		Object identifierHydratedForm = entityProcessingState.getIdentifierHydratedForm();
//		if ( identifierHydratedForm == null ) {
//			// if there is none, read it from the result set
//			identifierHydratedForm = readIdentifierHydratedState( resultSet, context );
//
//			// broadcast the fact that a hydrated identifier value just became associated with
//			// this entity reference
//			processingState.registerIdentifierHydratedForm( identifierHydratedForm );
//		}






//		final ResultSetProcessingContext.EntityReferenceProcessingState processingState = getIdentifierResolutionContext( context );
//
//		final EntityKey entityKey = processingState.getEntityKey();
//		final Object entityInstance = context.getProcessingState( entityReturn ).getEntityInstance();
//
//		if ( context.shouldReturnProxies() ) {
//			final Object proxy = context.getSession().getPersistenceContext().proxyFor(
//					entityReturn.getEntityPersister(),
//					entityKey,
//					entityInstance
//			);
//			if ( proxy != entityInstance ) {
//				( (HibernateProxy) proxy ).getHibernateLazyInitializer().setImplementation( proxy );
//				return proxy;
//			}
//		}
//
//		return entityInstance;
	}

//	private Object readIdentifierHydratedState(ResultSet resultSet, ResultSetProcessingContext context)
//			throws SQLException {
//		try {
//			return expression.getEntityPersister().getIdentifierType().hydrate(
//					resultSet,
//					entityReferenceAliases.getColumnAliases().getSuffixedKeyAliases(),
//					context.getSession(),
//					null
//			);
//		}
//		catch (Exception e) {
//			throw new HibernateException(
//					"Encountered problem trying to hydrate identifier for entity ["
//							+ entityReference.getEntityPersister() + "]",
//					e
//			);
//		}
//	}

	@Override
	public void resolveBasicValues(
			RowProcessingState processingState,
			ResultSetProcessingOptions options) throws SQLException {

	}

	@Override
	public Object assemble(
			RowProcessingState processingState,
			ResultSetProcessingOptions options) throws SQLException {
		return null;
	}

	@Override
	public Class getReturnedJavaType() {
		return null;
	}

	@Override
	public int getNumberOfColumnsRead(SessionFactoryImplementor sessionFactory) {
		return 0;
	}
}
