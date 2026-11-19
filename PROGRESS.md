# PROGRESS — ConnectX

## Current step
**Step 0: Project setup** (files written, awaiting first build check)

## Steps
| # | Step | Status | Done when |
|---|------|--------|-----------|
| 0 | Project setup | ⏳ files written, needs build check | Gradle project `com.connectx.app` builds, folder structure from AGENTS.md, ktlint + detekt configured, `.gitignore`, `.env.example`, empty Compose "Hello ConnectX" screen runs |
| 1 | CI/CD | todo | GitHub Actions: lint + test + debug APK build on every PR; signed AAB on tag |
| 2 | DB schema | todo | `supabase/migrations`: users, prekeys, pending_messages, blocks, reports, reserved_usernames, RLS, indexes, pg_cron cleanup |
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

## Next agent: start here
- Step 0 skeleton exists: Gradle KTS, version catalog, Hilt app, Compose "Hello ConnectX" screen, ktlint + detekt, bn/en strings, folder structure (`.gitkeep` placeholders).
- **It has NOT been built yet.** First job: generate the wrapper (`gradle wrapper --gradle-version 8.9`, creates `gradlew` and `gradle/wrapper/gradle-wrapper.jar`), run `./gradlew ktlintCheck detekt testDebugUnitTest assembleDebug`, fix any version/plugin errors, then mark Step 0 done.
- Add a launcher icon (none yet; default icon is used).
- Then start Step 1 (CI/CD): release signing in `app/build.gradle.kts` must read `KEYSTORE_PATH`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD` from env.

## Known issues
- Fixed: workflows used `hashFiles()` in job-level `if` (invalid) which made every run fail. Gates now use a first step writing `ready=true/false`.
- Do NOT merge Dependabot's AGP 9.x bump until Gradle/Kotlin plugin migration is done deliberately.
- `gradle-wrapper.jar` and `gradlew` are missing (cannot be generated without Gradle installed). CI skips until `gradlew` exists.
- Versions in `libs.versions.toml` are unverified; bump if sync fails.

## Log (newest first)
- (agents: add one line per session — date, what you did, what is left)
