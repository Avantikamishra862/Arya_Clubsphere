# Event Management & QR Registration

**Application:** Arya College ClubSphere  
**Module:** Event Management & QR Code Registration  
**Developer:** Akansha Sharma (24EARCS012)

---

## Overview

Club heads publish campus events. Students register individually or as a group. Each event has a unique `qr_token`. ClubSphere renders that token as a PNG QR code whose payload is the public registration URL.

QR codes are a convenience for signup. They are **not** a cryptographic attendance proof.

---

## Architecture

```
Club head creates event  →  EventService stores unique qr_token
        │
        ▼
GET /club-head/event/qr  →  GET /qr/{token}
        │
        │  APP_BASE_URL + /event/register?token=…
        ▼
ZXing PNG (320×320, error-correction H)
        │
        ▼
Student scans → login if needed → RegistrationServlet → confirmation
```

---

## Files

| File | Purpose |
|---|---|
| `controller/EventServlet.java` | Public list / view |
| `controller/RegistrationServlet.java` | Register by `eventId` or `token` |
| `controller/QRCodeServlet.java` | PNG at `/qr/{token}` |
| `controller/ClubHeadServlet.java` | Event CRUD, cancel, QR page, registrations |
| `controller/AdminServlet.java` | `/admin/events`, `/admin/registrations` |
| `service/EventService.java` | Event lifecycle and token lookup |
| `service/RegistrationService.java` | Capacity, deadline, duplicates, confirmation notification |
| `dao/EventDAO.java`, `dao/RegistrationDAO.java` | SQL |
| `util/QRCodeUtil.java` | ZXing PNG and Base64 data URI |
| `model/Event.java`, `Registration.java`, `EventParticipant.java` | Domain |
| `events/list.jsp`, `view.jsp`, `register.jsp`, `confirmation.jsp` | Public / student |
| `WEB-INF/views/club-head/events.jsp`, `event-form.jsp`, `event-qr.jsp`, `registrations.jsp` | Club head |
| `src/test/java/com/clubsphere/util/QRCodeUtilTest.java` | PNG magic bytes and data URI |

---

## URL contract

### Public catalogue

| Method | Path | Action |
|---|---|---|
| GET | `/events` | Filter by `search`, `type`, `clubId` |
| GET | `/events?action=view&id={id}` | Details, capacity, deadline, registration status |

### Club-head lifecycle

| Method | Path | Action |
|---|---|---|
| GET | `/club-head/events` | Events for the managed club |
| GET | `/club-head/event/create` | New event form |
| GET | `/club-head/event/edit?id=` | Edit form |
| POST | `/club-head/event/save` | Create or update (assigns `qr_token` on create) |
| POST | `/club-head/event/cancel` | Mark CANCELLED |
| GET | `/club-head/event/qr?id=` | Show QR image for the event |
| GET | `/club-head/registrations` | Registrations for the club's events |

Event fields: date, start/end time, venue, registration deadline, capacity, `event_type`, `participation_type` (`INDIVIDUAL` / `GROUP` / `BOTH`), status.

Event types: `WORKSHOP`, `SEMINAR`, `COMPETITION`, `CULTURAL`, `TECHNICAL`, `SPORTS`, `HACKATHON`, `EXHIBITION`, `OTHER`.

Statuses: `UPCOMING`, `REGISTRATION_OPEN`, `REGISTRATION_CLOSED`, `COMPLETED`, `CANCELLED`.

### Student registration

`GET|POST /event/register`

Lookup order:

1. `token` query parameter (from a scanned QR)
2. `eventId` query parameter (from the events UI)

`RegistrationServlet` / `RegistrationService` rules:

- Caller must be logged in; otherwise redirect to login with a return URL.
- Reject cancelled, completed, or closed events.
- Reject after `registration_deadline`.
- Reject when `registered_count >= capacity`.
- Duplicate `(event_id, user_id)` is rejected (unique key + service check).
- Group registrations store extra rows in `event_participants`.
- Confirmed registrations increment `events.registered_count` and send `REGISTRATION_CONFIRMED`.

Students can cancel from `POST /student/registration/cancel` (dashboards module).

### QR image

| Method | Path | Action |
|---|---|---|
| GET | `/qr/{token}` | PNG of `APP_BASE_URL/event/register?token=…` |
| GET | `/qr/{token}?download=true` | Same PNG as an attachment |

Unknown tokens return 404. Missing token returns 400.

### Admin oversight

Admins list all events (`/admin/events`) and all registrations (`/admin/registrations`). Event creation stays with the owning club head.

---

## Database

```
events
  id, title, club_id → clubs (CASCADE), description,
  event_date, start_time, end_time, venue, registration_deadline,
  capacity, registered_count, event_type, participation_type, status,
  qr_token VARCHAR(64) UNIQUE, banner_url, created_by_user_id → users

registrations
  id, registration_number UNIQUE, event_id, user_id,
  registration_type ENUM(INDIVIDUAL, GROUP), group_name, group_size,
  status ENUM(PENDING, CONFIRMED, CANCELLED),
  UNIQUE (event_id, user_id)

event_participants
  id, registration_id → registrations (CASCADE),
  participant_name, student_id, email, phone, is_leader
```

---

## Environment

| Variable | Used for |
|---|---|
| `APP_BASE_URL` | Origin baked into every QR payload |

Examples:

- Local WAR: `http://localhost:8080/aryaClgClubSphere`
- Docker `ROOT.war`: `http://localhost:8080`
- Live demo: `https://event-management-uylc.onrender.com`

If this value does not match the deployed context path, scanned QR links 404.

---

## Tests

```bash
mvn test
```

`QRCodeUtilTest` checks PNG magic bytes and Base64 data URIs. It does not hit MySQL or `QRCodeServlet`.

---

## Integration points

- **Auth:** unauthenticated QR scans bounce to login with `redirect` set to the registration URL.
- **Clubs:** events belong to one club; club-head scope is `head_user_id`.
- **Notifications:** successful signup writes `REGISTRATION_CONFIRMED`.

---

## Limitations

- QR encodes a registration URL, not a separate attendance credential.
- `EVENT_REMINDER` / `EVENT_CANCELLED` types exist in the schema; reminder e-mail is not implemented.
- No waitlist when capacity is full.
