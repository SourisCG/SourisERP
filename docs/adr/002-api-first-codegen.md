# ADR 002 — API-First with generated server interfaces

**Status:** Accepted

## Context
Hand-written controllers drift from documentation. The project wants the API
contract to be verifiable and the Swagger UI to show exactly what is implemented.

## Decision
`src/main/resources/api/openapi.yaml` (OpenAPI 3.1) is the single source of
truth. `openapi-generator-maven-plugin` generates Spring interfaces
(`useSpringBoot4` + `useJackson3`), and controllers implement them. Swagger UI
serves the YAML file itself.

## Consequences
- Changing the contract is a deliberate, reviewable step.
- Generated code is never edited (it lives in `target/`).
- Codegen is pinned (7.25.0) and validated in CI by compiling from a clean state.
