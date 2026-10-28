# Arya College ClubSphere

The hosted demo can take a minute to open on the first visit while the server wakes up.

**Live demo:** [event-management-uylc.onrender.com](https://event-management-uylc.onrender.com/)

**Demo logins** (admin, club head, club member, student): see [`docs/demo-accounts.md`](docs/demo-accounts.md).

**Demo video:** [ClubSphere walkthrough on Google Drive](https://drive.google.com/file/d/1o5umJYwDw9k0V-BGlIcvP0j_xMHfoaXU/view) · notes in [`docs/demo-video.md`](docs/demo-video.md).

**One platform for clubs, events, and campus activities.** Arya College ClubSphere is a web-based club and event management system for Arya College of Engineering & IT, Jaipur. It centralizes clubs, memberships, events, QR registration, tasks, and notifications for administrators, club heads, club members, and students.

Built with **Java 17, JSP, Servlets, JDBC, MySQL 8, Maven, and Apache Tomcat 10.1**.

---

## Table of Contents

- [Features](#features)
- [User Roles](#user-roles)
- [Team Module Allocation](#team-module-allocation)
- [Tech Stack](#tech-stack)
- [How It Works](#how-it-works)
- [Project Structure](#project-structure)
- [Getting Started (Local Setup)](#getting-started-local-setup)
- [Configuration](#configuration)
- [Database Setup](#database-setup)
- [Running the Project](#running-the-project)
- [Running Tests](#running-tests)
- [Deployment with Docker](#deployment-with-docker)
- [Documentation](#documentation)
- [GitHub Workflow](#github-workflow)
- [Security](#security)
- [Team](#team)
- [Contributing](#contributing)

---

## Features

| Module | Description |
|---|---|
| Authentication & User Management | Login, logout, BCrypt passwords, session handling, and admin user directory. |
| Club Management | Directory of 26 college clubs, admin club CRUD, and club-head membership. |
| Event Management | Club events with schedule, venue, capacity, and individual or group registration. |
| QR Code Registration | Per-event QR PNG that opens the registration URL for check-in and signup. |
| Role-Based Dashboards | Separate homes for Admin, Club Head, Club Member, and Student. |
| Tasks & Notifications | Club tasks, announcements, in-app notifications, and simple reports. |

---

## User Roles

| Role | Responsibilities |
|---|---|
| Admin | Users, clubs, system-wide events/registrations, reports. |
| Club Head | Members, events, QR codes, tasks, and announcements for one assigned club. |
| Club Member | Assigned tasks, club events, and announcements. |
| Student | Explore clubs and events, register, manage personal registrations and profile. |

Access is enforced in `AuthenticationFilter`, not only in the UI.

---

## Team Module Allocation

| Module | Module Name | Assigned Member |
|---|---|---|
| Module 1 | Authentication & User Management | Avantika Mishra |
| Module 2 | Club Management | Avanshi Kothari |
| Module 3 | Event Management & QR Code Registration | Akansha Sharma |
| Module 4 | Dashboards, Tasks & Notifications | Aditi Maheshwari |

---

## Tech Stack

| Layer | Technologies |
|---|---|
| Frontend | HTML, CSS, JavaScript, JSP, Bootstrap 5 |
| Backend | Java 17, Jakarta Servlets, HikariCP |
| Database | MySQL 8 |
| Database connectivity | JDBC (`PreparedStatement`) |
| Build | Apache Maven |
| Server | Apache Tomcat 10.1 |
| QR | ZXing |
| Passwords | BCrypt (`jbcrypt`) |
| Deployment | Dockerfile + Docker Compose |

---

## How It Works

```text
        Browser (JSP pages)
                 |
                 v
        AuthenticationFilter
                 |
                 v
           Java Servlets
                 |
                 v
             Services
                 |
                 v
           DAO + JDBC
                 |
                 v
              MySQL
```

1. Open the site and browse clubs/events, or sign in.
2. The filter checks the session and the role for `/admin`, `/club-head`, `/club-member`, and `/student`.
3. Servlets call services; DAOs run parameterized SQL.
4. Club heads publish events; `/qr/{token}` returns a PNG whose payload is the registration URL.

More detail: [`docs/architecture.md`](docs/architecture.md).

---

## Project Structure

```text
Arya_Clubsphere/
├── pom.xml
├── Dockerfile
├── docker-compose.yml
├── .env.example
├── database/
│   ├── schema.sql          creates clubsphere_db and tables
│   └── seed.sql            demo users, 26 clubs, sample events
├── docs/                   SRS, UML, reports, module notes
├── src/main/java/com/clubsphere/
│   ├── controller/         Servlets
│   ├── filter/             AuthenticationFilter
│   ├── service/
│   ├── dao/
│   ├── model/
│   └── util/               DBUtil, PasswordUtil, QRCodeUtil
├── src/main/resources/application.properties
├── src/main/webapp/        JSP, css, js
└── src/test/java/          JUnit tests (no database required)
```

---

## Getting Started (Local Setup)

### Prerequisites

- JDK 17
- Apache Maven 3.9+
- Apache Tomcat 10.1 (if you deploy the WAR yourself)
- MySQL Server 8.0+
- Git
- Docker Desktop (optional, for Compose)

```bash
java -version
mvn -version
git --version
```

### 1. Clone the repository

```bash
git clone https://github.com/Avantikamishra862/Arya_Clubsphere.git
cd Arya_Clubsphere
```

### 2. Create the database

```bash
mysql -u root -p < database/schema.sql
mysql -u root -p < database/seed.sql
```

This creates `clubsphere_db`, tables, demo users, and 26 clubs.

### 3. Configure credentials

Copy the env template and export variables (or set the same keys in Tomcat):

```bash
cp .env.example .env
```

Minimum values:

- `DB_USERNAME` / `DB_PASSWORD` — your local MySQL login
- `APP_BASE_URL` — `http://localhost:8080/aryaClgClubSphere` for a named WAR, or `http://localhost:8080` for Docker `ROOT.war`

`DBUtil` reads environment variables **before** `src/main/resources/application.properties`. Keep real passwords out of Git. The properties file uses `CHANGE_ME` as a placeholder.

### 4. Build

```bash
mvn clean package
```

Artifact: `target/aryaClgClubSphere.war`.

---

## Configuration

| Variable | Required | Purpose |
|---|---|---|
| `DB_URL` | No | JDBC URL (default local `clubsphere_db`) |
| `DB_USERNAME` | Yes for a locked-down MySQL | Database user |
| `DB_PASSWORD` | Yes | Database password |
| `APP_BASE_URL` | Yes for working QR links | Public origin + context path |
| `PORT` | Docker / Render | Tomcat HTTP port (default 8080) |
| `APP_UPLOAD_DIR` | No | Upload directory (default `uploads`) |

Full comments: [`.env.example`](.env.example). Never commit `.env`.

---

## Database Setup

1. Start MySQL.
2. Run `schema.sql`, then `seed.sql`.
3. Set `DB_*` to match that instance.
4. Confirm the app can read the `users` table (startup logs from `DBUtil`).

Seed password for demo accounts: `password123` (see [`docs/demo-accounts.md`](docs/demo-accounts.md)).

---

## Running the Project

### WAR on Tomcat

```bash
mvn clean package
```

Copy `target/aryaClgClubSphere.war` into Tomcat `webapps/` and start Tomcat.

Open:

```text
http://localhost:8080/aryaClgClubSphere
```

### Fast path: Docker Compose

See [Deployment with Docker](#deployment-with-docker). Compose serves the app at **http://localhost:8080/** (root context).

---

## Running Tests

Unit tests do not need MySQL:

```bash
mvn test
```

Single class:

```bash
mvn test -Dtest=PasswordUtilTest
mvn test -Dtest=QRCodeUtilTest
```

---

## Deployment with Docker

`Dockerfile` builds the WAR with Maven and copies it into Tomcat 10.1 as `ROOT.war`. The entrypoint binds the HTTP connector to `$PORT` (Render-compatible).

### Full demo (app + MySQL)

```bash
docker compose up --build
```

Then open **http://localhost:8080** and use [`docs/demo-accounts.md`](docs/demo-accounts.md). Compose maps MySQL to host port **3307**.

### Image only (external database)

```bash
docker build -t clubsphere .

docker run -p 8080:8080 \
  -e DB_URL="jdbc:mysql://HOST:3306/clubsphere_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC" \
  -e DB_USERNAME=USER \
  -e DB_PASSWORD=PASSWORD \
  -e APP_BASE_URL="http://localhost:8080" \
  clubsphere
```

On Render, set the same `DB_*` variables plus `APP_BASE_URL` to the public `https://…onrender.com` origin.

---

## Documentation

| Document | Location |
|---|---|
| Documentation index | [`docs/README.md`](docs/README.md) |
| Demo logins | [`docs/demo-accounts.md`](docs/demo-accounts.md) |
| Demo video | [`docs/demo-video.md`](docs/demo-video.md) |
| Architecture | [`docs/architecture.md`](docs/architecture.md) |
| Authentication module | [`docs/authentication-and-user-management.md`](docs/authentication-and-user-management.md) |
| Club Management | [`docs/club-management.md`](docs/club-management.md) |
| Events & QR | [`docs/event-management-and-qr.md`](docs/event-management-and-qr.md) |
| Dashboards, tasks, notifications | [`docs/dashboards-tasks-notifications.md`](docs/dashboards-tasks-notifications.md) |
| Team integration notes | [`docs/team-integration-notes.md`](docs/team-integration-notes.md) |
| IEEE SRS | [`docs/SRS/`](docs/SRS/) |
| UML diagrams | [`docs/UML/`](docs/UML/) |
| Team and individual reports | [`docs/reports/`](docs/reports/) |
| Research paper | [`docs/research-paper/`](docs/research-paper/) |

---

## GitHub Workflow

Repository: [Avantikamishra862/Arya_Clubsphere](https://github.com/Avantikamishra862/Arya_Clubsphere)

Keep `main` deployable. Use feature branches (`feature/…`, `docs/…`, `fix/…`) and pull requests.

```bash
git clone https://github.com/Avantikamishra862/Arya_Clubsphere.git
git checkout main
git pull origin main
git switch -c feature/your-change
```

Do not commit `.env`, passwords, or `application-local.properties`. When you add a setting, add it to `.env.example` too. Coordinate edits to `pom.xml`, `schema.sql`, `AuthenticationFilter`, and shared JSP chrome.

---

## Security

- BCrypt password hashing
- Session fixation mitigation on login
- RBAC in `AuthenticationFilter`
- Parameterized SQL in DAOs
- Open-redirect checks on the login `redirect` parameter
- Env-based secrets; placeholder only in `application.properties`

---

## Team

| S. No. | Team Member | Enrollment Number | Responsibility |
|---|---|---|---|
| 1 | Avantika Mishra | 24EARCS032 | Authentication & User Management |
| 2 | Avanshi Kothari | 24EARCS031 | Club Management |
| 3 | Akansha Sharma | 24EARCS012 | Event Management & QR Code Registration |
| 4 | Aditi Maheshwari | 24EARCS007 | Dashboards, Tasks & Notifications |

**PBL Team ID:** `PBL2627-CSE-A-024`

Individual reports: [`docs/reports/`](docs/reports/).

---

## Contributing

1. Do not commit directly to `main`.
2. Pull latest `main`, then branch from it.
3. Keep commits small and named after the module (`feat(events): …`, `docs(srs): …`).
4. Run `mvn test` and smoke-test the affected role.
5. Never commit secrets. Update `.env.example` when adding variables.
6. Talk to the team before changing shared files listed in [`docs/team-integration-notes.md`](docs/team-integration-notes.md).

---

**Developed by the Arya College ClubSphere Team**  
**Arya College of Engineering & IT, Jaipur**
