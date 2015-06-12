parser grammar HqlSemantic;

options {
	tokenVocab=HqlLexer;
}

tokens {
	ALIAS_NAME,
	ALIAS_REFERENCE,
	ATTRIBUTE_REFERENCE
}

@header {
/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.hql.antlr;
}


@members {
/*
 * The intention of this grammar is only to generate walking artifacts (walker, listener, visitor).
 *
 * The idea being to mimic Antlr 2/3 style tree parsing and tree re-writing in an Antlr based translator.
 */
}

statement
	: ( selectStatement | updateStatement | deleteStatement | insertStatement )
	;

selectStatement
	: queryExpression orderByClause?
	;

queryExpression
	:	querySpec ( (UNION | INTERSECT | EXCEPT ) ALL? querySpec )*
	;

updateStatement
	: UPDATE ENTITY_NAME ALIAS_NAME? setClause whereClause
	;

setClause
	: SET assignment+
	;

assignment
	: VERSIONED
	| ATTRIBUTE_REFERENCE EQUAL expression
	;

deleteStatement
	: DELETE ENTITY_NAME ALIAS_NAME? whereClause
	;

insertStatement
// todo : lots of things
	: INSERT INTO ENTITY_NAME
	;

// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
// ORDER BY clause

orderByClause
	: ORDER_BY sortSpecification (COMMA sortSpecification)*
	;

sortSpecification
	:	sortKey COLLATE? ORDER_SPEC
	;

sortKey
	: expression
	;


// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
// QUERY SPEC - general structure of root query or sub query

querySpec
	:	selectClause fromClause whereClause? ( groupByClause havingClause? )?
	;


// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
// SELECT clause

selectClause
	:	SELECT DISTINCT? rootSelectExpression
	;

rootSelectExpression
	:	dynamicInstantiation
//	|	jpaSelectObjectSyntax
	|	explicitSelectList
	;

dynamicInstantiation
	:	DYNAMIC_INSTANTIATION dynamicInstantiationArgs
	;

dynamicInstantiationArgs
	:	dynamicInstantiationArg ( COMMA dynamicInstantiationArg )*
	;

dynamicInstantiationArg
	:	dynamicInstantiationArgExpression (ALIAS_NAME)?
	;

dynamicInstantiationArgExpression
	:	expression
	|	dynamicInstantiation
	;

explicitSelectList
	:	explicitSelectItem (COMMA explicitSelectItem)*
	;

explicitSelectItem
	:	expression (ALIAS_NAME)?
	;


// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
// FROM clause

fromClause
	: FROM persisterSpaces
	;

persisterSpaces
	:	persisterSpace (COMMA persisterSpace)*
	;

persisterSpace
//	:	persisterSpaceRoot ( qualifiedJoin | crossJoin )*
	:	persisterSpaceRoot
	;

persisterSpaceRoot
	:	mainEntityPersisterReference
//	|	hibernateLegacySyntax
//	|	jpaCollectionReference
	;

mainEntityPersisterReference
	: ENTITY_NAME ALIAS_NAME PROP_FETCH?
	;


// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
// GROUP BY clause

groupByClause
	:	GROUP_BY groupingSpecification
	;

groupingSpecification
	:	groupingValue (COMMA groupingValue)*
	;

groupingValue
	:	expression COLLATE?
	;


// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
//HAVING clause

havingClause
	:	HAVING logicalExpression
	;


// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
// WHERE clause

whereClause
	:	WHERE logicalExpression
	;

logicalExpression
	:	logicalExpression OR logicalExpression
	|	logicalExpression AND logicalExpression
	| 	NOT logicalExpression
	|   relationalExpression
	;

relationalExpression
	: expression IS (NOT)? (NULL | EMPTY)
	| expression (EQUAL | NOT_EQUAL | GREATER | GREATER_EQUAL | LESS | LESS_EQUAL) expression
	| expression IN inList
	| expression BETWEEN expression AND expression
	| expression LIKE expression likeEscape
	| expression MEMBER_OF expression
	;

expression
	: expression DOUBLE_PIPE expression		# ConcatenationExpression
	| expression PLUS expression			# AdditionExpression
	| expression MINUS expression			# SubtractionExpression
	| expression ASTERISK expression		# MultiplicationExpression
	| expression SLASH expression			# DivisionExpression
	| expression PERCENT expression			# ModuloExpression
	| MINUS expression						# UnaryMinusExpression
	| PLUS expression						# UnaryPlusExpression
	| literal								# LiteralExpression
	| parameter								# ParameterExpression
	| ALIAS_REFERENCE						# AliasReferenceExpression
	| ATTRIBUTE_REFERENCE					# AttributeReferenceExpression
	| JAVA_CONSTANT							# JavaConstantExpression
	| DISCRIMINATOR							# DiscriminatorExpression
	;

inList
	: ATTRIBUTE_REFERENCE
	| literalInList
	;

literalInList
	: expression (COMMA expression)*
	;

likeEscape
	: ESCAPE expression
	;

literal
	:	STRING_LITERAL
	|	CHARACTER_LITERAL
	|	INTEGER_LITERAL
	|	DECIMAL_LITERAL
	|	FLOATING_POINT_LITERAL
	|	HEX_LITERAL
	|	OCTAL_LITERAL
	| 	NULL
	| 	TRUE
	| 	FALSE
	;

parameter
	: NAMED_PARAM
	| POSITIONAL_PARAM
	| JPA_POSITIONAL_PARAM
	;