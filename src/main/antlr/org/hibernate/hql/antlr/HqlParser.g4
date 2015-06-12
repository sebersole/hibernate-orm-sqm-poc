parser grammar HqlParser;

options {
	tokenVocab=HqlLexer;
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
	: ( selectStatement | updateStatement | deleteStatement | insertStatement ) EOF
	;

selectStatement
	: queryExpression orderByClause?
	;

updateStatement
// todo : add set-clause
	: updateKeyword FROM? mainEntityPersisterReference whereClause
	;

deleteStatement
	: deleteKeyword FROM? mainEntityPersisterReference whereClause
	;

insertStatement
// todo : lots of things
	: insertKeyword INTO insertTarget
	;

insertTarget
	: dotIdentifierPath
	;

queryExpression
	:	querySpec ( ( unionKeyword | intersectKeyword | exceptKeyword ) allKeyword? querySpec )*
	;


// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
// ORDER BY clause

orderByClause
	: orderByKeyword sortSpecification (COMMA sortSpecification)*
	;

sortSpecification
	:	sortKeyword collationSpecification? orderingSpecification?
	;

sortKeyword
	: expression
	;

collationSpecification
	:	collateKeyword collateName
	;

collateName
	:	dotIdentifierPath
	;

orderingSpecification
	:	ascendingKeyword
	|	descendingKeyword
	;

// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
// QUERY SPEC - general structure of root query or sub query

querySpec
	:	selectClause? fromClause whereClause? ( groupByClause havingClause? )?
	;


// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
// SELECT clause

selectClause
	:	selectKeyword distinctKeyword? rootSelectExpression
	;

rootSelectExpression
	:	dynamicInstantiation
	|	jpaSelectObjectSyntax
	|	explicitSelectList
	;

dynamicInstantiation
	:	newKeyword dynamicInstantiationTarget LEFT_PAREN dynamicInstantiationArgs RIGHT_PAREN
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
	:	dynamicInstantiationArgExpression (asKeyword? IDENTIFIER)?
	;

dynamicInstantiationArgExpression
	:	expression
	|	dynamicInstantiation
	;

jpaSelectObjectSyntax
	:	objectKeyword LEFT_PAREN IDENTIFIER RIGHT_PAREN
	;

explicitSelectList
	:	explicitSelectItem (COMMA explicitSelectItem)*
	;

explicitSelectItem
	// I have noticed thaty without this predicate, Antlr will sometimes
	// interpret `select a.b from Something ...` as `from` being the
	// select-expression alias
	:	expression (asKeyword? {!isUpcomingTokenKeyword("from")}? IDENTIFIER)?
	;


// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
// FROM clause

fromClause
	: fromKeyword fromElementSpace (COMMA fromElementSpace)*
	;

fromElementSpace
	:	fromElementSpaceRoot ( qualifiedJoin | crossJoin )*
	;

fromElementSpaceRoot
	:	mainEntityPersisterReference	# RootEntityReference
//	|	hibernateLegacySyntax
//	|	jpaCollectionReference
	;

mainEntityPersisterReference
	:	dotIdentifierPath (asKeyword? {!isUpcomingTokenKeyword("where")}? IDENTIFIER)?
	;

crossJoin
	: crossKeyword joinKeyword mainEntityPersisterReference
	;

qualifiedJoin
	: joinKeyword fetchKeyword? qualifiedJoinRhs								# ImplicitInnerJoinType
	| innerKeyword joinKeyword	fetchKeyword? qualifiedJoinRhs					# ExplicitInnerJoinType
	| leftKeyword? outerKeyword joinKeyword	fetchKeyword? qualifiedJoinRhs		# ExplicitOuterJoinType
	;

qualifiedJoinRhs
	: dotIdentifierPath (asKeyword? IDENTIFIER)? ( onKeyword | withKeyword) logicalExpression
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
	:	groupByKeyword groupingSpecification
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
	:	havingKeyword logicalExpression
	;


// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
// WHERE clause

whereClause
	:	whereKeyword logicalExpression
	;

logicalExpression
	: logicalExpression orKeyword logicalExpression					# LogicalOrExpression
	| logicalExpression andKeyword logicalExpression				# LogicalAndExpression
	| notKeyword logicalExpression									# LogicalNegatedExpression
	| expression isKeyword (notKeyword)? NULL						# LogicalIsNullExpression
	| expression isKeyword (notKeyword)? emptyKeyword				# LogicalIsEmptyExpression
	| expression EQUAL expression									# LogicalEqualityExpression
	| expression NOT_EQUAL expression								# LogicalInequalityExpression
	| expression GREATER expression									# LogicalGreaterThanExpression
	| expression GREATER_EQUAL expression							# LogicalGreaterThanOrEqualExpression
	| expression LESS expression									# LogicalLessThanExpression
	| expression LESS_EQUAL expression								# LogicalLessThanOrEqualExpression
	| expression IN inList											# LogicalInExpression
	| expression between_key expression andKeyword expression		# LogicalBetweenExpression
	| expression likeKeyword expression likeEscape					# LogicalLikeExpression
	| memberOfKeyword path											# LogicalMemberOfExpression
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
	:	elementsKeyword? dotIdentifierPath					# PersistentCollectionReferenceInList
	| 	expression (COMMA expression)*						# ExplicitTupleInList
	;

likeEscape
	: escapeKeyword expression
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
	: COLON IDENTIFIER						# NamedParameter
	| QUESTION_MARK (INTEGER_LITERAL)?		# JpaPositionalParameter
	| QUESTION_MARK							# PositionalParameter
	;


// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
// Key word rules


allKeyword
	: {isUpcomingTokenKeyword("all")}? IDENTIFIER
	;

andKeyword
	: {isUpcomingTokenKeyword("and")}? IDENTIFIER
	;

asKeyword
	: {isUpcomingTokenKeyword("as")}? IDENTIFIER
	;

ascendingKeyword
	: {(isUpcomingTokenKeyword("ascending") || isUpcomingTokenKeyword("asc"))}? IDENTIFIER
	;

between_key
	: {isUpcomingTokenKeyword("between")}? IDENTIFIER
	;

classKeyword
	: {isUpcomingTokenKeyword("class")}? IDENTIFIER
	;

collateKeyword
	: {isUpcomingTokenKeyword("collate")}? IDENTIFIER
	;

crossKeyword
	: {isUpcomingTokenKeyword("cross")}? IDENTIFIER
	;

deleteKeyword
	: {isUpcomingTokenKeyword("delete")}? IDENTIFIER
	;

descendingKeyword
	: {(isUpcomingTokenKeyword("descending") || isUpcomingTokenKeyword("desc"))}? IDENTIFIER
	;

distinctKeyword
	: {isUpcomingTokenKeyword("distinct")}? IDENTIFIER
	;

elementsKeyword
	: {isUpcomingTokenKeyword("elements")}? IDENTIFIER
	;

emptyKeyword
	: {isUpcomingTokenKeyword("escape")}? IDENTIFIER
	;

escapeKeyword
	: {isUpcomingTokenKeyword("escape")}? IDENTIFIER
	;

exceptKeyword
	: {isUpcomingTokenKeyword("except")}? IDENTIFIER
	;

fetchKeyword
	: {isUpcomingTokenKeyword("fetch")}? IDENTIFIER
	;

fromKeyword
	: {isUpcomingTokenKeyword("from")}? IDENTIFIER
	;

groupByKeyword
	: {isUpcomingTokenKeyword(1,"group") && isUpcomingTokenKeyword(2,"by")}? IDENTIFIER IDENTIFIER
	;

havingKeyword
	: {isUpcomingTokenKeyword("having")}? IDENTIFIER
	;

inKeyword
	: {isUpcomingTokenKeyword("in")}? IDENTIFIER
	;

innerKeyword
	: {isUpcomingTokenKeyword("inner")}? IDENTIFIER
	;

insertKeyword
	: {isUpcomingTokenKeyword("insert")}? IDENTIFIER
	;

isKeyword
	: {isUpcomingTokenKeyword("is")}? IDENTIFIER
	;

intersectKeyword
	: {isUpcomingTokenKeyword("intersect")}? IDENTIFIER
	;

joinKeyword
	: {isUpcomingTokenKeyword("join")}? IDENTIFIER
	;

leftKeyword
	: {isUpcomingTokenKeyword("left")}?  IDENTIFIER
	;

likeKeyword
	: {isUpcomingTokenKeyword("like")}?  IDENTIFIER
	;

memberOfKeyword
	: {isUpcomingTokenKeyword(1,"member") && isUpcomingTokenKeyword(2,"of")}?  IDENTIFIER IDENTIFIER
	;

newKeyword
	: {isUpcomingTokenKeyword("new")}?  IDENTIFIER
	;

notKeyword
	: {isUpcomingTokenKeyword("not")}?  IDENTIFIER
	;

objectKeyword
	: {isUpcomingTokenKeyword("object")}?  IDENTIFIER
	;

onKeyword
	: {isUpcomingTokenKeyword("on")}?  IDENTIFIER
	;

orKeyword
	: {isUpcomingTokenKeyword("or")}?  IDENTIFIER
	;

orderByKeyword
	: {(isUpcomingTokenKeyword("order") && isUpcomingTokenKeyword(2, "by"))}?  IDENTIFIER IDENTIFIER
	;

outerKeyword
	: {isUpcomingTokenKeyword("outer")}?  IDENTIFIER
	;

propertiesKeyword
	: {isUpcomingTokenKeyword("properties")}?  IDENTIFIER
	;

selectKeyword
	: {isUpcomingTokenKeyword("select")}?  IDENTIFIER
	;

unionKeyword
	: {isUpcomingTokenKeyword("union")}?  IDENTIFIER
	;

updateKeyword
	: {isUpcomingTokenKeyword("update")}?  IDENTIFIER
	;

whereKeyword
	: {isUpcomingTokenKeyword("where")}?  IDENTIFIER
	;

withKeyword
	: {isUpcomingTokenKeyword("with")}?  IDENTIFIER
	;
