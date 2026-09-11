# Palmlens — manual setup

The app builds and runs with no keys (readings are stubbed). Add an OpenAI key to turn on
real readings. Each item below unlocks a later phase.

## What's already wired (no action needed)

- Room DB (`palmlens.db`), DataStore profile, `java.time` + desugaring
- 10 free lifetime scans → `ScanQuotaRepository`; the next scan routes to the paywall *before* any
  generation call
- CameraX capture + on-device preprocess (crop / EXIF-rotate / resize 1024 / JPEG 85 /
  GPS stripped) — all off the main thread
- **Phase 3 (OpenAI) is implemented** — `OpenAiContentGenerator` over a hand-rolled OkHttp
  client. When `OPENAI_API_KEY` is set it replaces `StubContentGenerator` automatically
  (`ContentGeneratorModule` picks at runtime); blank key → stays on the stub.
- **Phase 4** retry / fallback pool, **Phase 5** daily WorkManager notification (~06:00),
  **Phase 7** AdMob + UMP, **Phase 8** Settings screen, **Phase 9** debug overlay.
- **Phase 9 debug overlay:** in a `debug` build, long-press the palm-scan result to see the
  model id, prompt version, latency, token usage, `finish_reason`, raw JSON, per-line
  confidence, `imageQuality`, scan/paywall counters and the interstitial state. Nothing to
  set up; it never appears in release builds.

## Phase 3 — OpenAI (turn it on)

Flow: **capture palm → image (base64) + templated prompt → GPT Chat Completions with
`response_format: json_schema` → JSON → domain model.** Text features are the same, text-only.

**The API key ships inside the APK.** It can be extracted from a release build. Accepted for
the MVP — mitigate it:

1. Get a key at <https://platform.openai.com/api-keys>.
2. Set a **hard monthly usage limit**:
   <https://platform.openai.com/settings/organization/limits>. Start low (e.g. $20–50).
3. Put it in `local.properties`: `OPENAI_API_KEY=sk-...` — then rebuild/reinstall.
4. Rotate the key immediately if usage spikes unexpectedly.
5. **Later**, to move the key off the device with no app change: deploy a ~30-line proxy
   and set `OPENAI_BASE_URL=https://your-proxy.example/v1` in `local.properties`.

**Models — you must set these.** Defaults `OPENAI_VISION_MODEL` / `OPENAI_TEXT_MODEL` are
both `gpt-5.6-luna`, which is a **guess**. Check what your account can call at
<https://platform.openai.com/docs/models> and set the real ids in `local.properties` — the
vision model must accept image input; both need Structured Outputs (`json_schema`). A wrong
id shows "the stars are cloudy" on the scan screen and logs the OpenAI error under the
`Palmlens` logcat tag.

**No Firebase.** Remote Config is gone. Prompts live in `app/src/main/assets/prompts/`,
model names in `local.properties`. The free-scan cap (10) is a constant in
`ScanQuotaRepositoryImpl` — change it and rebuild. If you later want to tune prompts without
a release, host a small `config.json` anywhere and fetch it on launch (a Phase 3.5 add).

## AdMob — Phase 7 (implemented)

- Create an AdMob account + app. Create **one adaptive banner** and **one interstitial** ad
  unit. Put all three ids in `local.properties`:
  `ADMOB_APP_ID` (the `ca-app-pub-…~…` app id — a wrong/blank value in a **release** build
  crashes on launch at `MobileAds.initialize`), `ADMOB_BANNER_UNIT`, `ADMOB_INTERSTITIAL_UNIT`.
  **Debug builds ignore these and always use Google's public test ids** (clicking a live ad
  on your own build violates AdMob policy).
- AdMob → **Privacy & messaging** → create the **GDPR / UMP** consent message; set the max
  ad content rating; complete the US-state settings. Host **app-ads.txt**.
- What's wired: `ads/AdManager` runs the UMP consent flow on launch then inits the SDK once;
  an adaptive banner sits on Home / Horoscope / Highlights / Love / Tarot; an interstitial is
  preloaded during each scan and shown on the result screen's exit for scans 2 through the cap. The
  "Privacy options" form is surfaced in **Settings** (Home → ⚙) when UMP says the region needs
  it. Premium-user suppression is a one-line `isPremium` hook in `AdManager`, wired when Play
  Billing (Phase 6) lands.

## Settings + localisation — Phase 8 (partly done)

- **Done:** `SettingsScreen` (Home → ⚙) — change language, review ad-consent (region-gated),
  "Delete my data" (wipes Room + DataStore, restarts at the splash), the "for entertainment
  purposes only" disclaimer + app version. `values/strings.xml` now holds `app_name`, the
  disclaimer, and every Settings string.
- **Deferred — full string extraction.** The other ~100 hard-coded UI strings across ~15
  screen files are still literals. Extracting them has no user-visible effect until a
  translated `values-<locale>/strings.xml` exists, and several are plurals / format strings
  worth getting right *with* a translator. Do this pass when translations are commissioned.
- **Deferred — the 8 non-English resource dirs** (`values-hi`, `values-es`, `values-b+pt+PT`,
  `values-ja`, `values-b+zh+Hans`, `values-fr`, `values-de`, `values-it`). Android already
  falls back to `values/` for any locale; empty English copies would just be dead weight.
  Create each when its translation lands.
- **Deferred — switching the *UI* language in-app.** The Settings "Language" row sets
  `UserProfile.languageCode`, which drives the language of *readings* (prompts). Making the
  Android UI follow it too needs `androidx.appcompat` + `AppCompatDelegate.setApplicationLocales`
  (or per-app language on API 33+) — add that alongside the first real translation.
- **Not built — "restore purchases"** in Settings: there's no billing (Phase 6 skipped).

## Play Console — Phase 6 (Google Play Billing)

- Create the app. Create **3 auto-renewing subscriptions** with ids **exactly**
  `palmlens_weekly`, `palmlens_monthly`, `palmlens_yearly` — **ids are permanent**.
  Placeholder prices now; attach **price templates** for the 9 launch markets.
- Create an **upload keystore** (`keytool -genkey ...`); store it + passwords safely,
  outside the repo.
- Fill **Data safety**: the palm image is *transmitted* to OpenAI for processing and *not
  stored*; advertising id + AdMob collection. Link the **privacy policy** URL. Add the
  astrology **"for entertainment purposes only"** disclaimer.

## Content you generate + hand-review — Phases 1 / 4 / 8

- `app/src/main/assets/fallback/horoscopes.json` — 12 signs × 9 locales × 2 **evergreen**
  entries (nothing datable). English is drafted; the other 8 locales are `"TODO"`.
- `app/src/main/assets/tarot/deck.json` — 78 cards listed; verify names/emoji.
- Translations for the 8 non-English `strings.xml` files (created in Phase 8).

## Running the tests

- Unit: `./gradlew :app:testDebugUnitTest`
- Instrumented (needs a device/emulator): `./gradlew :app:connectedDebugAndroidTest`
