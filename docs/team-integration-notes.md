# Team integration notes

How the four ClubSphere modules meet in one WAR. Read this before editing shared files.

**PBL Team ID:** PBL2627-CSE-A-024  
**Repository:** [Avantikamishra862/Arya_Clubsphere](https://github.com/Avantikamishra862/Arya_Clubsphere)

## Owned areas

| Member | Roll | Module | Touch these first |
|---|---|---|---|
| Avantika Mishra | 24EARCS032 | Authentication & User Management | `LoginServlet`, `LogoutServlet`, `AuthenticationFilter`, `UserService`, `UserDAO`, `PasswordUtil`, `login.jsp`, `admin/users.jsp` |
| Avanshi Kothari | 24EARCS031 | Club Management | `ClubServlet`, `ClubService`, `ClubDAO`, `ClubMemberDAO`, `clubs/*.jsp`, admin club forms, club-head members |
| Akansha Sharma | 24EARCS012 | Events & QR | `EventServlet`, `RegistrationServlet`, `QRCodeServlet`, `EventService`, `RegistrationService`, `QRCodeUtil`, `events/*.jsp`, club-head event + QR views |
| Aditi Maheshwari | 24EARCS007 | Dashboards, Tasks, Notifications | role dashboards, `TaskService`, `NotificationService`, `ReportService`, `notifications.jsp`, sidebar / navbar |

## Shared files — talk before editing

- `pom.xml`
- `src/main/resources/application.properties`
- `src/main/webapp/WEB-INF/web.xml`
- `src/main/java/com/clubsphere/util/DBUtil.java`
- `src/main/java/com/clubsphere/filter/AuthenticationFilter.java`
- `database/schema.sql` and `database/seed.sql`
- `src/main/webapp/components/navbar.jsp`, `sidebar.jsp`, `footer.jsp`
- `src/main/webapp/css/style.css`
- `Dockerfile`, `docker-compose.yml`

`AdminServlet` and `ClubHeadServlet` host URLs for more than one module. Coordinate before changing path routing.

## Suggested merge order

1. Database schema + seed (needed by everyone).
2. Auth filter and login (otherwise role URLs are unreachable).
3. Clubs (events attach to a club).
4. Events, registrations, QR.
5. Dashboards, tasks, notifications, reports.

## Branches

`main` is the deployable line. Prefer feature branches named after the module, for example `feature/event-qr`. Open a pull request into `main`.

Do not commit `.env` files or real database passwords.

## Local integration checklist

1. `mysql < database/schema.sql` then `mysql < database/seed.sql`
2. Copy `.env.example` and set `DB_PASSWORD` (never commit the filled file)
3. `mvn test`
4. `mvn clean package`
5. Deploy the WAR or run `docker compose up --build`
6. Log in with the four demo roles in [`demo-accounts.md`](demo-accounts.md)
7. Confirm QR origin: `APP_BASE_URL` must match the context path (`/aryaClgClubSphere` for the named WAR, `/` for Docker `ROOT.war`)

## Academic PDFs (do not regenerate into this folder)

Finished deliverables live as PDFs only:

- [`SRS/ClubSphere_SRS_IEEE_Format.pdf`](SRS/ClubSphere_SRS_IEEE_Format.pdf)
- [`UML/Arya_College_ClubSphere_UML_Diagrams.pdf`](UML/Arya_College_ClubSphere_UML_Diagrams.pdf)
- [`reports/`](reports/)
- [`research-paper/ClubSphere_Research_Paper.pdf`](research-paper/ClubSphere_Research_Paper.pdf)
