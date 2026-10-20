# Event Check-in

A small student-style microservice demo: users register for an event, get a pass with a QR code, and an organizer scans the QR at the door to mark attendance.

Flow: sign up -> log in -> view events -> register -> pass + QR -> scan -> verify -> attendance marked.

Design details live in [PROJECT.md](PROJECT.md). This is a demo: passwords are plain text and there is almost no input validation.

## Components

| Folder | Port | Role |
|-|-|-|
| `client` | 9080 | One static page (Tailwind + html5-qrcode via CDN), served by a tiny Spring Boot app |
| `event-service` | 9081 | Events and registrations |
| `pass-service` | 9082 | Passes, QR image (ZXing), pass verification |
| `user-service` | 9083 | Users, login, organizer flag, attendance |

Each backend service has its own H2 file database (created under its `data/` folder). Stack: Java 21, Spring Boot 4.1.1, Spring MVC, JPA/Hibernate, Maven wrapper.

Roles: anyone can view events. Logged-in users register and see their passes. The admin (`admin` / `admin`, user id 1) creates events and makes users organizers. Organizers and the admin can scan.

## Quick start

Needs the project-local JDK in `.tools/jdk-21` and an internet connection (Maven dependencies, CDN scripts).

Open four terminals (Git Bash), one per service, in this order:

```bash
export JAVA_HOME="$PWD/.tools/jdk-21"
cd user-service && ./mvnw spring-boot:run
```

Repeat for `pass-service`, `event-service`, then `client` (set `JAVA_HOME` again in each terminal, run from the project root).

Then open http://localhost:9080 and:

1. Log in as `admin` / `admin`, create an event in the Admin panel.
2. Log out, sign up a normal user, log in, click **Register** on the event. The pass with its QR appears under "My passes".
3. Log in as admin (or promote the user in the Admin panel) and use **Scan pass**: start the camera and point it at the QR, or paste the pass token and click **Verify & mark**.

## Command reference

| Task | Command (from the service folder) |
|-|-|
| Run a service | `./mvnw spring-boot:run` |
| Build a jar | `./mvnw -DskipTests package` |
| Run the jar | `"$JAVA_HOME/bin/java" -jar target/<service>-0.0.1-SNAPSHOT.jar` |
| Run tests | `./mvnw test` |
| Check Java in use | `"$JAVA_HOME/bin/java" -version` |
| Who is on a port (PowerShell) | `Get-NetTCPConnection -State Listen -LocalPort 9083` |
| Stop it (PowerShell) | `Stop-Process -Id <pid>` |
| Reset a service's data | stop it, delete its `data/` folder |

API quick reference:

| Service | Endpoint |
|-|-|
| event-service | `GET /events` (public), `POST /events` (admin), `POST /events/{id}/register`, `GET /registrations/mine` |
| pass-service | `POST /passes`, `GET /passes/{token}/qr`, `GET /passes/{token}/verify` |
| user-service | `POST /users/signup`, `POST /users/login`, `GET /users/me`, `GET /users` (admin), `PUT /users/{id}/organizer` (admin), `POST /attendance/scan`, `GET /attendance` |

Authenticated calls send the login token in an `Authorization` header.

## Troubleshooting

* **"Port 908x was already in use"**: another process holds the port. Find it with the PowerShell command above and stop it, or change `server.port` in that service's `application.properties` (and the URLs at the top of the script in `client/src/main/resources/static/index.html`, plus `user.service.url` / `pass.service.url` in the other services).
* **Build fails with "release version 21 not supported" or class version errors**: `JAVA_HOME` is not set, so system Java 8 is being used. Export `JAVA_HOME` to `.tools/jdk-21` in that terminal.
* **Page loads but events or login do nothing / red message at top**: a backend service is not running. Start all four; user-service and pass-service before event-service.
* **Registering says "login required" or 401**: the saved token is stale (for example after wiping `data/`). Log out and log in again.
* **Registering fails with a 500 error**: pass-service is down, since event-service calls it to create the pass.
* **Scan says "invalid pass"**: the token is wrong, or the DB for pass-service was reset after the pass was made.
* **Scan says "already checked in"**: expected, each pass works once.
* **Camera does not start**: allow camera access in the browser, use `http://localhost` (not an IP), and close other apps using the camera. The paste-token box always works.
* **Page is unstyled or the camera button errors**: Tailwind and html5-qrcode load from CDNs, so you need internet access.
* **Not an admin after wiping the database**: the admin is the first user created on an empty DB. If someone signed up before the seed ran, delete `user-service/data` and restart.
* **H2 "database may be already in use"**: two copies of the same service are running. Stop the extra one.
