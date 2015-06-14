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
	 * the passed argument.  Internally calls doesUpcomingTokenMatchAny( 1, checks )
	 */
	protected boolean doesUpcomingTokenMatchAny(String... checks) {
		return doesUpcomingTokenMatchAny( 1, checks );
	}

	/**
	 * Determine if the text of the new upcoming token LT(offset), if one, matches
	 * the passed argument.
	 */
	protected boolean doesUpcomingTokenMatchAny(int offset, String... checks) {
		final Token token = retrieveUpcomingToken( offset );
		if ( token != null ) {
			if ( token.getType() == IDENTIFIER ) {
				// todo : is this really a check we want?

				final String textToValidate = token.getText();
				if ( textToValidate != null ) {
					for ( String check : checks ) {
						if ( textToValidate.equalsIgnoreCase( check ) ) {
							return true;
						}
					}
				}
			}
		}

		return false;
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
	: querySpec
//	:	querySpec ( ( unionKeyword | intersectKeyword | exceptKeyword ) allKeyword? querySpec )*
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
	:	expression (asKeyword? {!doesUpcomingTokenMatchAny("from")}? IDENTIFIER)?
	;


// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
// FROM clause

fromClause
	: fromKeyword fromElementSpace (COMMA fromElementSpace)*
	;

fromElementSpace
	:	fromElementSpaceRoot ( crossJoin | qualifiedJoin )*
	;

fromElementSpaceRoot
	:	mainEntityPersisterReference	# RootEntityReference
//	|	hibernateLegacySyntax
//	|	jpaCollectionReference
	;

mainEntityPersisterReference
	:	dotIdentifierPath (asKeyword? {!doesUpcomingTokenMatchAny("where","join")}? IDENTIFIER)?
	;

crossJoin
	: crossKeyword joinKeyword mainEntityPersisterReference
	;

qualifiedJoin
//	: ( innerKeyword | (leftKeyword? outerKeyword) )? joinKeyword fetchKeyword? qualifiedJoinRhs
	: joinKeyword fetchKeyword? qualifiedJoinRhs															# ImplicitInnerJoin
	| innerKeyword joinKeyword	fetchKeyword? qualifiedJoinRhs												# ExplicitInnerJoin
	| (leftKeyword|rightKeyword|fullKeyword)? outerKeyword joinKeyword	fetchKeyword? qualifiedJoinRhs		# ExplicitOuterJoin
	;

qualifiedJoinRhs
	: dotIdentifierPath (asKeyword? IDENTIFIER)? ( (onKeyword | withKeyword) logicalExpression )?
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
	: {doesUpcomingTokenMatchAny("all")}? IDENTIFIER
	;

andKeyword
	: {doesUpcomingTokenMatchAny("and")}? IDENTIFIER
	;

asKeyword
	: {doesUpcomingTokenMatchAny("as")}? IDENTIFIER
	;

ascendingKeyword
	: {(doesUpcomingTokenMatchAny("ascending","asc"))}? IDENTIFIER
	;

between_key
	: {doesUpcomingTokenMatchAny("between")}? IDENTIFIER
	;

classKeyword
	: {doesUpcomingTokenMatchAny("class")}? IDENTIFIER
	;

collateKeyword
	: {doesUpcomingTokenMatchAny("collate")}? IDENTIFIER
	;

crossKeyword
	: {doesUpcomingTokenMatchAny("cross")}? IDENTIFIER
	;

deleteKeyword
	: {doesUpcomingTokenMatchAny("delete")}? IDENTIFIER
	;

descendingKeyword
	: {(doesUpcomingTokenMatchAny("descending","desc"))}? IDENTIFIER
	;

distinctKeyword
	: {doesUpcomingTokenMatchAny("distinct")}? IDENTIFIER
	;

elementsKeyword
	: {doesUpcomingTokenMatchAny("elements")}? IDENTIFIER
	;

emptyKeyword
	: {doesUpcomingTokenMatchAny("escape")}? IDENTIFIER
	;

escapeKeyword
	: {doesUpcomingTokenMatchAny("escape")}? IDENTIFIER
	;

exceptKeyword
	: {doesUpcomingTokenMatchAny("except")}? IDENTIFIER
	;

fetchKeyword
	: {doesUpcomingTokenMatchAny("fetch")}? IDENTIFIER
	;

fromKeyword
	: {doesUpcomingTokenMatchAny("from")}? IDENTIFIER
	;

fullKeyword
	: {doesUpcomingTokenMatchAny("full")}? IDENTIFIER
	;

groupByKeyword
	: {doesUpcomingTokenMatchAny(1,"group") && doesUpcomingTokenMatchAny(2,"by")}? IDENTIFIER IDENTIFIER
	;

havingKeyword
	: {doesUpcomingTokenMatchAny("having")}? IDENTIFIER
	;

inKeyword
	: {doesUpcomingTokenMatchAny("in")}? IDENTIFIER
	;

innerKeyword
	: {doesUpcomingTokenMatchAny("inner")}? IDENTIFIER
	;

insertKeyword
	: {doesUpcomingTokenMatchAny("insert")}? IDENTIFIER
	;

isKeyword
	: {doesUpcomingTokenMatchAny("is")}? IDENTIFIER
	;

intersectKeyword
	: {doesUpcomingTokenMatchAny("intersect")}? IDENTIFIER
	;

joinKeyword
	: {doesUpcomingTokenMatchAny("join")}? IDENTIFIER
	;

leftKeyword
	: {doesUpcomingTokenMatchAny("left")}?  IDENTIFIER
	;

likeKeyword
	: {doesUpcomingTokenMatchAny("like")}?  IDENTIFIER
	;

memberOfKeyword
	: {doesUpcomingTokenMatchAny(1,"member") && doesUpcomingTokenMatchAny(2,"of")}?  IDENTIFIER IDENTIFIER
	;

newKeyword
	: {doesUpcomingTokenMatchAny("new")}?  IDENTIFIER
	;

notKeyword
	: {doesUpcomingTokenMatchAny("not")}?  IDENTIFIER
	;

objectKeyword
	: {doesUpcomingTokenMatchAny("object")}?  IDENTIFIER
	;

onKeyword
	: {doesUpcomingTokenMatchAny("on")}?  IDENTIFIER
	;

orKeyword
	: {doesUpcomingTokenMatchAny("or")}?  IDENTIFIER
	;

orderByKeyword
	: {(doesUpcomingTokenMatchAny("order") && doesUpcomingTokenMatchAny(2, "by"))}?  IDENTIFIER IDENTIFIER
	;

outerKeyword
	: {doesUpcomingTokenMatchAny("outer")}?  IDENTIFIER
	;

propertiesKeyword
	: {doesUpcomingTokenMatchAny("properties")}?  IDENTIFIER
	;

rightKeyword
	: {doesUpcomingTokenMatchAny("right")}?  IDENTIFIER
	;

selectKeyword
	: {doesUpcomingTokenMatchAny("select")}?  IDENTIFIER
	;

unionKeyword
	: {doesUpcomingTokenMatchAny("union")}?  IDENTIFIER
	;

updateKeyword
	: {doesUpcomingTokenMatchAny("update")}?  IDENTIFIER
	;

whereKeyword
	: {doesUpcomingTokenMatchAny("where")}?  IDENTIFIER
	;

withKeyword
	: {doesUpcomingTokenMatchAny("with")}?  IDENTIFIER
	;
