# Smart E-Commerce System – Spring Security

REST (and GraphQL) backend with **JWT authentication**, **Google OAuth2**, **RBAC**, **CORS/CSRF** configuration, and **security event logging**.

## Tech Stack

- **Java 21**, **Spring Boot 4.x**
- **Spring Security** (JWT, OAuth2 Client)
- **BCrypt** password hashing, **HMAC-SHA256** JWT signatures
- **PostgreSQL**, JPA

## Lab 8 – Performance (Advanced Optimization)

Profiling (JFR), async (CompletableFuture, `@EnableAsync`), concurrency (ConcurrentHashMap, CopyOnWriteArrayList), caching, and metrics are implemented for the Smart E-Commerce lab. See **[docs/PERFORMANCE.md](docs/PERFORMANCE.md)** for:

- How to run with Java Flight Recorder and capture baseline metrics
- Actuator and custom metrics endpoints
- DSA/caching notes and report template
- Postman load-testing steps

New endpoints: `GET /api/dashboard/stats`, `GET /api/dashboard/activity`, `GET /api/dashboard/metrics` (ADMIN).

## Quick Start

1. **Environment**
   - Set `DB_URL`, `DB_USER`, `DB_PASSWORD` for PostgreSQL.
   - (Optional) Set `APP_JWT_SECRET` (min 32 chars) and `GOOGLE_CLIENT_ID` / `GOOGLE_CLIENT_SECRET` for OAuth2.

2. **Run**
   ```bash
   ./mvnw spring-boot:run
   ```

3. **Open**
   - Swagger UI: `http://localhost:8080/swagger-ui.html`
   - Register: `POST /api/users/register` (body: username, email, password; role optional, defaults to CUSTOMER).
   - Login: `POST /auth/login` (username, password) → returns JWT.
   - Use the JWT in **Authorization: Bearer \<token\>** for protected endpoints.

## Authentication & Authorization

| Mechanism        | Use case                          |
|------------------|-----------------------------------|
| **JWT**          | Username/password login, API access |
| **OAuth2 (Google)** | Social login; user created/persisted, then JWT issued via redirect |

- **Public:** `POST /auth/login`, `POST /api/users/register`, `GET /api/products`, `GET /api/category`, OAuth2 callback, Swagger, `/v3/api-docs`.
- **Authenticated:** All other `/api/*` and `/graphql` require a valid JWT (or session after OAuth2 redirect).
- **RBAC:** Roles `ADMIN`, `STAFF`, `CUSTOMER`. Endpoints are restricted with `@PreAuthorize` (e.g. user management and order status → ADMIN/STAFF; customers see only their orders).

## CORS and CSRF

### CORS (Cross-Origin Resource Sharing)

- **Purpose:** Controls which **origins** (e.g. `https://myapp.com`) can call your API from the browser.
- **Configured:** Global CORS allows `http://localhost:*`, common headers, and methods (GET, POST, PUT, PATCH, DELETE, OPTIONS). Other origins are rejected.
- **Testing:** From a web app on another port (e.g. `http://localhost:3000`), the browser sends an `Origin` header; the server allows or denies based on CORS. In **Postman** there is no browser origin, so CORS does not apply to Postman-only flows.

### CSRF (Cross-Site Request Forgery)

- **Purpose:** Stops attackers from submitting forms (or state-changing requests) on behalf of a logged-in user when the browser automatically sends cookies.
- **This API:** CSRF is **disabled** because we use **stateless JWT** in the `Authorization` header. There are no session cookies; each request is authenticated by the token. So CSRF protection is not required for these APIs.
- **When to enable CSRF:** Use it for **stateful** apps that rely on **cookie-based sessions** (e.g. server-rendered forms, browser posting with session cookie). Then the server issues a CSRF token per session and the client must send it back (e.g. in header or form field) for state-changing requests.

### Summary

| Concern | CORS | CSRF |
|--------|------|------|
| **What** | Who can call the API from a browser (origin) | Who can submit state-changing requests using the user’s session |
| **Relevant when** | Any browser client from another origin | Cookie-based sessions and form submissions |
| **This project** | Allowed origins configured; Postman unaffected | Disabled for stateless JWT APIs |

## Testing with Postman

1. **Register**
   - `POST {{base}}/api/users/register`
   - Body (JSON): `{"username":"testuser","email":"test@example.com","password":"Test@1234","role":"CUSTOMER"}`

2. **Login**
   - `POST {{base}}/auth/login`
   - Body: `{"username":"testuser","password":"Test@1234"}`
   - Copy `data.token` from the response.

3. **Protected endpoints**
   - Add header: **Authorization** = `Bearer <paste token>`.
   - Examples: `GET /api/orders`, `GET /api/users` (ADMIN), `POST /api/orders`, etc.

4. **Roles**
   - Create users with different roles (e.g. via DB or register with role CUSTOMER; ADMIN/STAFF created by admin or DB). Call admin-only endpoints (e.g. `GET /api/users`) with an ADMIN JWT.

5. **Invalid/expired token**
   - Send an invalid or expired JWT → expect **401 Unauthorized**.

## Google OAuth2

1. Create OAuth2 credentials in Google Cloud Console (Web application), set redirect URI:  
   `http://localhost:8080/login/oauth2/code/google`.
2. Set env: `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET`.
3. In browser: `http://localhost:8080/oauth2/authorization/google`.
4. After login, you are redirected to `app.oauth2.redirect-uri` with `?token=<JWT>` (default: `http://localhost:8080/oauth2-redirect`). Use this token in **Authorization: Bearer** for API calls.

## Security Features

- **Passwords:** Stored with **BCrypt** (Spring Security’s `BCryptPasswordEncoder`).
- **JWT:** Signed with **HMAC-SHA256**; contains subject (username), roles, issued-at, expiration. Tampered or expired tokens → **401**.
- **Token blacklist:** Logout adds the JWT to an in-memory blacklist (revoked tokens rejected).
- **Logging:** Authentication success/failure events are logged (e.g. for auditing and brute-force detection). Check logs for `SECURITY_AUTH_SUCCESS` and `SECURITY_AUTH_FAILURE`.

## API Documentation

- **OpenAPI (Swagger):** `http://localhost:8080/swagger-ui.html`
- Secured endpoints are documented with the **bearerAuth** scheme; use **Authorize** in Swagger UI with the JWT from `/auth/login`.

## Configuration

- **JWT:** `app.jwt.secret` (min 32 chars in production), `app.jwt.expiration-ms`.
- **OAuth2 redirect:** `app.oauth2.redirect-uri`.
- **CORS:** See `SecurityConfig#corsConfigSource()` to adjust allowed origins and methods.
