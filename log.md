Shared context for every agent working in this directory. Read this file **before** starting work and update it **as** you work.

## Status Legend

|Marker|Meaning|
|-|-|
|`[ ]` TODO|Planned, not started|
|`[~]` WORKING|In progress (set *before* starting the task)|
|`[x]` DONE|Completed and verified|
|`[!]` FAILED|Attempted but failed or interrupted; add a note on why|

## Rules for Agents

1. **Read first.** Check "Current Status" and the latest log entries to learn context and what is in flight.
2. **Plan as TODOs.** Add your tasks under "Task List" as `[ ]` before touching any files.
3. **Mark `[~]` before starting** a task, and `[x]` immediately after finishing it. Never batch updates at the end.
4. **Log every change** (file created/edited/deleted/moved) in the "Change Log" with timestamp, agent/model name, and files touched.
5. **On failure, don't delete.** Mark the task `[!]` and record the error, last successful step, and any partial changes so the next agent can diagnose and resume.
6. **Recovering from an interrupted run:** any task still `[~]` when you start means the previous agent stopped midway. Inspect the files listed in its log entry (`git status` / `git diff`), then either finish it or mark it `[!]` with notes.
7. Keep entries append-only, newest first in the Change Log. Don't rewrite history.

## Current Status

* **Last updated:** 2026-10-20 — log initialised; no work started.
* **Project:** <!-- TODO: name and one-line goal -->
* **Stack:** <!-- TODO: Java version, build tool, framework -->
* **Commands:** <!-- TODO: build/test/run commands -->
* **Blockers / Notes:** none

## Task List

* [x] T01: Initialise `CLAUDE.md` and `log.md` for a new Java project
* [ ] T02: Define the project (name, goal, stack) in `CLAUDE.md` and "Current Status" above
* [ ] T03: Create the repo skeleton (build file + wrapper, `.gitignore`, `.gitattributes`, README)

* [x] T04: Agree design with owner and record it in `PROJECT.md`
* [x] T05: Install JDK 21 into `.tools/` (project-local, git-ignored); add `.gitignore`
* [x] T06: user-service (users, login, admin seed, organizer flag, attendance scan)
* [x] T07: event-service
* [x] T08: pass-service (token + QR)
* [x] T09: client page
* [x] T10: end-to-end run, README

* [x] T11: README
* [x] T12: Analyse project, record exclusions (no security/hardening/reliability/speed/tests) in `PROJECT.md`
* [x] T13: Frontend polish + demo seed data

* [x] T14: Remove vibe-coded visual tells from the UI

*Add new tasks at the bottom with the next free ID. Never reuse or renumber IDs.*

## Change Log

<!-- Newest first. Copy the template below for each entry. -->

<!--
### YYYY-MM-DD HH:MM — Agent (Model) — Short title

* **Task:** Txx — what was attempted
* **Status:** DONE | WORKING | FAILED
* **Files:** created / edited / deleted / moved
* **Details:** what changed and how it was verified
* **Notes / Errors:** anything the next agent needs to know
-->

### 2026-10-21 — Claude (Sonnet 5.5) — T14: remove "vibe-coded" look

* **Task:** T14
* **Status:** DONE
* **Files:** edited `client/.../static/index.html`, `log.md`
* **Details:** based on web research on AI-UI tells: removed purple/indigo gradients, emoji icons, pill badges, big rounded cards and shadows. Now: one accent (burnt orange), stone neutrals, Public Sans + IBM Plex Mono, flat header with underline tabs, events as a dated list, thin borders, small radii. Same JS logic. Re-verified with Playwright demo flow (no page errors); services stopped, test DBs deleted.

### 2026-10-21 — Claude (Sonnet 5.5) — T13: frontend redesign + demo seed

* **Task:** T13
* **Status:** DONE
* **Files:** rewrote `client/.../static/index.html`; edited `user-service/.../UserController.java` (seed organizer/demo/alice), `event-service/.../EventController.java` (seed 4 events; `/registrations/mine` adds eventId/location/dateTime), `PROJECT.md`, `log.md`
* **Details:** tabs nav, hero login with demo buttons, event cards with registered state, ticket passes with zoom/download, full-screen scan result, toasts, skeletons. No dark mode, no tests (per owner). Verified with Playwright (scratchpad `demo.js`): demo login -> register -> pass -> organizer scan OK -> rescan rejected; no page errors. Camera scanning not tested (no webcam in the run). All 4 services stopped, test DB files deleted so the seed runs fresh.
* **Notes:** old DB files would block seeding (seed only runs on an empty DB); delete `*/data/*.db` before the review.

### 2026-10-21 — Claude (Sonnet 5.5) — T12: analysis + exclusions

* **Task:** T12
* **Status:** DONE
* **Files:** edited `PROJECT.md` (added "Explicitly NOT doing" section), `log.md`
* **Details:** analysed the project and proposed frontend-focused improvements to the owner; no code changed.

### 2026-10-20 — Claude (Sonnet 5.5) — T11b: README Windows commands

* **Task:** T11
* **Status:** DONE
* **Files:** edited `README.md`
* **Details:** quick start and command table now have PowerShell and Git Bash variants. Verified `.\mvnw.cmd spring-boot:run` under PowerShell with `$env:JAVA_HOME` set: user-service started on 9083; then stopped it and deleted `user-service/data`. cmd.exe not tested.

### 2026-10-20 — Claude (Sonnet 5.5) — T11: README

* **Task:** T11 — README (what it is, quick start, command reference, troubleshooting)
* **Status:** DONE
* **Files:** created `README.md`
* **Details:** commands were taken from what was run this session; the "run from project root / JAVA_HOME" quick-start steps were not re-run after writing.

### 2026-10-20 — Claude (Sonnet 5.5) — T06-T09: code written; approved global install

* **Task:** T06, T07, T08, T09 (code written, not yet built/run); verification tooling
* **Status:** DONE
* **Files:** created `user-service/`, `event-service/`, `pass-service/`, `client/` (Spring Initializr skeletons, Boot 4.1.1, Java 21, groupId com.example) with entities, repos, controllers, `application.properties`, `client/.../static/index.html`
* **Details:** **Approved global install (owner asked explicitly):** `npm install -g playwright` (v1.64.1, user scope `%APPDATA%\npm`). Chromium 1248 already cached in `%LOCALAPPDATA%\ms-playwright`, matches. Ports moved to 9080-9083 (8080-8083 held by the unrelated 'WP Sem 5' project, left running). Boot 4 property is `spring.web.error.include-message`. Fixed client bug: inline `createEvent()` collided with `document.createEvent` (renamed `addEvent`). Verified with Playwright (scratchpad `e2e.js`): public events, signup/login, register, QR image loads, plain user scan 403, admin scan marks attendance, rescan 409 'already checked in'. All 4 services stopped, test H2 data deleted. Design tweak: event-service register now also calls pass-service to create the pass; `GET /registrations/mine` returns pass tokens.

### 2026-10-20 — Claude (Sonnet 5.5) — T05: JDK 21 + .gitignore

* **Task:** T05
* **Status:** DONE
* **Files:** created `.gitignore`, `.tools/jdk-21/` (Temurin 21.0.12.1, from api.adoptium.net, git-ignored)
* **Details:** project-local only; no PATH/registry change. Verified with `.tools\jdk-21\bin\java.exe -version`. Use it by setting `JAVA_HOME` to `.tools/jdk-21` per shell. System Java 8 untouched.
* **Notes / Errors:** first rename of the extracted folder was denied; succeeded on retry.

### 2026-10-20 — Claude (Sonnet 5.5) — T04: design finalised

* **Task:** T04
* **Status:** DONE (design agreed; waiting for owner to commit and say go)
* **Files:** edited `PROJECT.md`
* **Details:** JDK 21 into `.tools/` (not installed yet, first task after go); events viewable without login; admin creates events and promotes organizers; event-service checks tokens via user-service `GET /users/me`.

### 2026-10-20 — Claude (Sonnet 5.5) — T04: roles

* **Task:** T04
* **Status:** WORKING
* **Files:** edited `PROJECT.md`
* **Details:** added isOrganizer flag, hardcoded admin (id 1), admin promotes organizers, only organizer/admin scan. Open: Java version, who creates events.

### 2026-10-20 — Claude (Sonnet 5.5) — T04: user-service change

* **Task:** T04
* **Status:** WORKING
* **Files:** rewrote `PROJECT.md`
* **Details:** attendance-service renamed to user-service (users + simple auth + attendance). Event registration now takes a userId. Java version open: system is Java 8 only.

### 2026-10-20 — Claude (Sonnet 5.5) — T04: draft PROJECT.md

* **Task:** T04 — design discussion
* **Status:** WORKING
* **Files:** created `PROJECT.md`; edited `log.md`
* **Details:** recorded confirmed choices (H2 per service, no gateway, 3 services, static Tailwind client served by Spring Boot, webcam + paste scan, reject duplicates). Awaiting owner sign-off on the endpoint table before any code.

### 2026-10-20 — Claude (Sonnet 5.5) — T01: init CLAUDE.md and log.md

* **Task:** T01 — reset both files as generic templates for a new Java project
* **Status:** DONE
* **Files:** edited `CLAUDE.md`, `log.md`
* **Details:** kept the process rules (log tracking, archive skip, workflow, dependencies, git, end-of-run); dropped all MITPass-specific tables and history.
* **Notes / Errors:** project name, stack and commands are left as TODO placeholders.
