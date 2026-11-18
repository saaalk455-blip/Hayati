# CLAUDE.md — Hayati Development & Collaboration Protocol

This file is the permanent protocol for the Hayati Android application.
Read it, `docs/PROJECT_STATUS.md` and the relevant feature docs before every task.

- Repository: https://github.com/saaalk455-blip/Hayati
- Default branch: `main`
- Development workflow: Claude Push

## 1. Roles

| Role | Responsibilities |
|---|---|
| ChatGPT | Product architecture, feature specifications, UX/UI scenarios, engineering instructions, code review guidance, acceptance criteria |
| Claude | Inspect the repository, implement approved specifications, write and maintain code, run tests and builds, fix implementation bugs, commit and push, maintain documentation |
| GitHub | Source of truth, commit history, branch management, version tracking, build automation when configured |
| Human owner | Gives development instructions, tests on a real Android device, reports actual device behavior, approves major product decisions |

Never claim another team member performed an action unless its result is verifiable.

## 2. Repository rules

Before every task:

1. Fetch the latest repository state.
2. Inspect the current branch.
3. Inspect recent commits.
4. Read `CLAUDE.md`.
5. Read `docs/PROJECT_STATUS.md`.
6. Read the relevant feature documentation.
7. Inspect all files related to the requested feature.

Rules:

- Never assume the repository is unchanged since the previous session.
- Never overwrite unrelated work.
- Never force push.
- Never delete project files without explicit approval.
- Never expose credentials, API keys, passwords, signing keys, or private user data.
- The repository is public: `.gitignore` must exclude sensitive files, local configuration, signing credentials, and build artifacts.

## 3. Branch and commit policy

Branch naming: `feature/<name>`, `fix/<name>`, `refactor/<name>`, `docs/<name>`.

Commit messages:

```
feat: add prayer tracking
fix: correct prayer notification scheduling
refactor: improve dashboard architecture
docs: update project roadmap
test: add habit repository tests
```

- Do not push unfinished work to `main`.
- Push completed feature branches and report their commit hashes.
- Do not merge into `main` until acceptance criteria and build checks pass.

## 4. Task execution

Every development task must define: Task ID, feature name, objective, files expected to change, technical implementation, dependencies, acceptance criteria, unit tests, integration tests where applicable, and build verification.

Do not expand a task beyond its approved scope. If an architectural issue is found, document it and explain its impact before changing unrelated modules.

## 5. Code quality

- Kotlin best practices, Jetpack Compose best practices.
- MVVM, Clean Architecture, SOLID, Dependency Injection.
- Lifecycle-aware state management, proper error handling.
- Responsive RTL layouts.
- No business logic inside Composable functions.
- No duplicated code or oversized classes.
- Completed features use real persistent data, not temporary hardcoded values.

## 6. Testing requirements

Before reporting a task complete:

1. Run relevant unit tests.
2. Run database tests if applicable.
3. Run UI tests where practical.
4. Run the appropriate Gradle build.
5. Inspect build errors and warnings.
6. Verify existing functionality was not broken.

If a test cannot be executed, report exactly why. Never fabricate test results.

## 7. Documentation to keep updated

- `CLAUDE.md`
- `docs/PROJECT_STATUS.md` (after every completed task)
- `docs/ROADMAP.md`
- `docs/CHANGELOG.md` (meaningful changes)
- `docs/DECISIONS.md` (architectural decisions)
- `docs/TEST_REPORT.md`
- `docs/KNOWN_ISSUES.md` (unresolved issues with severity)

## 8. Mandatory final report (after each task)

```
TASK ID:
TASK NAME:

STATUS: Completed | Partially completed | Blocked

IMPLEMENTATION:
- What was implemented / how it works
- Files created / Files modified

TESTING:
- Tests executed / passed / failed
- Build result

GITHUB:
- Repository / Branch / Commit hash / Push result / PR URL if applicable

REMAINING WORK:
- Unfinished requirements / Known bugs / Dependencies

NEXT RECOMMENDED TASK:
- Exact next development objective
```

## 9. Communication rule

Reports must be factual. Never call a feature complete when only its UI exists. Always distinguish between:

- UI implemented
- Logic implemented
- Data persistence implemented
- Notifications implemented
- Device tested
- Fully verified

## 10. Current phase

Phase 2 (foundation, PR #2) was owner-tested and is awaiting merge. Phase 3 (tasks, PR #3,
`feature/tasks-persistence`) awaits owner device testing and merge. Phase 4 (professional prayer
system, version 0.4.0) is implemented on `feature/prayer-system`, stacked on Phase 3 with the
owner's explicit approval ("موافق"); it passes CI (137 tests) but is **not device-tested**. Quran,
adhkar, tasbeeh and statistics data are not implemented.
**Do not start the next phase or implement features until a specification is approved.**
See `docs/PROJECT_STATUS.md` for the actual state.

Build facts: this authoring environment has no Android SDK and no Maven access, so builds and
unit tests run in GitHub Actions (`.github/workflows/android-build.yml`); check the CI run result
instead of claiming a local build.
