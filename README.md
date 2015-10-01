Hibernate ORM and SQM Integration PoC
=====================================

This project represents a proof-of-concept (PoC) for integrating SQM into Hibernate ORM as the basis for SQL generation.  A big part of this requires some changes to Hibernate ORM internals to better expose the table/column mapping information.  So the PoC acts as a sounding board, too, for these proposed changes.  As much as possible each proposed change should have a high-level discussion (as a GitHub repo wiki).

See also the [SQM repo](https://github.com/hibernate/hibernate-semantic-query) for any SQM design discussions.  There is obviously a lot of interplay.

Proposed Changes
----------------

* Persisters (entiyt and colection) -> https://github.com/sebersole/hibernate-orm-sqm-poc/wiki/Proposed-changes-to-persisters
* JPA type model -> https://github.com/sebersole/hibernate-orm-sqm-poc/wiki/Proposed-changes-to-JPA-type-model
