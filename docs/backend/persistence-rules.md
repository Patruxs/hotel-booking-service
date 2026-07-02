# Backend Persistence Rules

The PostgreSQL schema is managed by Flyway. Hibernate must not create, update, or validate the production schema on startup; `spring.jpa.hibernate.ddl-auto` stays `none` for active backend profiles.

JPA remains available for entity mapping, transactions, relationships, and simple CRUD repositories.

Custom persistence logic must use raw SQL, preferably through `NamedParameterJdbcTemplate`. Repository-local native queries are allowed only for small lookups or deletes. JPQL/HQL custom queries are not allowed because migration-critical behavior must stay explicit against PostgreSQL and reviewable beside the Flyway schema.

The current legacy repositories still contain JPQL queries for the pre-migration integer-ID model. They are listed in `docs/backend/legacy-jpql-debt.md` and may stay only until the owning module is migrated to the PostgreSQL schema. New custom queries must not add to that list.

Stable system authorization rows, including roles, permissions, API actions, and action policies, are seeded by Flyway with fixed UUID literals. Profile-specific demo data must not be mixed into Flyway's required catalog rows.
