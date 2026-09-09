# Palmlens — manual setup

The app builds and runs with no keys (readings are stubbed). Add an OpenAI key to turn on
real readings. Each item below unlocks a later phase.

## What's already wired (no action needed)

- Room DB (`palmlens.db`), DataStore profile, `java.time` + desugaring
- 40 free lifetime scans → `ScanQuotaRepository`; scan 41 routes to the paywall *before* any
  generation call
- CameraX capture + on-device preprocess (crop / EXIF-rotate / resize 1024 / JPEG 85 /
  GPS stripped) — all off the main thread
- **Phase 3 (OpenAI) is implemented** — `OpenAiContentGenerator` over a hand-rolled OkHttp
  client. When `OPENAI_API_KEY` is set it replaces `StubContentGenerator` automatically
  (`ContentGeneratorModule` picks at runtime); blank key → stays on the stub.

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
model names in `local.properties`. The free-scan cap (40) is a constant in
`ScanQuotaRepositoryImpl` — change it and rebuild. If you later want to tune prompts without
a release, host a small `config.json` anywhere and fetch it on launch (a Phase 3.5 add).

## AdMob — Phase 7

- Create an AdMob account + app. Create **one adaptive banner** and **one interstitial** ad
  unit. Put the ids in `local.properties` (debug builds always use Google's test ids).
- AdMob → **Privacy & messaging** → create the **GDPR / UMP** consent message; set the max
  ad content rating; complete the US-state settings. Host **app-ads.txt**.

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
