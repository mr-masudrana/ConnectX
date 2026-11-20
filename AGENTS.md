# AGENTS.md — ConnectX

Read this file and `PROGRESS.md` before doing anything. Continue from "Current step" in `PROGRESS.md`.

## Project
- **App name:** ConnectX
- **Package / applicationId:** `com.connectx.app`
- **Platform:** Android only (minSdk 26, targetSdk = latest stable)
- **What it is:** E2EE messaging app with Telegram-style usernames. The server only relays encrypted blobs and deletes them after delivery. History lives only on the device.

## Stack (FIXED — do not add or swap libraries without owner approval)
- Language: Kotlin, Gradle Kotlin DSL, version catalog (`gradle/libs.versions.toml`)
- UI: Jetpack Compose + Material 3
- Architecture: MVVM, Hilt (DI), Coroutines + Flow, single-activity, Navigation Compose
- Local DB: Room + SQLCipher (encrypted)
- Background/retry: WorkManager
- Backend: Supabase (Auth, Postgres, Realtime, pg_cron) via `supabase-kt` (BOM) + Ktor OkHttp engine + kotlinx.serialization
- Media storage (Phase 2): Cloudflare R2 via Cloudflare Workers (signed URLs)
- Push: Firebase Cloud Messaging ONLY (no Firebase Auth/Firestore)
- E2EE: official `libsignal` (Signal Protocol). Never write custom crypto.
- Private keys: Android Keystore only
- Lint/format: ktlint + detekt
- CI/CD: GitHub Actions (Linux runner)

## Folder structure
```
app/src/main/java/com/connectx/app/
  core/            # common utils, result types, constants
  di/              # Hilt modules
  data/
    local/         # Room, DAOs, SQLCipher
    remote/        # supabase-kt, API clients
    repository/
  domain/
    model/
    repository/    # AuthRepository, ProfileRepository interfaces
    transport/     # MessageTransport interface
    crypto/        # CryptoEngine interface
  crypto/          # libsignal implementation
  transport/       # SupabaseRealtimeTransport
  feature/
    auth/  profile/  username/  chat/  settings/  safety/
  ui/              # AppRoot, shared components, theme
supabase/
  migrations/      # SQL, numbered: 0001_xxx.sql
  functions/
workers/           # Cloudflare Workers (rate limit, R2 signed URLs)
.github/workflows/
```

## Rules
1. One step = one branch (`step-N-short-name`) = one Pull Request. Never mix steps.
2. Work only on the "Current step". Do not start the next step.
3. Commit small and often. Conventional commits (`feat:`, `fix:`, `chore:`).
4. Update `PROGRESS.md` **during** work, not only at the end. Your limit may end at any time.
5. Naming: classes `PascalCase`, functions/vars `camelCase`, packages lowercase. Files named after the main class.
6. Code against interfaces: `MessageTransport` and `CryptoEngine` are contracts. Do not change their signatures without noting it in `PROGRESS.md` under "Decisions".
7. Every step needs tests where logic exists. CI must be green before the PR is done.
8. Never commit secrets. Use `local.properties`, GitHub Secrets, and `.env.example` with placeholders.
9. No plaintext message content in logs, push payloads, or the server DB. Ever.
10. Security-sensitive code (crypto, key storage, RLS) is written by one agent and must be reviewed in Step 12. Do not "improve" it casually.
11. Keep UI strings in `strings.xml` (Bengali `values-bn` + English default).

## Core design decisions
- **Identity:** internal `user_id` (UUID). Username is a changeable alias, never the identity.
- **Username:** `citext`, unique, regex `^[a-z][a-z0-9_]{4,31}$`, reserved names blocked, old names reserved 30 days, exact-match search only, rate limited.
- **Server message table `pending_messages`:** `id, recipient_id, sender_id, encrypted_blob, created_at`. Index on `(recipient_id, created_at)`. Deleted immediately on recipient ACK. `pg_cron` deletes anything older than 30 days.
- **Push:** FCM payload has no content, only a "new message" signal.
- **Typing/online status:** Realtime Presence/Broadcast, never written to the DB.
- **Media (Phase 2):** encrypt on device with random AES-256 key, upload to R2, key travels inside the Signal-encrypted message, separate download ACK, R2 lifecycle rule as 30-day failsafe.
- **Multi-device:** NOT in MVP. One user = one device.

## Definition of Done (every step)
- [ ] Code builds, `./gradlew ktlintCheck detekt test` passes
- [ ] CI green
- [ ] `PROGRESS.md` updated (step marked done, decisions, notes for next agent)
- [ ] PR opened with a short summary

## Start-of-session prompt
> Read `AGENTS.md` and `PROGRESS.md`. Continue the "Current step". Do not add libraries. Commit small. Update `PROGRESS.md` as you go and before you stop.
