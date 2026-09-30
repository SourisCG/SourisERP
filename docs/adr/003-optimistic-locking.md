# ADR 003 — Optimistic locking with retry for stock

**Status:** Accepted

## Context
Concurrent sales orders may try to reserve the same units. Overselling must be
impossible, and the losing request must fail gracefully with a clear message.

## Decision
`inventory_items` carries a `version` column (`@Version`). Stock mutations are
`@Transactional` and `@Retryable` on `OptimisticLockingFailureException` (3
attempts, 50 ms backoff). Retry is configured *outside* the transaction
(`@EnableRetry(order = HIGHEST_PRECEDENCE)`) so each attempt reloads fresh state.
If stock is genuinely gone, the domain throws `409 error.inventory.insufficientStock`.

## Consequences
- Proven by a two-thread integration test (`concurrent_reservations_do_not_oversell_the_last_unit`).
- No pessimistic locks or serialized queues needed for this workload.
- The pattern generalizes to any aggregate (orders, invoices, products all carry `version`).
