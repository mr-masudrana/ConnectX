# ConnectX

End-to-end encrypted Android messenger with Telegram-style usernames.
Package: `com.connectx.app`

- Server only relays encrypted blobs and deletes them after delivery.
- Message history stays on the device (SQLCipher-encrypted).
- Stack: Kotlin, Jetpack Compose, Hilt, Room, Supabase, Cloudflare R2/Workers, FCM, libsignal.

## For contributors and AI agents
1. Read `AGENTS.md` (rules, stack, structure)
2. Read `PROGRESS.md` (current step, notes)
3. One step = one branch = one PR

## Local setup
Copy `.env.example` to `.env` (and put Android keys in `local.properties`). Never commit secrets.
See `SETUP_GITHUB.md` for repository and CI setup.
