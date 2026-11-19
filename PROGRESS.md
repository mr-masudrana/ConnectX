# PROGRESS — ConnectX

## Current step
**Step 2: DB schema** (SQL written, needs first CI run of `Database check`)

## Steps
| # | Step | Status | Done when |
|---|------|--------|-----------|
| 0 | Project setup | ✅ done (PR #11, CI green) | Gradle project builds, ktlint + detekt pass |
| 1 | CI/CD | ✅ done (keystore + tag test deferred until first release) | CI on every PR; env-based release signing |
| 2 | DB schema | ⏳ current | `supabase/migrations`: users, prekeys, pending_messages, blocks, reports, reserved_usernames, RLS, indexes, pg_cron cleanup |
| 3 | Auth & profile | todo | Sign up, OTP, profile (name, photo) |
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
- Dependabot ignores AGP major, Kotlin major/minor, KSP bumps (update these manually together)

## Next agent: start here
- Step 2 files exist: `supabase/migrations/0001..0007`, `supabase/tests/database/schema_rules.test.sql` (pgTAP), `supabase/config.toml`, `.github/workflows/db-check.yml`.
- **Not yet run.** First job: make `Database check` CI green (it runs `supabase start` + `supabase test db`). Fix SQL or test errors; do not weaken RLS to make tests pass.
- Then: owner creates a Supabase project and sets secrets `SUPABASE_ACCESS_TOKEN`, `SUPABASE_DB_PASSWORD`, `SUPABASE_PROJECT_ID`; `supabase-deploy.yml` then pushes migrations on merge to main.
- Known design notes: blocked senders are silently dropped (trigger); `is_blocked` is not callable by clients; delivery/seen receipts will travel as encrypted control messages (Step 8/9), not DB columns.
- (older notes below)
- Step 0 is merged and CI is green (ktlint, detekt, unit test, debug APK).
- Step 1 remaining:
  1. Verify CI on the PR that adds signing config stays green.
  2. Owner creates a release keystore and sets GitHub Secrets (see `SETUP_GITHUB.md`) — only needed before the first release tag.
  3. Test a tag (e.g. `v0.1.0-test`) to confirm the signed AAB builds; delete the tag afterwards.
- Add a launcher icon (none yet).
- Then start Step 2 (DB schema).

## Known issues
- Do NOT merge Dependabot's AGP 9.x bump until a deliberate Gradle/Kotlin plugin migration.
- Repository is currently Public; make it Private before launch work.
- `google-services.json` is not used yet (FCM arrives in Step 10); release workflow treats it as optional.

## Log (newest first)
- Step 0 merged: wrapper generated via workflow, CI green.
- Fixed invalid `hashFiles()` usage in workflows.
