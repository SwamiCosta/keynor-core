# Skill: Security Model and OAuth2 Bootstrap

> Covers keynor-core's Authorization Server / Resource Server model — roles, token flow, filter chain ordering, CORS — and the bootstrap procedure for obtaining the first token. Read before modifying anything under `infrastructure/security/` or before any developer/agent needs API access for the first time.

---

## Roles

| Role | Grantee | Grant type |
|------|---------|------------|
| `ADMIN` | Human users (admin panel / RPG integration) | `authorization_code` + PKCE, form login |
| `SYSTEM` | Service-to-service calls (keynor-rpg, aniannoth, etc.) | `client_credentials` |

Both roles have full access to all `/api/**` endpoints by default. No hierarchy between them. **Exception:** a specific endpoint may be restricted to `ADMIN` only via `@PreAuthorize("hasRole('ADMIN')")` on the controller method (first used by `InternalMapPinController`'s create/delete, PR — map pins feature) — see "Role claim" below for how `ADMIN` becomes a checkable authority.

---

## Role claim (ADMIN vs SYSTEM at the Resource Server)

The `role` claim is not a built-in part of the JWT — it is added by a custom `OAuth2TokenCustomizer<JwtEncodingContext>` bean (`AuthorizationServerConfig#jwtTokenCustomizer`). It only fires for the `authorization_code` human login flow, where the token's principal is backed by `UserDetails` (from `UserDetailsServiceImpl`, which sets the Spring Security authority `ROLE_<UserRole>` from the `users.role` column — currently only `ADMIN` exists as a `UserRole` value). `client_credentials` tokens (SYSTEM) have no such `UserDetails` principal, so they never receive a `role` claim.

On the Resource Server side, `ResourceServerConfig#jwtAuthenticationConverter` reads the `role` claim (if present) and maps it to a `ROLE_<value>` `GrantedAuthority`; absent, it resolves to no authorities. This is what makes `@PreAuthorize("hasRole('ADMIN')")` reject SYSTEM tokens: they authenticate successfully (satisfying the blanket `.anyRequest().authenticated()`) but carry no `ROLE_ADMIN` authority.

**Before this addition (pre map-pins feature), there was no way to distinguish an ADMIN request from a SYSTEM request at the Resource Server** — both were just "authenticated." Any future endpoint that needs to be ADMIN-only (or SYSTEM-only) should reuse this same `role` claim / `hasRole(...)` mechanism rather than inventing a new one.

---

## Token flow

- Authorization Server exposes `/oauth2/token`, `/oauth2/authorize`, OIDC discovery
- All `/api/**` endpoints are protected and require a valid Bearer JWT — **except** `/api/public/**`, which is `permitAll`
- JWT is validated by the Resource Server filter chain
- RSA key pair (2048-bit) is generated at startup — **ephemeral for dev**. Must be externalized for production.
- OAuth2 clients and authorizations are persisted via `JdbcRegisteredClientRepository` / `JdbcOAuth2AuthorizationService`

---

## Security filter chains

Spring Security evaluates filter chains in `@Order` sequence — the first chain whose `securityMatcher` matches the request wins.

| Order | Chain | Matcher | Purpose |
|-------|-------|---------|---------|
| 1 | `authorizationServerSecurityFilterChain` | OAuth2/OIDC endpoints | Issues and manages tokens; handles OIDC discovery |
| 2 | `resourceServerSecurityFilterChain` | `/api/**` | Enforces JWT on internal endpoints; `permitAll` on `/api/public/**` |
| 3 | `defaultSecurityFilterChain` | everything else (catch-all) | Serves the `/login` form; supports the `authorization_code` human login flow |

**Critical ordering constraint:** the Resource Server chain (`@Order(2)`) must come before the Form Login chain (`@Order(3)`). The Form Login chain has no `securityMatcher` and is a catch-all — if it ran first it would intercept `/api/**` requests and redirect them to `/login` instead of letting the Resource Server handle them.

---

## CORS

Allowed origins (configured in `ResourceServerConfig`'s `corsConfigurationSource` bean):
- `http://localhost:5173` (aniannoth-overview dev server)
- `http://localhost:4173` (aniannoth-overview preview)

The `CorsConfigurationSource` bean registers patterns for both `/api/**` and `/oauth2/**`, but **registering the pattern is not sufficient by itself** — CORS is enforced per Spring Security filter chain (see "Security filter chains" above), so `.cors(Customizer.withDefaults())` must also be called on every chain that serves an endpoint the browser calls cross-origin via `fetch()`/XHR. `resourceServerSecurityFilterChain` (`/api/**`) has always had this. `authorizationServerSecurityFilterChain` (`/oauth2/**`, including `POST /oauth2/token`) needed it added separately (map-pins PKCE login flow, aniannoth-overview) — `/oauth2/authorize` never needed it (full-page browser navigation, not subject to CORS), which is why the gap went unnoticed until the token exchange itself was exercised.

If a future endpoint needs to be called cross-origin, check both: (1) the path is covered by a `corsConfigurationSource` pattern, and (2) `.cors(Customizer.withDefaults())` is called on whichever chain actually serves that path.

---

## Bootstrap and token acquisition

### Context

keynor-core acts as both Authorization Server and Resource Server. No users or OAuth2 clients exist after a fresh Flyway migration — they must be inserted manually before any authenticated API call can succeed.

The `PasswordEncoder` bean is a plain **`BCryptPasswordEncoder`** — not a `DelegatingPasswordEncoder`. This has one critical consequence:

> **Stored password hashes must NOT include the `{bcrypt}` prefix.**
> Store only the raw BCrypt hash: `$2a$10$...`
> The `{bcrypt}` prefix is specific to `DelegatingPasswordEncoder` and will cause `"Encoded password does not look like BCrypt"` and `invalid_client` errors.

### Step 1 — Generate a BCrypt hash

Use `jshell` with the Spring Security Crypto jar from the local Maven repository:

```bash
jshell --class-path "C:/Users/<user>/.m2/repository/org/springframework/security/spring-security-crypto/6.3.3/spring-security-crypto-6.3.3.jar;C:/Users/<user>/.m2/repository/commons-logging/commons-logging/1.2/commons-logging-1.2.jar"
```

Inside jshell:

```java
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
System.out.println(new BCryptPasswordEncoder().encode("your-password-here"));
```

The output is the raw hash to store — for example:
```
$2a$10$ncuvtKlGkqK/UGdm8ebus.aWGXCjF8D9STYDp7P8RpoVeTmkCK6GC
```

Do NOT add any prefix. Store this value exactly as printed.

### Step 2 — Insert the ADMIN user

```sql
INSERT INTO users (id, email, password, role, active)
VALUES (
    gen_random_uuid(),
    'admin@yourdomain.com',
    '$2a$10$<hash-generated-above>',
    'ADMIN',
    true
);
```

### Step 3 — Insert the SYSTEM client

```sql
INSERT INTO oauth2_registered_client (
    id, client_id, client_id_issued_at, client_secret, client_name,
    client_authentication_methods, authorization_grant_types,
    redirect_uris, post_logout_redirect_uris, scopes, client_settings, token_settings
)
VALUES (
    gen_random_uuid()::text,
    'system-client',
    NOW(),
    '$2a$10$<hash-generated-above>',
    'System Client',
    'client_secret_basic',
    'client_credentials',
    '',
    '',
    'openid',
    '{"@class":"java.util.Collections$UnmodifiableMap","settings.client.require-proof-key":false,"settings.client.require-authorization-consent":false}',
    '{"@class":"java.util.Collections$UnmodifiableMap","settings.token.reuse-refresh-tokens":true,"settings.token.id-token-signature-algorithm":["org.springframework.security.oauth2.jose.jws.SignatureAlgorithm","RS256"],"settings.token.access-token-time-to-live":["java.time.Duration",3600.000000000],"settings.token.access-token-format":{"@class":"org.springframework.security.oauth2.server.authorization.settings.OAuth2TokenFormat","value":"self-contained"},"settings.token.refresh-token-time-to-live":["java.time.Duration",86400.000000000],"settings.token.authorization-code-time-to-live":["java.time.Duration",300.000000000],"settings.token.device-code-time-to-live":["java.time.Duration",300.000000000]}'
);
```

Keep the plaintext secret in a local `.env.local` file (gitignored) — it cannot be recovered from the database after hashing.

### Step 4 — Obtain a token (client_credentials)

Used by agents and services (Aroneus, keynor-rpg, etc.) for service-to-service calls.

**Postman:**
- Method: `POST`
- URL: `http://localhost:8080/oauth2/token`
- Tab Authorization → Type: `Basic Auth` → Username: `system-client` → Password: `<plaintext secret>`
- Tab Body → `x-www-form-urlencoded` → `grant_type: client_credentials`

**curl:**
```bash
curl -X POST http://localhost:8080/oauth2/token \
  -u "system-client:<plaintext-secret>" \
  -d "grant_type=client_credentials"
```

The response includes an `access_token` (Bearer JWT). Use it in subsequent API calls:
```
Authorization: Bearer <access_token>
```

### Step 5 — Obtain a token (authorization_code — ADMIN human flow)

Used for the admin panel. Open a browser and navigate to:
```
http://localhost:8080/oauth2/authorize?response_type=code&client_id=<admin-client-id>&redirect_uri=<redirect>&code_challenge=<pkce-challenge>&code_challenge_method=S256
```

The Authorization Server redirects to `/login`. Submit ADMIN credentials. After login, the code is returned to the redirect URI and exchanged for a token via `POST /oauth2/token`.

### Common errors

| Error | Cause | Fix |
|-------|-------|-----|
| `"Encoded password does not look like BCrypt"` | Hash stored with `{bcrypt}` prefix | Remove the prefix — store only `$2a$10$...` |
| `invalid_client` | Wrong `client_id`, wrong secret, or hash mismatch | Verify with SELECT; regenerate hash if needed |
| `invalid_grant` | Grant type not registered for this client | Check `authorization_grant_types` column |
| Token invalid after app restart | RSA key is ephemeral in dev | Obtain a new token — existing tokens are invalidated on restart |

---

*Maintained by Imaws. Formerly `oauth2-bootstrap.md` — renamed and expanded to cover the full security model, not just the bootstrap procedure.*
