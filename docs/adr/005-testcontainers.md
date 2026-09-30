# ADR 005 — Testcontainers everywhere, never H2

**Status:** Accepted

## Context
H2 hides PostgreSQL-specific behavior (JSONB, partial indexes, real SQL
planning) and teaches false confidence. The project also runs all tests inside
Docker without a local JVM.

## Decision
All integration tests use a JVM-wide singleton PostgreSQL 16 container
(`AbstractIntegrationTest`) with the real Flyway migrations; the datasource is
supplied via `@DynamicPropertySource`. When the test JVM itself runs in a
container, test containers join the `erp-network` Docker network and the tests
connect through the container IP, which works with rootless Podman where
published ports may be unreachable.

## Consequences
- Tests exercise the same database, drivers and constraints as production.
- The JSONB audit trail and `NUMERIC` math are covered for real.
- Slightly slower than H2 — accepted as the cost of meaningful tests.
