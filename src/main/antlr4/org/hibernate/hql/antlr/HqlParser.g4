parser grammar HqlParser;

options {
	tokenVocab=HqlLexer;
}

@header {
/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * Copyright (c) 2008-2012, Red Hat Inc. or third-party contributors as
 * indicated by the @author tags or express copyright attribution
 * statements applied by the authors.  All third-party contributions are
 * distributed under license by Red Hat Inc.
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
package org.hibernate.hql.antlr;
}

@members {
	/**
	 * Determine if the text of the new upcoming token LT(1), if one, matches
	 * the passed argument.  Internally calls isUpcomingTokenKeyword( 1, tokenText )
	 */
	protected boolean isUpcomingTokenKeyword(String tokenText) {
		return isUpcomingTokenKeyword( 1, tokenText );
	}

	/**
	 * Determine if the text of the new upcoming token LT(offset), if one, matches
	 * the passed argument.
	 */
	protected boolean isUpcomingTokenKeyword(int offset, String keyword) {
		final Token token = retrieveUpcomingToken( offset );
		if ( token == null ) {
			return false;
		}

		if ( token.getType() != IDENTIFIER ) {
			// todo : is this really a check we want?
			return false;
		}

		final String textToValidate = token.getText();
		if ( textToValidate == null ) {
			return false;
		}
		return textToValidate.equalsIgnoreCase( keyword );
	}

	protected Token retrieveUpcomingToken(int offset) {
		if ( null == _input ) {
			return null;
		}
		return _input.LT( offset );
	}

	protected String retrieveUpcomingTokenText(int offset) {
		Token token = retrieveUpcomingToken( offset );
		return token == null ? null : token.getText();
	}
}

statement
	: selectStatement
//	| updateStatement
//	| deleteStateent
	;

selectStatement
	: queryExpression orderByClause?
	;

queryExpression
	:	querySpec ( ( union_key | intersect_key | except_key ) all_key? querySpec )*
	;


// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
// ORDER BY clause

orderByClause
	: order_by_key sortSpecification (COMMA sortSpecification)*
	;

sortSpecification
	:	sortKey collationSpecification? orderingSpecification?
	;

sortKey
	: expression
	;

collationSpecification
	:	collate_key collateName
	;

collateName
	:	dotIdentifierPath
	;

orderingSpecification
	:	ascending_key
	|	descending_key
	;

// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
// QUERY SPEC - general structure of root query or sub query

querySpec
	:	selectClause? fromClause whereClause? ( groupByClause havingClause? )?
	;


// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
// SELECT clause

selectClause
	:	select_key distinct_key? rootSelectExpression
	;

rootSelectExpression
	:	dynamicInstantiation
//	|	jpaSelectObjectSyntax
	|	explicitSelectList
	;

dynamicInstantiation
	:	new_key dynamicInstantiationTarget LEFT_PAREN dynamicInstantiationArgs RIGHT_PAREN
	;

dynamicInstantiationTarget
	:	dotIdentifierPath
	;


dotIdentifierPath
	:	IDENTIFIER (DOT IDENTIFIER)*
	;

path
	:	IDENTIFIER
		(
				DOT IDENTIFIER
			|	LEFT_BRACKET expression RIGHT_BRACKET
			|	LEFT_BRACKET RIGHT_BRACKET
		)*
	;

dynamicInstantiationArgs
	:	dynamicInstantiationArg ( COMMA dynamicInstantiationArg )*
	;

dynamicInstantiationArg
	:	dynamicInstantiationArgExpression (as_key IDENTIFIER)?
	;

dynamicInstantiationArgExpression
	:	selectExpression
	|	dynamicInstantiation
	;

//jpaSelectObjectSyntax
//	:	object_key LEFT_PAREN aliasReference RIGHT_PAREN
//	;

explicitSelectList
	:	explicitSelectItem (COMMA explicitSelectItem)*
	;

explicitSelectItem
	:	selectExpression (as_key IDENTIFIER)?
	;

selectExpression
	:	expression
	;

aliasReference
	:	IDENTIFIER
	;


// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
// FROM clause

fromClause
	: from_key persisterSpaces
	;

persisterSpaces
//	:	persisterSpace ( COMMA persisterSpace )*
	:	persisterSpace
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
//	:	entityName ac=aliasClause[true] propertyFetch?
	:	dotIdentifierPath as_key? IDENTIFIER
	;

propertyFetch
	:	fetch_key all_key properties_key
	;

//hibernateLegacySyntax returns [boolean isPropertyJoin]
//@init {$isPropertyJoin = false;}
//	:	ad=aliasDeclaration in_key
//	(
//			class_key entityName
//		|	collectionExpression {$isPropertyJoin = true;}
//	)
//	;
//
//jpaCollectionReference
//	:	in_key LEFT_PAREN propertyReference RIGHT_PAREN ac=aliasClause[true]
//	;


// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
// GROUP BY clause

groupByClause
	:	group_by_key groupingSpecification
	;

groupingSpecification
	:	groupingValue ( COMMA groupingValue )*
	;

groupingValue
	:	expression collationSpecification?
	;

// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
//HAVING clause

havingClause
	:	having_key logicalExpression
	;


// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
// WHERE clause

whereClause
	:	where_key logicalExpression
	;

logicalExpression
	:	logicalExpression or_key logicalExpression
	|	logicalExpression and_key logicalExpression
	| 	not_key logicalExpression
	|   relationalExpression
	;

relationalExpression
	: expression is_key (not_key)? (NULL | empty_key)
	| expression (EQUAL | NOT_EQUAL | GREATER | GREATER_EQUAL | LESS | LESS_EQUAL) expression
	| expression IN inList
	| expression between_key expression and_key expression
	| expression like_key expression likeEscape
	| member_of_key path
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
	| dotIdentifierPath						# DotIdentExpression
	;

inList
	:	dotIdentifierPath
	| 	expression (COMMA expression)*
	;

likeEscape
	: escape_key expression
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
	: COLON IDENTIFIER
	| QUESTION_MARK (INTEGER_LITERAL)?
	;


// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
// Key word rules


all_key
	:	{isUpcomingTokenKeyword("all")}?  IDENTIFIER
	;

and_key
	:	{isUpcomingTokenKeyword("and")}?  IDENTIFIER
	;

as_key
	:	{isUpcomingTokenKeyword("as")}?  IDENTIFIER
	;

ascending_key
	:	{(isUpcomingTokenKeyword("ascending") || isUpcomingTokenKeyword("asc"))}?  IDENTIFIER
	;

between_key
	:	{isUpcomingTokenKeyword("between")}?  IDENTIFIER
	;

collate_key
	:	{isUpcomingTokenKeyword("collate")}?  IDENTIFIER
	;

class_key
	:	{isUpcomingTokenKeyword("class")}?  IDENTIFIER
	;

descending_key
	:	{(isUpcomingTokenKeyword("descending") || isUpcomingTokenKeyword("desc"))}?  IDENTIFIER
	;

distinct_key
	:	{isUpcomingTokenKeyword("distinct")}?  IDENTIFIER
	;

empty_key
	:	{isUpcomingTokenKeyword("escape")}?  IDENTIFIER
	;

escape_key
	:	{isUpcomingTokenKeyword("escape")}?  IDENTIFIER
	;

except_key
	:	{isUpcomingTokenKeyword("except")}?  IDENTIFIER
	;

fetch_key
	:	{isUpcomingTokenKeyword("fetch")}?  IDENTIFIER
	;

from_key
	:	{isUpcomingTokenKeyword("from")}?  IDENTIFIER
	;

group_by_key
	:	{isUpcomingTokenKeyword(1,"group") && isUpcomingTokenKeyword(2,"by")}?  IDENTIFIER IDENTIFIER
	;

having_key
	:	{isUpcomingTokenKeyword("having")}?  IDENTIFIER
	;

in_key
	:	{isUpcomingTokenKeyword("in")}?  IDENTIFIER
	;

is_key
	:	{isUpcomingTokenKeyword("is")}?  IDENTIFIER
	;

intersect_key
	:	{isUpcomingTokenKeyword("intersect")}?  IDENTIFIER
	;

like_key
	:	{isUpcomingTokenKeyword("like")}?  IDENTIFIER
	;

member_of_key
	:	{isUpcomingTokenKeyword(1,"member") && isUpcomingTokenKeyword(2,"of")}?  IDENTIFIER IDENTIFIER
	;

new_key
	:	{isUpcomingTokenKeyword("new")}?  IDENTIFIER
	;

not_key
	:	{isUpcomingTokenKeyword("not")}?  IDENTIFIER
	;

object_key
	:	{isUpcomingTokenKeyword("object")}?  IDENTIFIER
	;

or_key
	:	{isUpcomingTokenKeyword("or")}?  IDENTIFIER
	;

order_by_key
	:	{(isUpcomingTokenKeyword("order") && isUpcomingTokenKeyword(2, "by"))}?  IDENTIFIER IDENTIFIER
	;

properties_key
	:	{isUpcomingTokenKeyword("properties")}?  IDENTIFIER
	;

select_key
	:	{isUpcomingTokenKeyword("select")}?  IDENTIFIER
	;

union_key
	:	{isUpcomingTokenKeyword("union")}?  IDENTIFIER
	;

where_key
	:	{isUpcomingTokenKeyword("where")}?  IDENTIFIER
	;
