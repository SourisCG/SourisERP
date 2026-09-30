# ADR 001 — Hexagonal architecture in a single Maven module

**Status:** Accepted

## Context
The project must demonstrate a clean separation between business rules and
technology, while staying approachable for reviewers who clone and run it.

## Decision
Use a single Maven module with strict package boundaries
(`domain`, `application`, `infrastructure`, `config`) instead of multi-module
Maven. Enforce the Dependency Rule with ArchUnit tests that fail the build.

## Consequences
- Reviewers can navigate the code in minutes; no module ceremony.
- The rule is machine-checked, not aspirational.
- If the project grows, packages can be extracted into modules without moving code.
