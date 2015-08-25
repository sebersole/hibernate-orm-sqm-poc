TODO Items
----------

* Created a unified "alias registry" to make sure we do not get alias collisions between different clauses/contexts.  
	For example, `select a.address as a from Anything as a`
* Possibly we should maintain a map from Expression -> "select alias" for substitution in other clauses.  For example,
	given `select a.b + a.c as s from Anything a order by a.b + a.c` the more efficient query (SQL-wise) is a substitution to
	`select a.b + a.c as s from Anything a order by s`.
* I just add `org.hibernate.sqm.domain.BasicTypeDescriptor#getCorrespondingJavaType`.  It is very worthwhile to
	know this information at the basic type level for many reasons such as validation operands of an operations, 
	determining the result type of a arithmetic operation, etc.  We *could* move this up to `org.hibernate.sqm.domain.TypeDescriptor`
	but my concern was relying on this in the code considering de-typed models (i.e. MAP entity-mode).
* Proper handling for GroupedPredicate alternatives (explicit grouping parenthesis) - ATM I simply
	created a GroupedPredicate class; maybe that is enough
* Proper identification of left and right hand side of joins, at least for joins with ON or WITH clauses.  See 
	`org.hibernate.query.parser.internal.hql.antlr.SemanticQueryBuilder#visitQualifiedJoinPredicate` for details.  Note that I keep
	joins in a flat structure because its easier during the initial phase (frm clause processing); and in fact it might
	be impossible to properly identify the left hand side of an "ad hoc" entity join.
* TREAT should be journaled into the respective FromElement along with some concept of where it came from (because ultimately that
  	affects its rendering into SQL).  For TREAT in SELECT we may still need a wrapper (see next point too)
* Make sure that FromElements are NEVER used directly in other parts of the query.  All references to a FromElement in
	another part of the query should always be "wrapped" in another type (FromElementReferenceExpression, e.g.).  Part
	of this is that I do not think its a good idea for all FromElement types (via org.hibernate.sqm.path.AttributePathPart) 
	to be Expressions; that change has some bearing on the org.hibernate.query.parser.internal.hql.path.AttributePathResolver
	code.