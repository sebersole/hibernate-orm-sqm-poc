TODO Items
----------

* Created a unified "alias registry" to make sure we do not get alias collisions between different clauses/contexts.  
For example, `select a.address as a from Anything as a`
* Possibly we should maintain a map from Expression -> "select alias" for substitution in other clauses.  For example,
given `select a.b + a.c as s from Anything a order by a.b + a.c` the more efficient query (SQL-wise) is a substitution to
`select a.b + a.c as s from Anything a order by s`.
* I just add `org.hibernate.hql.parser.model.BasicTypeDescriptor#getCorrespondingJavaType`.  It is very worthwhile to
know this information at the basic type level for many reasons such as validation operands of an operations, 
determining the result type of a arithmetic operation, etc.  We *could* move this up to `org.hibernate.hql.parser.model.TypeDescriptor`
but my concern was relying on this in the code considering de-typed models (i.e. MAP entity-mode).
* Proper handling for GroupedPredicate alternatives (explicit grouping parenthesis) - ATM I simply
created a GroupedPredicate class; maybe that is enough
* Proper identification of left and right hand side of joins, at least for joins with ON or WITH clauses.  See 
`org.hibernate.hql.parser.antlr.SemanticQueryBuilder#visitQualifiedJoinPredicate` for details.  Note that I keep
joins in a flat structure because its easier during the initial phase (frm clause processing); and in fact it might
be impossible to properly identify the left hand side of an "ad hoc" entity join.