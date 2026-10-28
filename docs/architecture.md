# ClubSphere Architecture

**Application:** Arya College ClubSphere  
**Stack:** Java 17 · Jakarta Servlet 6 · JSP · Tomcat 10.1 · JDBC + HikariCP · MySQL 8  
**PBL Team ID:** PBL2627-CSE-A-024

## Overview

ClubSphere is a **server-rendered** Java web application. A browser loads JSP pages and posts HTML forms. Jakarta Servlets handle HTTP, services hold business rules, and DAOs talk to MySQL through JDBC and HikariCP.

This is not a React SPA and not a JWT REST API. Authorization lives in `AuthenticationFilter` and in servlets, not in hidden menu links.

```
Browser (HTML / CSS / JS / JSP)
        │  HTTP form posts and page loads
        ▼
Apache Tomcat 10.1
        │
  AuthenticationFilter   – public whitelist, session check, RBAC
        │
  Servlet (controller)   – URL mapping, request parameters, view forward
        │
  Service                – business rules (login, capacity, membership)
        │
  DAO                    – PreparedStatement JDBC
        │
  HikariCP
        │
        ▼
      MySQL 8  (clubsphere_db)
```

Role dashboards live under `src/main/webapp/WEB-INF/views/`. Those JSP files are not URL-addressable; servlets forward to them.

## Packages (`com.clubsphere`)

| Package | Responsibility |
|---|---|
| `controller` | Servlets mapped with `@WebServlet` |
| `filter` | `AuthenticationFilter` for session + RBAC |
| `service` | Business logic used by servlets |
| `dao` | JDBC access with parameterized SQL |
| `model` | Domain objects mapped to tables |
| `util` | `DBUtil`, `PasswordUtil`, `QRCodeUtil` |

Conventions:

- Servlets do not open JDBC connections. They call services; services call DAOs.
- SQL uses `PreparedStatement`.
- Secrets and environment-specific values come from environment variables first, then `application.properties`.
- The acting user is always taken from the HTTP session attribute `currentUser`. A hidden form field is never treated as authorization.

## Session

| Attribute | Set when | Purpose |
|---|---|---|
| `currentUser` | Successful `POST /login` | Authenticated `User` |
| `unreadCount` | Login, and after marking notifications read | Badge on the navbar |

Session timeout is 60 minutes. The cookie is HttpOnly. `secure` is currently `false` in `web.xml` (set `true` only when the site is served over HTTPS).

## Runtime configuration

`DBUtil` loads `application.properties` from the classpath, then overrides:

| Setting | Environment variable | Property key |
|---|---|---|
| JDBC URL | `DB_URL` or `DATABASE_URL` | `db.url` |
| Database user | `DB_USERNAME`, `DB_USER`, or `MYSQLUSER` | `db.username` |
| Database password | `DB_PASSWORD` or `MYSQLPASSWORD` | `db.password` |
| Public origin for QR links | `APP_BASE_URL` | `app.base.url` |
| Upload directory | `APP_UPLOAD_DIR` | `app.upload.dir` |
| HTTP port (Docker / Render) | `PORT` | Tomcat connector (see Dockerfile) |

See [`.env.example`](../.env.example). Never commit a filled `.env`.

## Security

- Passwords are stored with BCrypt (`PasswordUtil`, cost factor 10).
- Login invalidates the previous HTTP session before creating a new one (session-fixation mitigation).
- `AuthenticationFilter` protects `/admin/*`, `/club-head/*`, `/club-member/*`, and other authenticated routes.
- Public whitelist: `/`, `/index.jsp`, `/login`, `/login.jsp`, `/logout`, `/css/`, `/js/`, `/images/`, `/uploads/`, `/clubs`, `/events`, `/qr/`. The filter also lists `/register`, but there is **no public self-registration servlet** in this release.
- The `redirect` query parameter on login is restricted to internal paths.
- Club-head screens resolve the managed club from `clubs.head_user_id`. A head cannot operate on another club by guessing IDs.

## Database

Schema and demo rows live in:

- [`database/schema.sql`](../database/schema.sql) — creates `clubsphere_db` and ten tables
- [`database/seed.sql`](../database/seed.sql) — 13 users, 26 clubs, sample events / tasks / registrations

Tables: `users`, `clubs`, `club_members`, `events`, `registrations`, `event_participants`, `tasks`, `announcements`, `notifications`, `activity_logs`.

Engine is InnoDB, character set `utf8mb4`, with foreign keys. Unique constraints prevent duplicate club memberships (`club_id`, `user_id`) and duplicate event registrations (`event_id`, `user_id`).

## Deployment shapes

| Mode | Context path | How to run |
|---|---|---|
| Local Tomcat + WAR | `/aryaClgClubSphere` | `mvn clean package`, deploy `target/aryaClgClubSphere.war` |
| Docker Compose | `/` (`ROOT.war`) | `docker compose up --build` |
| Render (live demo) | `/` | Dockerfile + `DB_*` and `APP_BASE_URL` env vars |

QR codes use `APP_BASE_URL`. Set it to the public origin that matches the deployed context path, or scanned links will 404.

## Request map

| URL | Servlet | Notes |
|---|---|---|
| `/login` | `LoginServlet` | GET form, POST authenticate |
| `/logout` | `LogoutServlet` | Invalidate session |
| `/clubs` | `ClubServlet` | Public explore / view |
| `/events` | `EventServlet` | Public list / view |
| `/event/register` | `RegistrationServlet` | Login required; `eventId` or QR `token` |
| `/qr/{token}` | `QRCodeServlet` | PNG of the registration URL |
| `/admin/*` | `AdminServlet` | ADMIN only |
| `/club-head/*` | `ClubHeadServlet` | CLUB_HEAD (ADMIN also allowed by the filter) |
| `/club-member/*` | `ClubMemberServlet` | CLUB_MEMBER and above |
| `/student/*` | `StudentServlet` | Any authenticated user |
| `/notifications/*` | `NotificationServlet` | Any authenticated user |

## Tests

`mvn test` runs five JUnit 5 methods in `PasswordUtilTest` and `QRCodeUtilTest`. They do not need MySQL. There are no servlet, DAO, or UI tests in this release.

## Known limitations

- No public self-registration; admins create accounts.
- Session cookie is not marked Secure in default `web.xml`.
- No rate limiting, e-mail verification, or password-reset flow.
- Notifications are in-app only.
- File uploads are not a first-class module (`APP_UPLOAD_DIR` exists for later use).
