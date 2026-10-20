# CLAUDE.md

## Context & Status Tracking (required)

Before doing anything, read `log.md` up to the latest Change Log entry to get the current context and task status. While working, use `log.md` as your todo list: add tasks as `[ ]`, mark `[~]` when you start, `[x]` when done (or `[!]` with notes if it fails), and log every change to files in this directory so later agents can diagnose and resume after a mid-run failure.

**`archive/` (skip by default):** old CI and error logs live here. Don't read, search or list it unless the current task needs a past failure (a recurring or similar error, or a regression).

## Project

<!-- TODO: project name, one-paragraph goal, and where the plan lives (e.g. PROJECT.md). -->

## Stack

<!-- TODO: pin versions here (Java, build tool, framework). Pin them in one place only (root pom.xml / build.gradle). -->

## Workflow for any change

1. **Orient** as in *Context & Status Tracking*. Work on a branch, not on `main`.
2. **Decide.** If the change alters a goal or a design rule, say so and get the owner's agreement first, and record it in the plan/docs before writing code.
3. **Plan** the tasks in `log.md`. Split anything bigger than one sitting into steps that each have a "done when" check.
4. **Build test-first, in small steps.** Failing test, then code. Business rules live in the service/domain layer, not in controllers or SQL.
5. **Check.** Run the build and the full test suite (e.g. `./mvnw verify`) before calling anything done.
6. **Review** the diff before committing and fix what the review finds.
7. **Document.** Update README, the plan and `log.md` for anything that changed behaviour.
8. **Commit** only as *Git & Commits* allows.
9. **Finish** as in *End of Run Behavior*.

## Working rules

- Follow the plan. If a task conflicts with it, say so before coding.
- If a goal or requirement changes, update every document that states the old one.
- Tunable values (limits, timeouts, URLs) go in config, not in code.
- Never edit an applied DB migration; add a new one.
- Published contracts (REST responses, events, tokens) only grow; never rename or remove fields.
- Add tests for what you build or change. If bugs turn up mid-run, fix them (with a test) and log them in `log.md`.
- Platform is Windows and paths may contain spaces: quote paths in shell commands. Check the system `java` version; use the wrapper or a project-local JDK if it isn't the pinned one.

## Dependencies (strict)

Keep every install local to the project: Maven/Gradle through the committed wrapper, npm without `-g` (use `npx`), services through `docker compose`. Never install anything globally (system packages, JDK/Maven/Node, PATH or registry changes) without asking and getting explicit permission; log any approved global install in `log.md`.

## Git & Commits

- Ask before committing; commit only with explicit permission, for that commit only. Don't push unless asked.
- Message format: `<type>: <summary>` (e.g. `feat:`, `fix:`, `chore:`).
- No dependency or build artifacts tracked. Review `.gitignore` after every install or update.

## Working with the owner

- Ask about critical decisions mid-run.
- After 7 attempts at the same step without a passing check, stop and report what you tried, what failed, and the options you see.

## End of Run Behavior

Before the final summary:

- Terminate all running instances unless explicitly asked to leave one up.
- Leave 0 background tasks and 0 open sockets.
- Update the docs related to the task/bug to reflect what changed.
