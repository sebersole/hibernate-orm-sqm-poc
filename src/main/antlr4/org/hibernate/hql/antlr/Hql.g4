grammar Hql;

options {
	tokenVocab=HqlLexer;
}

@header {
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
	:
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
	:	rootDynamicInstantiation
//	|	jpaSelectObjectSyntax
	|	explicitSelectList
	;

rootDynamicInstantiation
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
			|	LEFT_SQUARE expression RIGHT_SQUARE
			|	LEFT_SQUARE RIGHT_SQUARE
		)*
	;

dynamicInstantiationArgs
	:	dynamicInstantiationArg ( COMMA dynamicInstantiationArg )*
	;

dynamicInstantiationArg
	:	selectExpression
	|	rootDynamicInstantiation
	;

//jpaSelectObjectSyntax
//	:	object_key LEFT_PAREN aliasReference RIGHT_PAREN
//	;

explicitSelectList
	:	explicitSelectItem ( COMMA explicitSelectItem )*
	;

explicitSelectItem
	:	selectExpression
	;

selectExpression
	// todo : I don't like this essentially makng AS required
	// but without that a query like:
	//		select a.b from A a
	// fails to parse properly, because it seems 'from' as the alias for the selectExpression
	// ugh!
	:	expression (as_key IDENTIFIER)?
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

collationSpecification
	:	collate_key collateName
	;

collateName
	:	dotIdentifierPath
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
	| expression SOLIDUS expression			# DivisionExpression
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
	:
	;

// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
// Key word rules


all_key
	:	{isUpcomingTokenKeyword("all")}?  id=IDENTIFIER 
	;

and_key
	:	{isUpcomingTokenKeyword("and")}?  id=IDENTIFIER 
	;

as_key
	:	{isUpcomingTokenKeyword("as")}?  id=IDENTIFIER 
	;

between_key
	:	{isUpcomingTokenKeyword("between")}?  id=IDENTIFIER
	;

collate_key
	:	{isUpcomingTokenKeyword("collate")}?  id=IDENTIFIER
	;

class_key
	:	{isUpcomingTokenKeyword("class")}?  id=IDENTIFIER
	;

distinct_key
	:	{isUpcomingTokenKeyword("distinct")}?  id=IDENTIFIER
	;

empty_key
	:	{isUpcomingTokenKeyword("escape")}?  id=IDENTIFIER
	;

escape_key
	:	{isUpcomingTokenKeyword("escape")}?  id=IDENTIFIER
	;

except_key
	:	{isUpcomingTokenKeyword("except")}?  id=IDENTIFIER
	;

fetch_key
	:	{isUpcomingTokenKeyword("fetch")}?  id=IDENTIFIER
	;

from_key
	:	{isUpcomingTokenKeyword("from")}?  id=IDENTIFIER
	;

group_by_key
	:	{isUpcomingTokenKeyword(1,"group") && isUpcomingTokenKeyword(2,"by")}? id=IDENTIFIER
	;

having_key
	:	{isUpcomingTokenKeyword("having")}?  id=IDENTIFIER
	;

in_key
	:	{isUpcomingTokenKeyword("in")}?  id=IDENTIFIER
	;

is_key
	:	{isUpcomingTokenKeyword("is")}?  id=IDENTIFIER
	;

intersect_key
	:	{isUpcomingTokenKeyword("intersect")}?  id=IDENTIFIER
	;

like_key
	:	{isUpcomingTokenKeyword("like")}?  id=IDENTIFIER
	;

member_of_key
	:	{isUpcomingTokenKeyword(1,"member") && isUpcomingTokenKeyword(2,"of")}? id=IDENTIFIER
	;

new_key
	:	{isUpcomingTokenKeyword("new")}?  id=IDENTIFIER
	;

not_key
	:	{isUpcomingTokenKeyword("not")}?  id=IDENTIFIER
	;

object_key
	:	{isUpcomingTokenKeyword("object")}?  id=IDENTIFIER
	;

or_key
	:	{isUpcomingTokenKeyword("or")}?  id=IDENTIFIER
	;

properties_key
	:	{isUpcomingTokenKeyword("properties")}?  id=IDENTIFIER
	;

select_key
	:	{isUpcomingTokenKeyword("select")}?  id=IDENTIFIER
	;

union_key
	:	{isUpcomingTokenKeyword("union")}?  id=IDENTIFIER
	;

where_key
	:	{isUpcomingTokenKeyword("where")}?  id=IDENTIFIER
	;
