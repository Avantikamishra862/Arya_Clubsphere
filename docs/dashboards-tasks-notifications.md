# Dashboards, Tasks & Notifications

**Application:** Arya College ClubSphere  
**Module:** Dashboards, Tasks & Notifications  
**Developer:** Aditi Maheshwari (24EARCS007)

---

## Overview

After login, ClubSphere sends each user to a role-specific dashboard. Club heads assign tasks and publish announcements. Members update task status. Every authenticated user has an in-app notification inbox.

The dashboard role is taken from the signed-in `User`, never from a URL the caller invents. Opening `/admin/dashboard` as a student is rejected by `AuthenticationFilter` with 403.

---

## Architecture

```
LoginServlet.redirectToDashboard
        │
        ├── ADMIN        → /admin/dashboard
        ├── CLUB_HEAD    → /club-head/dashboard
        ├── CLUB_MEMBER  → /club-member/dashboard
        └── STUDENT      → /student/dashboard

Shared chrome: components/navbar.jsp, sidebar.jsp, footer.jsp
```

---

## Files

| File | Purpose |
|---|---|
| `controller/AdminServlet.java` | Admin dashboard + `/admin/reports` |
| `controller/ClubHeadServlet.java` | Head dashboard, tasks, announcements, reports |
| `controller/ClubMemberServlet.java` | Member dashboard, own tasks, events, announcements |
| `controller/StudentServlet.java` | Student dashboard, registrations, profile |
| `controller/NotificationServlet.java` | Inbox and mark-read |
| `service/TaskService.java` | Assign / update; writes `TASK_ASSIGNED` |
| `service/NotificationService.java` | Inbox + announcements; broadcasts `CLUB_ANNOUNCEMENT` |
| `service/ReportService.java` | Counts and `activity_logs` |
| `dao/TaskDAO.java`, `AnnouncementDAO.java`, `NotificationDAO.java`, `ReportDAO.java` | SQL |
| `model/Task.java`, `Announcement.java`, `Notification.java`, `ActivityLog.java` | Domain |
| `WEB-INF/views/admin/dashboard.jsp`, `reports.jsp` | Admin |
| `WEB-INF/views/club-head/dashboard.jsp`, `tasks.jsp`, `announcements.jsp`, `reports.jsp` | Club head |
| `WEB-INF/views/club-member/dashboard.jsp`, `tasks.jsp`, `events.jsp`, `announcements.jsp` | Club member |
| `WEB-INF/views/student/dashboard.jsp`, `registrations.jsp`, `profile.jsp` | Student |
| `notifications.jsp` | Inbox |

---

## Role dashboards

| Role | Entry URL | Typical widgets |
|---|---|---|
| ADMIN | `/admin/dashboard` | Overall stats, clubs, upcoming events, recent registrations, activity |
| CLUB_HEAD | `/club-head/dashboard` | Club stats, members, events, tasks, announcements |
| CLUB_MEMBER | `/club-member/dashboard` | Assigned tasks, memberships, upcoming events |
| STUDENT | `/student/dashboard` | Own registrations, clubs, upcoming events |

Admin also has `/admin/reports`. Club heads have `/club-head/reports`. Both use `ReportService` / `ReportDAO`. Login and logout write to `activity_logs`.

---

## Tasks

Table `tasks`: club, optional event, assignee, assigner, due date, priority (`LOW` / `MEDIUM` / `HIGH` / `URGENT`), status (`PENDING` / `IN_PROGRESS` / `COMPLETED`).

### Club head

| Method | Path | Action |
|---|---|---|
| GET | `/club-head/tasks` | List club tasks |
| POST | `/club-head/task/create` | Assign a task (notifies assignee) |
| POST | `/club-head/task/update-status` | Update status |

### Club member

| Method | Path | Action |
|---|---|---|
| GET | `/club-member/tasks` | Tasks assigned to the current user |
| POST | `/club-member/task/update-status` | Own task only (`assigned_to_user_id` must match session user) |
| GET | `/club-member/events` | Upcoming events |
| GET | `/club-member/announcements` | Public / club announcements |

---

## Announcements

Club heads publish to `announcements` (`NORMAL` / `IMPORTANT` / `URGENT`, target `ALL` or `MEMBERS_ONLY`).

| Method | Path | Action |
|---|---|---|
| GET | `/club-head/announcements` | List for the managed club |
| POST | `/club-head/announcement/create` | Publish; broadcasts `CLUB_ANNOUNCEMENT` to active users |

---

## Notifications

`NotificationServlet` at `/notifications/*` (any authenticated user).

| Method | Path | Action |
|---|---|---|
| GET | `/notifications` | Inbox |
| GET | `/notifications/mark-all-read` | Mark all read |
| POST | `/notifications` with `id` | Mark one read; JSON `{success, unread}` |

Types in schema: `EVENT_CREATED`, `REGISTRATION_CONFIRMED`, `EVENT_REMINDER`, `EVENT_CANCELLED`, `DEADLINE_ALERT`, `TASK_ASSIGNED`, `CLUB_ANNOUNCEMENT`, `GENERAL`.

Implemented writers today:

- `REGISTRATION_CONFIRMED` — `RegistrationService` after a successful signup
- `TASK_ASSIGNED` — `TaskService` when a club head creates a task
- `CLUB_ANNOUNCEMENT` — `NotificationService.publishAnnouncement`

Unread count is stored on the session as `unreadCount` and set at login.

---

## Student extras

| Method | Path | Action |
|---|---|---|
| GET | `/student/registrations` | Personal event registrations |
| POST | `/student/registration/cancel` | Cancel a registration |
| GET | `/student/profile` | Profile page |
| POST | `/student/profile/update` | Name, email, phone, department, year |

---

## Database

```
tasks
  club_id, event_id (nullable), title, description,
  assigned_to_user_id, assigned_by_user_id, due_date, priority, status

announcements
  club_id, created_by_user_id, title, content, priority, target_role

notifications
  user_id, title, message, type, related_event_id, related_task_id,
  related_club_id, is_read

activity_logs
  user_id, action, details, ip_address, created_at
```

---

## Integration points

- **Auth:** dashboard URL is chosen from `currentUser.role`. Filter enforces prefixes.
- **Clubs:** club-head dashboard aborts if `head_user_id` is missing.
- **Events:** student dashboard lists registrations from `RegistrationService`.

Shared chrome (`navbar.jsp`, `sidebar.jsp`, `style.css`) is used by every role. Coordinate before changing it.

---

## Limitations

- Notifications are in-app only (no e-mail or SMS).
- `EVENT_REMINDER` and `DEADLINE_ALERT` are schema values, not scheduled jobs.
- Club-member announcements currently load public announcements, not a per-club membership filter beyond what the DAO already returns.
- Reports are summary counts, not exportable PDFs from the app.
