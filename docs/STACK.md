# Stack

This document maps the technologies that are actually present in Kanarek. Exact dependency pins are intentionally not copied here: the maintained version sources are `gradle/libs.versions.toml`, `gradle/wrapper/gradle-wrapper.properties`, module build files, and `worker/package.json`.

## Runtime surfaces

| Surface | Primary stack | Responsibility |
|---|---|---|
| Android app (`app/`) | Kotlin, Jetpack Compose Material 3, Android App Widgets | Reader, radio/IPTV player, settings, notifications, widgets |
| Shared module (`shared/`) | Kotlin Multiplatform | Platform-independent feed/domain codecs, state logic and UI models |
| Worker (`worker/`) | TypeScript, Cloudflare Workers | Optional feed proxy/merge, discovery, scraping, clean-reader extraction, station/logo lookup and synchronized state |

## Android

- **UI:** Jetpack Compose Material 3 inside a single `HomeActivity`; launcher widgets use classic `RemoteViews` because App Widgets cannot render arbitrary Compose UI.
- **Playback:** AndroidX Media3/ExoPlayer with HLS and DASH modules. `PlayerService` is a `MediaSessionService` and owns the playback engine and media session.
- **Persistence:** AndroidX DataStore for preferences and app state, plus small file-backed caches/stores where appropriate.
- **Background work:** WorkManager for reader refresh, news notifications and widget refresh.
- **Images:** Coil in the app UI, including SVG support; launcher widgets use their own bounded disk image cache instead of Coil in the launcher process.
- **Storage interchange:** Android Storage Access Framework for portable OPML/M3U import/export rather than broad storage permissions.
- **Distribution:** `play` and `foss` product flavors. The play flavor contains Google Cast dependencies; the foss flavor stays GMS-free for F-Droid.
- **Toolchain:** JDK/JVM 17. Android SDK levels and application versions live in `app/build.gradle.kts`.

## Kotlin Multiplatform

`shared/` targets Android plus iOS device/simulator architectures.

- `commonMain`: pure Kotlin logic that must not depend on Android or iOS APIs.
- `androidMain` / `iosMain`: platform implementations where a shared contract needs platform I/O.
- `commonTest`: platform-independent tests for parsers, codecs, state transitions and UI/domain models.

This source-set split matches current Kotlin Multiplatform guidance: shared logic belongs in `commonMain`, while platform APIs stay in the corresponding platform source sets.

## Cloudflare Worker

- **Language/runtime:** TypeScript on Cloudflare Workers.
- **Feed parsing:** `feedsmith`.
- **Edge/cache:** Workers Cache API, with optional KV as a durable discovery/scrape cache.
- **State:** optional D1 binding for synchronized read state, subscriptions and pairing.
- **HTML processing:** Cloudflare `HTMLRewriter` for discovery/scraping and article extraction paths.
- **Tooling:** Wrangler, TypeScript typecheck and Vitest.
- **Tests:** current Worker unit tests deliberately run in plain Node because they target exported pure helpers rather than a simulated Workers runtime.

## Build, CI and delivery

- **Gradle:** wrapper version is declared in `gradle/wrapper/gradle-wrapper.properties`; wrapper scripts/JAR are generated rather than committed.
- **Dependency catalog:** `gradle/libs.versions.toml` is the Android/KMP dependency source of truth.
- **GitHub Actions:** Android/KMP builds and tests, Worker checks, linting, release packaging and dependency graph submission.
- **Cloudflare Workers Builds:** owns production Worker deployment from `worker/` changes on `main`; GitHub Actions checks the Worker but intentionally does not deploy it a second time.
- **Release artifacts:** play and foss APKs; release signing is optional at Gradle configuration time so downstream F-Droid builds remain possible.

## Source-of-truth rule

Do not copy exact Kotlin, AGP, Gradle, SDK, Media3 or Worker dependency versions into architecture prose. When a version matters, read it from the maintained build/configuration file above.
