/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.hql.parser.antlr.path;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import org.hibernate.hql.parser.JoinType;
import org.hibernate.hql.parser.ParsingContext;
import org.hibernate.hql.parser.ParsingException;
import org.hibernate.hql.parser.SemanticException;
import org.hibernate.hql.parser.antlr.HqlParser;
import org.hibernate.hql.parser.antlr.HqlParser.IndexedPathContext;
import org.hibernate.hql.parser.antlr.HqlParser.SimplePathContext;
import org.hibernate.hql.parser.antlr.HqlParser.TreatedPathContext;
import org.hibernate.hql.parser.semantic.expression.ConstantEnumExpression;
import org.hibernate.hql.parser.semantic.expression.ConstantExpression;
import org.hibernate.hql.parser.semantic.expression.ConstantFieldExpression;
import org.hibernate.hql.parser.semantic.from.FromElement;
import org.hibernate.hql.parser.semantic.from.JoinedFromElement;

/**
 * @author Steve Ebersole
 */
public abstract class AbstractAttributePathResolverImpl<T> implements AttributePathResolver<T> {
	protected abstract ParsingContext parsingContext();

	@Override
	public T resolvePath(HqlParser.PathContext pathContext) {
		if ( pathContext instanceof SimplePathContext ) {
			return resolveSimplePathContext( (SimplePathContext) pathContext );
		}
		else if ( pathContext instanceof IndexedPathContext ) {
			return resolveIndexedPathContext( (IndexedPathContext) pathContext  );
		}
		else if ( pathContext instanceof TreatedPathContext ) {
			return resolveTreatedPathContext( (TreatedPathContext) pathContext  );
		}

		throw new ParsingException( "Unexpected concrete HqlParser.PathContext type" );
	}

	protected abstract T resolveSimplePathContext(SimplePathContext pathContext);

	protected abstract T resolveIndexedPathContext(IndexedPathContext pathContext);

	protected abstract T resolveTreatedPathContext(TreatedPathContext pathContext);

	protected FromElement resolveAnyIntermediateAttributePathJoins(
			FromElement lhs,
			String[] pathParts,
			int start,
			JoinType joinType,
			boolean fetched) {
		int i = start;

		// build joins for any intermediate path parts
		while ( i < pathParts.length-1 ) {
			lhs = buildAttributeJoin( lhs, pathParts[i], joinType, null, fetched );
			i++;
		}

		return lhs;
	}

	protected JoinedFromElement buildAttributeJoin(
			FromElement lhs,
			String attributeName,
			JoinType joinType,
			String alias,
			boolean fetched) {
		return lhs.getContainingSpace().buildAttributeJoin(
				lhs,
				attributeName,
				alias,
				joinType,
				fetched
		);
	}

	@SuppressWarnings("unchecked")
	protected ConstantExpression resolveConstantExpression(String reference) {
		// todo : hook in "import" resolution using the ParsingContext, and probably ClassLoader access too
		final int dotPosition = reference.lastIndexOf( '.' );
		final String className = reference.substring( 0, dotPosition - 1 );
		final String fieldName = reference.substring( dotPosition+1, reference.length() );

		try {
			final Class clazz = Class.forName( className );
			if ( clazz.isEnum() ) {
				try {
					return new ConstantEnumExpression( Enum.valueOf( clazz, fieldName ) );
				}
				catch (IllegalArgumentException e) {
					throw new SemanticException( "Name [" + fieldName + "] does not represent an emum constant on enum class [" + className + "]" );
				}
			}
			else {
				try {
					final Field field = clazz.getField( fieldName );
					if ( !Modifier.isStatic( field.getModifiers() ) ) {
						throw new SemanticException( "Field [" + fieldName + "] is not static on class [" + className + "]" );
					}
					field.setAccessible( true );
					return new ConstantFieldExpression( field.get( null ) );
				}
				catch (NoSuchFieldException e) {
					throw new SemanticException( "Name [" + fieldName + "] does not represent a field on class [" + className + "]", e );
				}
				catch (SecurityException e) {
					throw new SemanticException( "Field [" + fieldName + "] is not accessible on class [" + className + "]", e );
				}
				catch (IllegalAccessException e) {
					throw new SemanticException( "Unable to access field [" + fieldName + "] on class [" + className + "]", e );
				}
			}
		}
		catch (ClassNotFoundException e) {
			throw new SemanticException( "Cannot resolve class for query constant [" + reference + "]" );
		}
	}
}
