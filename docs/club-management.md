# Club Management

**Application:** Arya College ClubSphere  
**Module:** Club Management  
**Developer:** Avanshi Kothari (24EARCS031)

---

## Overview

Club Management is the official directory of Arya College societies. Admins create and maintain club records. Club heads manage membership for **the one club** assigned to them. Students browse public club pages. Joining as a managed member is a club-head / admin action, not a public self-join.

The seed data includes **26 clubs** (coding, robotics, AI, cultural, NSS, sports, and others).

---

## Architecture

```
Public browse          Admin CRUD              Club-head roster
ClubServlet /clubs     AdminServlet /admin/club*    ClubHeadServlet /club-head/members
        │                      │                           │
        └──────────────────────┴───────────────────────────┘
                               │
                          ClubService
                               │
                    ClubDAO / ClubMemberDAO
                               │
                    clubs, club_members
```

---

## Files

| File | Purpose |
|---|---|
| `controller/ClubServlet.java` | Public explore and club profile |
| `controller/AdminServlet.java` | Club create / edit / toggle status |
| `controller/ClubHeadServlet.java` | Members, profile update, managed-club check |
| `service/ClubService.java` | Club and membership rules |
| `dao/ClubDAO.java` | Club SQL |
| `dao/ClubMemberDAO.java` | Membership SQL |
| `model/Club.java`, `model/ClubMember.java` | Domain objects |
| `clubs/explore.jsp`, `clubs/view.jsp` | Public pages |
| `WEB-INF/views/admin/clubs.jsp`, `club-form.jsp` | Admin |
| `WEB-INF/views/club-head/members.jsp` | Roster |

---

## URL contract

### Public discovery

| Method | Path | Action |
|---|---|---|
| GET | `/clubs` | Search by `search` (keyword) and `category` |
| GET | `/clubs?action=view&id={id}` | Profile, advisor, members, related events |

Unauthenticated visitors can browse.

### Admin

| Method | Path | Action |
|---|---|---|
| GET | `/admin/clubs` | List all clubs |
| GET | `/admin/club/create` | New club form |
| GET | `/admin/club/edit?id=` | Edit form |
| POST | `/admin/club/save` | Create or update |
| POST | `/admin/club/toggle-status` | ACTIVE / INACTIVE |

Club fields: `club_code`, name, category, description, `head_user_id`, faculty advisor, contact, meeting venue, status.

### Club-head membership

A club head is the user referenced by `clubs.head_user_id`. If none is assigned, club-head screens stop on `error.jsp`.

| Method | Path | Action |
|---|---|---|
| GET | `/club-head/members` | Roster for the managed club |
| POST | `/club-head/member/add` | Add a user (`userId`, `memberRole`, `notes`) |
| POST | `/club-head/member/remove` | Remove a member |
| POST | `/club-head/profile/update` | Update club profile fields |

Adding a `STUDENT` as a member also upgrades that user's role to `CLUB_MEMBER`.

Member roles in `club_members`: `MEMBER`, `COORDINATOR`, `VICE_HEAD`.

---

## Database

```
clubs
  id, club_code (unique), name, category, description, logo_url,
  head_user_id → users (ON DELETE SET NULL),
  faculty_advisor, contact_email, contact_phone, meeting_venue,
  status ENUM(ACTIVE, INACTIVE)

club_members
  id, club_id → clubs (CASCADE), user_id → users (CASCADE),
  member_role ENUM(MEMBER, COORDINATOR, VICE_HEAD),
  joined_date, status ENUM(ACTIVE, INACTIVE, REMOVED), notes
  UNIQUE (club_id, user_id)
```

Deleting a club cascades to members, events, tasks, and announcements (`ON DELETE CASCADE` in `schema.sql`). Setting a club INACTIVE keeps history but takes it out of operational use.

---

## Integration points

- **Auth (Avantika):** club-head login loads `managedClubId` from `head_user_id`. Filter allows ADMIN into `/club-head/*`.
- **Events (Akansha):** every event stores `club_id`. Public club view lists that club's events.
- **Dashboards (Aditi):** club-head dashboard and reports are scoped to the managed club.

Talk before editing `database/schema.sql`, `AuthenticationFilter`, or `AdminServlet` (shared with users, events, and reports).

---

## Limitations

- Students cannot self-join a club from the public page.
- Many seed clubs have no assigned head (`head_user_id` is NULL).
- There is no separate faculty-advisor login role.
