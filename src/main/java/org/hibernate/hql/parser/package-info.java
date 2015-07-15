/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */

/**
 * API for HQL query parsing.
 *
 * The main entry point into the parsing is {@link org.hibernate.hql.parser.SemanticQueryInterpreter}.
 * Pass it the HQL query string and a {@link org.hibernate.hql.parser.ConsumerContext} and get back the
 * semantic query tree as a {@link org.hibernate.hql.parser.semantic.Statement}.
 *
 * Generally, the parser will throw exceptions as one of 2 types:<ul>
 *     <li>
 *         {@link org.hibernate.hql.parser.QueryException} and derivatives represent problems with the
 *         query itself.
 *     </li>
 *     <li>
 *         {@link org.hibernate.hql.parser.ParsingException} and derivatives represent errors (potential bugs)
 *         during parsing.
 *     </li>
 * </ul>
 */
package org.hibernate.hql.parser;
