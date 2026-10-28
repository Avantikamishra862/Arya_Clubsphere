# Demo Accounts

Use these seeded accounts after running `database/schema.sql` and `database/seed.sql`.
They are created by the seed script, not by public self-registration.

The hosted demo can take a minute to open on the first visit while the server wakes up.

**Live demo:** [event-management-uylc.onrender.com](https://event-management-uylc.onrender.com/)

**Demo video:** [ClubSphere walkthrough on Google Drive](https://drive.google.com/file/d/1o5umJYwDw9k0V-BGlIcvP0j_xMHfoaXU/view)

Password for every seed user: `password123`

These accounts are for evaluation only. Do not reuse this password in production.

## Primary logins

| Role | Username | Email | What it is for |
|---|---|---|---|
| Admin | `admin` | `admin@aryacollege.in` | Manage users, clubs, events, registrations, and reports |
| Club Head | `head_coding` | `head.coding@aryacollege.in` | Manage Arya Coding Ninjas Club: members, events, QR, tasks, announcements |
| Club Member | `member_coding_1` | `priya.sharma@aryacollege.in` | Club tasks, announcements, and club events |
| Student | `student_1` | `dev.kumar@aryacollege.in` | Explore clubs, register for events, view registrations and profile |

The login page has one-click autofill buttons for these four roles. Username or email both work.

## Other seeded club heads

| Username | Club |
|---|---|
| `head_robotics` | Arya Robotics & Mechatronics Society |
| `head_ai` | Artificial Intelligence & Data Science Club |
| `head_cyber` | Arya Cyber Security & Ethical Hacking Guild |
| `head_cultural` | Spandan Cultural Society |
| `head_music` | Symphony Music & Band Club |

Other students: `student_2`, `student_3`. Other members: `member_coding_2`, `member_robotics_1`.

## Quick path after login

1. **Admin** → `/admin/dashboard` → Users, Clubs, Events, Reports.
2. **Club Head** (`head_coding`) → `/club-head/dashboard` → create an event, open `/club-head/event/qr`.
3. **Student** → `/events` → register, or scan the event QR which opens `/event/register?token=...`.
4. **Club Member** → `/club-member/tasks` → update an assigned task.

Sample seeded event for QR testing: HackArya 2026, token `QR_HACKARYA_2026_TOKEN_01`.
