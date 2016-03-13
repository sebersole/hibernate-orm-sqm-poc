/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query;

import java.util.Iterator;
import java.util.List;

import javax.persistence.TemporalType;

import org.hibernate.Incubating;
import org.hibernate.LockMode;
import org.hibernate.LockOptions;
import org.hibernate.NonUniqueResultException;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.type.Type;

/**
 * Represents an HQL/JPQL query or a compiled Criteria query
 *
 * Clean room design of {@link org.hibernate.Query} based on this SQM work
 *
 * @author Steve Ebersole
 */
@Incubating
public interface Query<T extends Query,R> extends BasicQueryContract<T> {
	/**
	 * Get the query string.
	 *
	 * @return the query string
	 */
	String getQueryString();

	/**
	 * Obtains the limit set on the maximum number of rows to retrieve.  No set limit means there is no limit set
	 * on the number of rows returned.  Technically both {@code null} and any negative values are interpreted as no
	 * limit; however, this method should always return null in such case.
	 *
	 * @return The
	 */
	Integer getMaxResults();

	/**
	 * Set the maximum number of rows to retrieve.
	 *
	 * @param maxResults the maximum number of rows
	 *
	 * @return {@code this}, for method chaining
	 *
	 * @see #getMaxResults()
	 */
	Query setMaxResults(int maxResults);

	/**
	 * Obtain the value specified (if any) for the first row to be returned from the query results; zero-based.  Used,
	 * in conjunction with {@link #getMaxResults()} in "paginated queries".  No value specified means the first result
	 * is returned.  Zero and negative numbers are the same as no setting.
	 *
	 * @return The first result number.
	 */
	Integer getFirstResult();

	/**
	 * Set the first row to retrieve.
	 *
	 * @param firstResult a row number, numbered from <tt>0</tt>
	 *
	 * @return {@code this}, for method chaining
	 *
	 * @see #getFirstResult()
	 */
	Query setFirstResult(int firstResult);

	/**
	 * Obtains the LockOptions in effect for this query.
	 *
	 * @return The LockOptions
	 *
	 * @see LockOptions
	 */
	LockOptions getLockOptions();

	/**
	 * Set the lock options for the query.  Specifically only the following are taken into consideration:<ol>
	 *     <li>{@link LockOptions#getLockMode()}</li>
	 *     <li>{@link LockOptions#getScope()}</li>
	 *     <li>{@link LockOptions#getTimeOut()}</li>
	 * </ol>
	 * For alias-specific locking, use {@link #setLockMode(String, LockMode)}.
	 *
	 * @param lockOptions The lock options to apply to the query.
	 *
	 * @return {@code this}, for method chaining
	 *
	 * @see #getLockOptions()
	 */
	Query setLockOptions(LockOptions lockOptions);

	/**
	 * Set the LockMode to use for specific alias (as defined in the query's <tt>FROM</tt> clause).
	 *
	 * The alias-specific lock modes specified here are added to the query's internal
	 * {@link #getLockOptions() LockOptions}.
	 *
	 * The effect of these alias-specific LockModes is somewhat dependent on the driver/database in use.  Generally
	 * speaking, for maximum portability, this method should only be used to mark that the rows corresponding to
	 * the given alias should be included in pessimistic locking ({@link LockMode#PESSIMISTIC_WRITE}).
	 *
	 * @param alias a query alias, or {@code "this"} for a collection filter
	 * @param lockMode The lock mode to apply.
	 *
	 * @return {@code this}, for method chaining
	 *
	 * @see #getLockOptions()
	 */
	Query setLockMode(String alias, LockMode lockMode);

	/**
	 * Obtain the comment currently associated with this query.  Provided SQL commenting is enabled
	 * (generally by enabling the {@code hibernate.use_sql_comments} config setting), this comment will also be added
	 * to the SQL query sent to the database.  Often useful for identifying the source of troublesome queries on the
	 * database side.
	 *
	 * @return The comment.
	 */
	String getComment();

	/**
	 * Set the comment for this query.
	 *
	 * @param comment The human-readable comment
	 *
	 * @return {@code this}, for method chaining
	 *
	 * @see #getComment()
	 */
	Query setComment(String comment);

	/**
	 * Add a DB query hint to the SQL.  These differ from JPA's {@link javax.persistence.QueryHint}, which is specific
	 * to the JPA implementation and ignores DB vendor-specific hints.  Instead, these are intended solely for the
	 * vendor-specific hints, such as Oracle's optimizers.  Multiple query hints are supported; the Dialect will
	 * determine concatenation and placement.
	 *
	 * @param hint The database specific query hint to add.
	 */
	Query addQueryHint(String hint);

// Depending on how we decide to handle Tuple results and RowTransformer
//	/**
//	 * Return the HQL select clause aliases, if any.
//	 *
//	 * @return an array of aliases as strings
//	 */
//	String[] getReturnAliases();

	/**
	 * Return the query results as an <tt>Iterator</tt>. If the query
	 * contains multiple results pre row, the results are returned in
	 * an instance of <tt>Object[]</tt>.<br>
	 * <br>
	 * Entities returned as results are initialized on demand. The first
	 * SQL query returns identifiers only.<br>
	 *
	 * @return the result iterator
	 */
	Iterator<R> iterate();

	/**
	 * Return the query results as <tt>ScrollableResults</tt>. The
	 * scrollability of the returned results depends upon JDBC driver
	 * support for scrollable <tt>ResultSet</tt>s.<br>
	 *
	 * @see ScrollableResults
	 *
	 * @return the result iterator
	 */
	ScrollableResults scroll();

	/**
	 * Return the query results as ScrollableResults. The scrollability of the returned results
	 * depends upon JDBC driver support for scrollable ResultSets.
	 *
	 * @param scrollMode The scroll mode
	 *
	 * @return the result iterator
	 *
	 * @see ScrollableResults
	 * @see ScrollMode
	 *
	 */
	ScrollableResults scroll(ScrollMode scrollMode);

	/**
	 * Return the query results as a <tt>List</tt>. If the query contains
	 * multiple results per row, the results are returned in an instance
	 * of <tt>Object[]</tt>.
	 *
	 * @return the result list
	 */
	List<R> list();

	/**
	 * Convenience method to return a single instance that matches
	 * the query, or null if the query returns no results.
	 *
	 * @return the single result or <tt>null</tt>
	 *
	 * @throws NonUniqueResultException if there is more than one matching result
	 */
	R uniqueResult();

	/**
	 * Execute the update or delete statement.
	 *
	 * The semantics are compliant with the ejb3 Query.executeUpdate() method.
	 *
	 * @return The number of entities updated or deleted.
	 */
	int executeUpdate();

	/**
	 * Bind a positional query parameter using its inferred Type.  If the parameter is
	 * defined in such a way that the Type cannot be inferred from its usage context then
	 * use of this form of binding is not allowed, and {@link #setParameter(int, Object, Type)}
	 * should be used instead
	 * <p/>
	 * todo : Warn about binding null values for parameters
	 *
	 * @param position the position of the parameter in the query
	 * string, numbered from <tt>0</tt>.
	 * @param val the possibly-null parameter value
	 *
	 * @return {@code this}, for method chaining
	 */
	Query setParameter(int position, Object val);

	/**
	 * Bind a positional query parameter using the supplied Type
	 * <p/>
	 * todo : Warn about binding null values for parameters
	 *
	 * @param position the position of the parameter in the query
	 * string, numbered from <tt>0</tt>.
	 * @param val the possibly-null parameter value
	 * @param type the Hibernate type
	 *
	 * @return {@code this}, for method chaining
	 */
	Query setParameter(int position, Object val, Type type);

	/**
	 * Bind a positional query parameter as some form of date/time using
	 * the indicated temporal-type.
	 * <p/>
	 * todo : Warn about binding null values for parameters
	 *
	 * @param position the position of the parameter in the query
	 * string, numbered from <tt>0</tt>.
	 * @param val the possibly-null parameter value
	 * @param temporalType the temporal-type to use in binding the date/time
	 *
	 * @return {@code this}, for method chaining
	 */
	Query setParameter(int position, Object val, TemporalType temporalType);

	/**
	 * Bind a named query parameter using its inferred Type.  If the parameter is
	 * defined in such a way that the Type cannot be inferred from its usage context then
	 * use of this form of binding is not allowed, and {@link #setParameter(String, Object, Type)}
	 * should be used instead
	 * <p/>
	 * todo : Warn about binding null values for parameters
	 *
	 * @param name the parameter name
	 * @param val the (possibly-null) parameter value
	 *
	 * @return {@code this}, for method chaining
	 */
	Query setParameter(String name, Object val);

	/**
	 * Bind a named query parameter using the supplied Type
	 * <p/>
	 * todo : Warn about binding null values for parameters
	 *
	 * @param name the parameter name
	 * @param val the possibly-null parameter value
	 * @param type the Hibernate type
	 *
	 * @return {@code this}, for method chaining
	 */
	Query setParameter(String name, Object val, Type type);

	/**
	 * Bind a named query parameter as some form of date/time using
	 * the indicated temporal-type.
	 * <p/>
	 * todo : Warn about binding null values for parameters
	 *
	 * @param name the parameter name
	 * @param val the possibly-null parameter value
	 * @param temporalType the temporal-type to use in binding the date/time
	 *
	 * @return {@code this}, for method chaining
	 */
	Query setParameter(String name, Object val, TemporalType temporalType);

	/**
	 * Bind a query parameter using its inferred Type.  If the parameter is
	 * defined in such a way that the Type cannot be inferred from its usage context then
	 * use of this form of binding is not allowed, and {@link #setParameter(int, Object, Type)}
	 * should be used instead
	 * <p/>
	 * todo : Warn about binding null values for parameters
	 *
	 * @param parameter The query parameter memento
	 * @param val the possibly-null parameter value
	 *
	 * @return {@code this}, for method chaining
	 */
	Query setParameter(QueryParameter parameter, Object val);

	/**
	 * Bind a query parameter using the supplied Type
	 * <p/>
	 * todo : Warn about binding null values for parameters
	 *
	 * @param parameter The query parameter memento
	 * @param val the possibly-null parameter value
	 * @param type the Hibernate type
	 *
	 * @return {@code this}, for method chaining
	 */
	Query setParameter(QueryParameter parameter, Object val, Type type);

	/**
	 * Bind a query parameter as some form of date/time using the indicated
	 * temporal-type.
	 * <p/>
	 * todo : Warn about binding null values for parameters
	 *
	 * @param parameter The query parameter memento
	 * @param val the possibly-null parameter value
	 * @param temporalType the temporal-type to use in binding the date/time
	 *
	 * @return {@code this}, for method chaining
	 */
	Query setParameter(QueryParameter parameter, Object val, TemporalType temporalType);

}
