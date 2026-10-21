# Demo script: Event Check-in

Total: about 8 minutes. Five parts, in this order: problem, approach, solution, tech, live demo.

## Pre-flight checklist (15 minutes before)

- [ ] Laptop charged and plugged in; notifications off; browser zoom 100%.
- [ ] Internet works (Tailwind, fonts and the QR scanner load from CDNs).
- [ ] Run `.\start-demo.ps1`. It takes about 1 minute and ends with "Ready". It wipes and reseeds everything, so do this last, right before presenting.
- [ ] Browser on http://localhost:9080, **logged out** (log out if you tested). Demo mode box visible.
- [ ] Camera: click Start camera once as organizer, allow the permission, then log out. Phone ready with brightness up if you scan from it.
- [ ] Event list shows "AI & Robotics Expo" as Full and the other three with seats left.
- [ ] Fallback video opens: `demo-fallback\demo-fallback.webm` (about 2 minutes, no sound). Keep it in a second window.
- [ ] Know the reset: `.\start-demo.ps1` (everything), `.\start-demo.ps1 -Only event-service` (one service), `.\stop-demo.ps1` (stop all).

If the demo is mid-way and breaks: do not debug live. Play the fallback video and talk over it.

---

## 1. The problem (1 min)

* Campus events still check people in with paper lists, spreadsheets or a student ID typed in by hand.
* Queues at the door are slow, names get misspelled, and the same person can be ticked twice or walk in without registering.
* Organizers have no live answer to "how many registered, how many actually came?"

## 2. How we approached it (1 min)

* Break the job into three things that change independently: **who you are** (users), **what you signed up for** (events), **proof you may enter** (the pass).
* Make the proof a QR code. Scanning is the fastest possible check at a door, and one code can be used only once.
* Keep it small on purpose. It is a demo, so we spent the time on the experience, not on production concerns (see "Deliberately not done").

## 3. Our solution (1 min)

Attendee: **sign up, pick an event, register, get a ticket with a QR.**
Organizer: **scan the QR, see a big green or red result with the attendee's name, watch the live count per event.**
Admin: **create events and promote users to organizer.**

The whole thing is one web page and three small services.

## 4. The tech (2 min)

```
 browser (client page, :9080)
   |-- user-service  :9083   users, login, attendance (check-ins)
   |-- event-service :9081   events, registrations, stats
   |-- pass-service  :9082   passes, QR image
```

* **Three microservices**, each with its own H2 database file. They call each other over plain HTTP (REST), no gateway.
* **Spring Boot 4 / Java 21**, Spring MVC, JPA with Hibernate, Maven wrapper. Nothing is installed globally: the JDK lives in the project.
* **Registering** (event-service) calls pass-service to create a pass with a random token. The QR image encodes that token (ZXing).
* **Scanning** (user-service) asks pass-service to verify the token, rejects a second scan ("already checked in"), then asks event-service who the registration belongs to so the result can show a name.
* **Roles**: one hardcoded admin; admin promotes organizers. Only organizer/admin can scan.
* **Frontend**: one static HTML page, Tailwind via CDN, plain JavaScript `fetch`, camera scanning with html5-qrcode.

## 5. Live demo (3 min)

The login page has a **Demo mode** box listing the accounts. Click a row to log in. Password = username.

| Step | Do this | Say / point out |
|-|-|-|
| 1 | Logged-out page: scroll the event list | Anyone can browse events without an account. |
| 2 | Click **demo** in the Demo mode box | Logged in as an attendee. |
| 3 | Click **Register** on "Hack the Campus 2026" | Button turns into "View pass". A registration and a pass were created across two services. |
| 4 | **My passes** tab | Ticket with the QR. Tap the QR to enlarge it. |
| 5 | **Log out**, click **organizer** | Organizers get a Scan tab. Attendees do not. |
| 6 | **Scan** tab. Show the live per-event bars | Counts are checked-in / registered. Alice already has two passes from the seed data. |
| 7 | Scan the demo user's pass: camera on the QR (phone or enlarged on screen) or paste the token | Big green "Checked in" with the name and event. |
| 8 | Scan the same pass again | Red "Already checked in". One pass, one entry. |
| 9 | Log out, click **alice**, open **My passes**, copy a token, log in as organizer, scan it | Second attendee, second event. The bar and list update. |
| 9b | On the attendee's side (second window), the pass flips to a green **Checked in** stamp within 3 seconds | Attendee sees the result too. Optional, needs two windows. |
| 10 | Log out, click **admin**, **Admin** tab: scroll to **Who checked in** | Admin sees each event with the names and times of everyone checked in. |
| 11 | Same tab: create an event with a capacity, promote a user | New event shows in the list straight away. A capacity of 1 shows "Full" once taken (the AI & Robotics Expo is already full in the seed data). |

Tip for step 7: if the camera is flaky, use the paste box. It exercises exactly the same code path.

---

## Deliberately not done (say it before they ask)

This was built in a one-hour budget with the experience as the priority. These are excluded on purpose, and listed in [PROJECT.md](PROJECT.md):

* **No security:** passwords are plain text, endpoints are open.
* **No hardening:** almost no input validation or edge-case handling.
* **No reliability work:** no retries or recovery if a service is down.
* **No speed optimisation.**
* **No automated tests.**

## If something goes wrong

| Symptom | Fix |
|-|-|
| Events or login do nothing | A service is down. Run `.\start-demo.ps1` again. |
| Page unstyled, camera button errors | No internet (Tailwind, fonts and the scanner load from CDNs). |
| Camera will not start | Use `http://localhost` (not an IP), allow the camera, or use the paste box. |
| Registering gives an error right after a restart | Log out and log in again (old saved token). |
| Demo data looks used up | `.\start-demo.ps1` resets everything. |
