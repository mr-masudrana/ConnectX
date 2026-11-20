# PROGRESS — ConnectX

## Current step
**Step 3: Auth & profile** (code written, needs CI + Supabase project setup + on-device test)

## Steps
| # | Step | Status | Done when |
|---|------|--------|-----------|
| 0 | Project setup | ✅ done (PR #11, CI green) | Gradle project builds, ktlint + detekt pass |
| 1 | CI/CD | ✅ done (keystore + tag test deferred until first release) | CI on every PR; env-based release signing |
| 2 | DB schema | ✅ done (DB check green, PR #12) | `supabase/migrations`: users, prekeys, pending_messages, blocks, reports, reserved_usernames, RLS, indexes, pg_cron cleanup |
| 3 | Auth & profile | ⏳ current | Sign up, OTP, profile (name, photo) |
| 4 | Username | todo | Validation, uniqueness, exact-match search, Worker rate limit, 30-day reservation |
| 5 | Local DB | todo | Room + SQLCipher, message/chat models, key from Keystore |
| 6 | MessageTransport | todo | Interface + Supabase Realtime implementation |
| 7 | E2EE | todo | libsignal, key generation, prekey upload, encrypt/decrypt (interface `CryptoEngine`) |
| 8 | 1-to-1 chat UI | todo | Chat list, chat screen, sent/delivered/seen |
| 9 | ACK-delete & offline queue | todo | Server delete after ACK, WorkManager retry |
| 10 | Push (FCM) | todo | Content-free push, fetch + decrypt on device |
| 11 | Block, report, privacy settings | todo | Block/report, searchable-by-phone/username toggles |
| 12 | Security review | todo | Independent review of crypto, key storage, RLS |

Phase 2 (after MVP): media via R2, typing/online, reply/edit/delete/reactions, message requests, QR.
Phase 3: groups (Sender Keys), calls (WebRTC), encrypted backup, multi-device, disappearing messages.

## Decisions made
- Android only, Kotlin + Jetpack Compose, MVVM + Hilt
- Backend: Supabase + Cloudflare (R2, Workers); FCM for push only
- App name: ConnectX, package: `com.connectx.app`
- Release signing reads `KEYSTORE_PATH`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD` env vars; unsigned if absent
- Workflow gates use a first step that writes `ready=true/false` (never `hashFiles` in job-level `if`)
- Repository interfaces live in `domain/repository`, Supabase implementations in `data/repository`
- Supabase URL/anon key come from env (CI) > `local.properties` > placeholder (BuildConfig)
- Dependabot ignores AGP major, Kotlin major/minor, KSP bumps (update these manually together)

## Next agent: start here
- Step 3 code is written (email OTP sign-in, profile setup with username, root state machine, bn/en strings, unit tests). **Not yet built.** First job: make CI green (ktlint, detekt, tests, assembleDebug) and fix any supabase-kt API mismatches (written for supabase-kt 3.0.3 / Ktor 3.0.3).
- Owner must: create the Supabase project, set GitHub secrets (`SUPABASE_URL`, `SUPABASE_ANON_KEY`, `SUPABASE_ACCESS_TOKEN`, `SUPABASE_DB_PASSWORD`, `SUPABASE_PROJECT_ID`), set the email templates to include `{{ .Token }}` (both "Confirm signup" and "Magic Link"), configure custom SMTP, then run Actions > Deploy Supabase.
- Then test on a device: email -> code -> profile -> welcome screen; kill and reopen the app (session must persist); sign out.
- Open items: resend-code cooldown, session storage is plain SharedPreferences (move to encrypted storage), R8 rules for release (minify is on; verify a release build runs), `SessionStatus.RefreshFailure` currently maps to LOADING (offline start with expired token would spin).
- After Step 3 is verified: Step 4 (username search UI + Worker rate limit).

## Known issues
- Do NOT merge Dependabot's AGP 9.x bump until a deliberate Gradle/Kotlin plugin migration.
- Repository is currently Public; make it Private before launch work.
- `google-services.json` is not used yet (FCM arrives in Step 10); release workflow treats it as optional.

## Log (newest first)
- Step 3 second CI run: Gradle could not resolve `kotlin-serialization:2.0.20` (parent POM `kotlin-gradle-plugins-bom:2.0.20` missing via the plugin portal). Removed the kotlinx.serialization compiler plugin; rows are parsed as `JsonObject` instead. If `@Serializable` is needed later, bump Kotlin (and KSP) together to a version whose plugin resolves.
- Step 3 first CI run failed only on ktlint style (functions with 2+ params must be multi-line; multi-line `=` expressions start on a new line). Fixed in build script, repositories, ViewModel, Compose screens and test fake. Detekt/compile/tests not yet seen.
- Step 3 code written (email OTP + profile).
- Step 2 merged: DB check green on PR and main.
- Step 0 merged: wrapper generated via workflow, CI green.
- Fixed invalid `hashFiles()` usage in workflows.
