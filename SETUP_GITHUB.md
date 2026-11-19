# GitHub Setup (one time)

## 1. Create repository
- Name: `connectx`, visibility: **Private**
- Push this folder's contents to `main`

```bash
git init -b main
git add .
git commit -m "chore: initial project setup"
git remote add origin git@github.com:YOUR-USERNAME/connectx.git
git push -u origin main
```

## 2. Edit CODEOWNERS
Replace `@YOUR-GITHUB-USERNAME` in `.github/CODEOWNERS`.

## 3. Branch protection (Settings > Branches > Add rule for `main`)
- Require a pull request before merging
- Require status checks: `Lint, test, build` and `Secret scan`
- Require branches to be up to date
- Do not allow force pushes

## 4. Secrets (Settings > Secrets and variables > Actions)

| Secret | Used by | How to get |
|---|---|---|
| `KEYSTORE_BASE64` | release | `base64 -w0 release.keystore` |
| `KEYSTORE_PASSWORD` | release | your keystore password |
| `KEY_ALIAS` | release | key alias |
| `KEY_PASSWORD` | release | key password |
| `GOOGLE_SERVICES_JSON_BASE64` | release | `base64 -w0 google-services.json` (FCM) |
| `SUPABASE_URL` | release | Supabase project settings |
| `SUPABASE_ANON_KEY` | release | Supabase project settings |
| `SUPABASE_ACCESS_TOKEN` | supabase deploy | Supabase account tokens |
| `SUPABASE_DB_PASSWORD` | supabase deploy | set at project creation |
| `SUPABASE_PROJECT_ID` | supabase deploy | project reference ID |
| `CLOUDFLARE_API_TOKEN` | workers deploy | Cloudflare dashboard > API tokens |
| `CLOUDFLARE_ACCOUNT_ID` | workers deploy | Cloudflare dashboard |

Release keystore: create once and **back it up safely**. If lost, you cannot update the app on Play Store.
```bash
keytool -genkey -v -keystore release.keystore -alias connectx \
  -keyalg RSA -keysize 2048 -validity 10000
```

## 5. External accounts
- **Supabase:** create project, enable Phone/Email auth, enable `pg_cron` and `citext` extensions
- **Firebase:** create project, add Android app `com.connectx.app`, download `google-services.json` (FCM only)
- **Cloudflare:** create R2 bucket `connectx-media` (used from Phase 2)

## 6. Start the first agent
> Read `AGENTS.md` and `PROGRESS.md`. Continue the "Current step". Do not add libraries. Commit small. Update `PROGRESS.md` as you go and before you stop.

## Notes
- CI and release jobs skip automatically until Step 0 creates `gradlew`.
- Step 1 must wire release signing in `app/build.gradle.kts` to read `KEYSTORE_PATH`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD` from environment variables.
