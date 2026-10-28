# Authentication & User Management

**Application:** Arya College ClubSphere  
**Module:** Authentication & User Management  
**Developer:** Avantika Mishra (24EARCS032)  
**Branch:** `feature/auth-user-management`

---

## Overview

This module is the identity layer for ClubSphere. It handles sign-in, sign-out, BCrypt passwords, HTTP sessions, role-based URL guards, and the administrative user directory.

There is no JWT, no Spring Security, and no public Register page. Accounts are created by an administrator. The four roles are `ADMIN`, `CLUB_HEAD`, `CLUB_MEMBER`, and `STUDENT`.

### Responsibilities

- Authenticate with username **or** email at `POST /login`.
- Invalidate the old session before creating a new one (session-fixation mitigation).
- Store `currentUser` and `unreadCount` on the session.
- Guard role URL prefixes in `AuthenticationFilter`.
- Let admins create users, change roles, and toggle status at `/admin/users`.
- Upgrade a legacy/non-standard password hash to BCrypt after a successful login.

---

## Architecture

```
Browser
   │
   ▼
AuthenticationFilter   (/*)
   │  public whitelist OR session + RBAC
   ▼
LoginServlet / LogoutServlet / AdminServlet (/user/*)
   │
   ▼
UserService
   │
   ▼
UserDAO  (PreparedStatement + HikariCP)
   │
   ▼
MySQL  users
```

---

## Files

| File | Purpose |
|---|---|
| `controller/LoginServlet.java` | GET form, POST authenticate, dashboard redirect |
| `controller/LogoutServlet.java` | Audit log + session invalidate |
| `controller/AdminServlet.java` | `/user/create`, `/user/change-role`, `/user/toggle-status` |
| `filter/AuthenticationFilter.java` | Public whitelist and role prefixes |
| `service/UserService.java` | Login, register, role/status/password |
| `dao/UserDAO.java` | Parameterized SQL |
| `util/PasswordUtil.java` | BCrypt hash / verify (cost 10) |
| `model/User.java` | Identity, role, status, managed-club fields |
| `login.jsp` | Sign-in form, password toggle, demo autofill |
| `WEB-INF/views/admin/users.jsp` | Admin user directory |
| `src/test/java/com/clubsphere/util/PasswordUtilTest.java` | Hash / verify / null tests |

---

## URL contract

| Method | Path | Who | Action |
|---|---|---|---|
| GET | `/login` | Public | Show form, or redirect if already signed in |
| POST | `/login` | Public | Authenticate; optional `redirect` |
| GET | `/logout` | Public | Invalidate session |
| GET | `/admin/users` | ADMIN | User directory |
| POST | `/admin/user/create` | ADMIN | Create account |
| POST | `/admin/user/change-role` | ADMIN | Change role |
| POST | `/admin/user/toggle-status` | ADMIN | ACTIVE / INACTIVE (cannot deactivate self) |

Login form fields: `username` (username or email), `password`, optional `redirect`.

After login, `LoginServlet.redirectToDashboard` sends:

| Role | Destination |
|---|---|
| ADMIN | `/admin/dashboard` |
| CLUB_HEAD | `/club-head/dashboard` |
| CLUB_MEMBER | `/club-member/dashboard` |
| STUDENT (default) | `/student/dashboard` |

If the user is a club head, login also loads `managedClubId` / `managedClubName` from `clubs.head_user_id`.

---

## RBAC matrix

| Area | Unauthenticated | STUDENT | CLUB_MEMBER | CLUB_HEAD | ADMIN |
|---|:---:|:---:|:---:|:---:|:---:|
| `/`, `/login`, `/logout` | yes | yes | yes | yes | yes |
| `/clubs`, `/events`, `/qr/*` | yes | yes | yes | yes | yes |
| `/student/*`, `/notifications/*` | login redirect | yes | yes | yes | yes |
| `/club-member/*` | login redirect | 403 | yes | yes | yes |
| `/club-head/*` | login redirect | 403 | 403 | yes | yes |
| `/admin/*` | login redirect | 403 | 403 | 403 | yes |

Hiding a sidebar link is only a convenience. The filter is the security boundary.

---

## Security behaviour

1. **Session fixation:** `LoginServlet` invalidates any existing session, then `req.getSession(true)`.
2. **Open redirect:** `redirect` must start with the context path or a single `/`, and must not contain `://` or `javascript:`.
3. **Hash upgrade:** after a successful verify, a non-standard hash is rewritten with `PasswordUtil.hashPassword`.
4. **Self-lockout:** an admin cannot deactivate the currently logged-in account.
5. **Uniqueness:** `registerUser` rejects duplicate username or email before insert.
6. **SQL:** all user queries use `PreparedStatement`.

---

## Database

```
users
  id, username (unique), email (unique), password_hash,
  full_name, role ENUM(ADMIN, CLUB_HEAD, CLUB_MEMBER, STUDENT),
  student_id, phone, department, year_of_study, avatar_url,
  status ENUM(ACTIVE, INACTIVE, SUSPENDED), created_at, updated_at
```

Login writes a row to `activity_logs` (`LOGIN`). Logout writes `LOGOUT`.

---

## Environment

Uses the shared DB settings in [`.env.example`](../.env.example). No extra auth secrets (no JWT signing key).

---

## Tests

```bash
mvn test
```

`PasswordUtilTest` covers hash/verify, salt diversity, and null inputs. There are no servlet or filter tests.

---

## Limitations

- No public self-registration (`/register` is listed on the filter whitelist but has no servlet).
- No password-reset or e-mail verification.
- Session cookie `secure` is `false` in `web.xml`.
- Demo seed password is documented in [`demo-accounts.md`](demo-accounts.md) and must not be used in production.
