# PROJECT.md

Student-level event check-in demo. Minimal on purpose; edge cases are not handled.

## Flow

Register (user) -> Login -> Event -> Register for event -> Generate Pass -> QR Code -> Scan -> Verify -> Mark Attendance

## Components (4)

1. event-service
2. pass-service
3. user-service (users, auth, attendance)
4. client page

Plus one H2 file database per backend service (not a separate component). Nothing else.

## Stack

* Spring Boot MVC, Spring Data JPA (Hibernate), H2 file DB per service
* ORM only: data access through JPA entities + Spring Data repositories; Hibernate creates the tables (`spring.jpa.hibernate.ddl-auto=update`). No SQL scripts, no raw queries, no migrations.
* Build tool: Maven, via the committed wrapper (`mvnw`) - nothing installed globally
* Java 21 (confirmed): project-local JDK in `.tools/` (git-ignored), installed as the first task after the owner says go. System Java 8 is not touched; shell uses `JAVA_HOME=.tools/jdk-21`. Spring Boot 4.1.1 (what start.spring.io gave). Ports are 9080-9083 because 8080-8083 were taken by another project.
* No API gateway; client calls each service directly (CORS enabled)

## Services

| Service | Port | Owns | Endpoints (proposed) |
|-|-|-|-|
| event-service | 9081 | Event, Registration | `POST /events`, `GET /events`, `POST /events/{id}/register` (login required; also calls pass-service to create the pass); `GET /registrations/mine` |
| pass-service | 9082 | Pass | `POST /passes` (registrationId) -> pass with UUID token; `GET /passes/{token}/qr` -> PNG; `GET /passes/{token}/verify` |
| user-service | 9083 | User, Attendance | `POST /users/signup`, `POST /users/login` -> session token; `POST /attendance/scan` (QR token, needs login); `GET /attendance` |

* Auth (student-level): username + plain-text password stored in user-service; login returns a random token kept in the DB; sent as `Authorization` header.
* Roles (confirmed): User has an `isOrganizer` boolean (default false). One admin, hardcoded: user-service seeds a user with id 1 (`admin` / `admin`) on startup; "is admin" = `id == 1`. Admin counts as an organizer.
  * Admin: `PUT /users/{id}/organizer` flips a user to organizer; `POST /events` creates events.
  * Organizer/admin only: `POST /attendance/scan`, `GET /attendance`.
  * Everyone, no login: `GET /events` (viewing events).
  * Logged-in users: register for events, see their own pass/QR.
* Token checks: user-service validates tokens itself and exposes `GET /users/me` (token -> user). event-service calls it for `POST /events` (must be admin) and `POST /events/{id}/register` (must be logged in; userId comes from the token). pass-service is open.
* The user-service `scan` calls pass-service verify, then marks attendance. A second scan of the same pass is rejected with "already checked in".
* QR content = the pass token (UUID). QR image made with ZXing in pass-service.
* Inter-service calls: plain `RestClient`/`RestTemplate`, URLs in `application.properties`.
* pass-service trusts the registrationId (does not call event-service) - shortcut.
* Layout: 4 sibling Maven projects (`event-service`, `pass-service`, `user-service`, `client`), each with its own wrapper, run separately.

## Client

* `client` (port 9080): Spring Boot app that only serves static files (one main class, no controllers).
* Single `index.html`, Tailwind via CDN, plain JS `fetch` to the services. Polished card layout.
* Sections: event list (visible without login); sign up / login; register for event -> pass with QR image; admin panel (create event, make organizer); scan (organizer/admin only, hidden otherwise).
* Scan: webcam via `html5-qrcode` (CDN) with a paste-token fallback box.

## Demo data (seeded on first start of an empty DB)

* Users (password = username): `admin` (admin, id 1), `organizer` (id 2), `demo` (id 3), `alice` (id 4). The login page has a "Demo mode" box that logs in with one click.
* event-service also seeds two registrations + passes for alice (user id 4, events 1 and 2). This needs pass-service up first and assumes alice is user id 4 (seed order). Shortcut: ids are hardcoded.
* Events: 4 sample events (hackathon, expo, cultural night, career fair), seeded by event-service.
* To reset to this state: `.\start-demo.ps1` (stops services, deletes `*/data/*.db`, starts in order, opens the browser). `-Stop` stops everything. Presenter script: `demo.md`.
* Demo script: login as Attendee -> register -> My passes (ticket + QR) -> log out -> Organizer -> Scan -> paste token or show the QR to the camera -> green result; scan again -> red "Already checked in". Admin tab creates events and promotes users.
* New: `GET /registrations/{id}` and `GET /events/stats` (event-service); user-service calls the first on scan and stores `username`, `eventName`, `eventId` on Attendance (fields added only). Scan result shows the name; the Scan tab shows checked-in / registered per event.
* `GET /registrations/mine` now also returns `eventId`, `location`, `dateTime` (fields added, none removed).

## Explicitly NOT doing (owner decision, 1-hour budget)

The project is a demo; the **frontend experience** is the priority. These are deliberately excluded and must not be added or "fixed":

* **No security**: plain-text passwords, open endpoints, no input sanitising.
* **No hardening**: no validation, no edge-case handling, no rate limits.
* **No reliability work**: no retries, timeouts, transactions across services, or failure recovery.
* **No speed optimisation**: no caching, pagination, or tuning.
* **No tests.** This overrides the "test-first" step in `CLAUDE.md` for this project.

## Out of scope

Gateway, service discovery, Docker, password hashing, roles, validation, tests beyond the bare minimum, pagination, emailing the pass.

## Run

Per service folder (`user-service`, `pass-service`, `event-service`, `client`), with `JAVA_HOME` set to `.tools/jdk-21`:
`./mvnw spring-boot:run`, then open http://localhost:9080. Start user-service and pass-service before event-service. Login `admin` / `admin` is the only admin. H2 files are created under each service's `data/` folder.
