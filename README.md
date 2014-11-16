Antlr v4 PoC
============

This project represents a proof-of-concept (PoC) of using Antlr v4 as the basis for the redesign of the HQL (/JPQL)
parser used in Hibernate.  The initial Antlr-based translator used v2.  The initial redesign work used v3, as that
was the latest release at that time.  Both rely heavily on the principals of tree parsing and tree re-writing, two
concepts that are no longer available in Antlr v4.

The PoC then, is meant to determine how Antlr v4 might be used to address the HQL translation needs and
whether that is the route we want to go.

There are 2 possible approaches this PoC will demonstrate: AST and decoration...

AST
---

The premise here is very similar to the design of our Antlr v2/v3 approach.  Basically it mimics the idea of
a tree parser.  The idea is to write multiple grammars.  The first grammar is the parse grammar and builds the
parse tree.  We use the Antlr-generator walker + visitor to produce a new, more semantically rich tree: an AST.
However, this AST would also be a parse tree by nature of implementing the proper Antlr contracts.  The second
grammar would be designed such that it would produce walker + visitor capable of walking this AST tree.

We could have as many phases (subsequent grammars) as we want here.  Really, I think the 2 approaches could even be
combined.

Decoration
----------

The idea behind decoration is to stuff semantic information (payload) into pertinent points in the parse
tree.  The walking is still done using the parse tree structure, but using the visitor you would decide
whether to continue down a particular sub-tree or not.  For example, given a parse tree like:

    [QUERY]
      [SELECT]
        [SELECT_ITEM]
          [DOT]
            [DOT]
              [DOT]
                [IDENT, "c"]
                [IDENT, "headquarters"]
              [IDENT, "state"]
            [IDENT, "code"]
    ...

We would push semantic information into the `[SELECT_ITEM]` node indicating the "resolution" of the
expression.  The grammar, the rules, the walker and the visitor/listener would still be stated in
relation to the less expressive model; we would just make decisions in the visitor based on the encountered
payload.

This is a completely different approach, and so I am completely unfamiliar with what risks it might entail.

Details
-------

The PoC of both approaches start off with the same grammar: Hql.g4.  This grammar uses HqlLexer.g4 as its lexer
grammar, which defines a complete set of tokens used across all phases of translation.  Later we may want to look
at breaking that up into tokens needed just for input parsing/recognition versus strictly semantic tokens.  But
for now, we will use the comp[lete common set of tokens.

_It is also important to note that this PoC defines a subset of HQL grammar.  It is not meant as THE redesign,
just a PoC of how Antlr v4 might be used in the redesign._

