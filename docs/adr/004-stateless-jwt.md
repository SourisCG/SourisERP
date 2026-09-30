# ADR 004 — Stateless JWT with Nimbus and role claims

**Status:** Accepted

## Context
The demo is public: sessions must be stateless, cheap and safe. Roles drive
module permissions (ADMIN, SALES, WAREHOUSE, ACCOUNTANT, VIEWER).

## Decision
Use `spring-boot-starter-oauth2-resource-server` with `NimbusJwtEncoder/Decoder`
(HS256, secret from the environment). `/auth/login` issues short-lived access
tokens carrying `roles`; `JwtAuthenticationConverter` maps them to `ROLE_*`
authorities consumed by `@PreAuthorize`. Login is rate-limited (sliding window,
429 problem+json).

## Consequences
- No server-side session store; horizontal scaling is trivial.
- The Next.js dashboard stores the token in an httpOnly cookie and proxies
  requests server-side, so the token never touches browser JavaScript.
- Refresh tokens are intentionally out of scope for the demo (30 min TTL).
