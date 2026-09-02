# Structure

Kanarek is a small mixed-stack repository with two Gradle modules and one standalone Worker package.

```text
kanarek/
├── app/                       Android application
├── shared/                    Kotlin Multiplatform shared logic
├── worker/                    Optional Cloudflare Worker
├── docs/                      Maintained project documentation
├── .github/                   CI, review, lint and Dependabot configuration
├── gradle/                    Version catalog + wrapper properties
├── assets/                    Repository-level artwork
├── build.gradle.kts           Root Gradle plugin declarations
└── settings.gradle.kts        Gradle repositories and :app/:shared modules
```

## `app/`: Android shell and platform services

`app/src/main/java/com/kanarek/` contains Android-specific orchestration and UI.

```text
com/kanarek/
├── HomeActivity.kt            Single-window navigation shell
├── KanarekProcessInitializer.kt
├── data/                      Android persistence, networking and repositories
├── notifications/             Background news notifications
├── player/                    Media3 playback service and runtime metadata
├── reader/                    Background reader refresh
├── ui/                        Compose reader/player/settings/storage UI
└── widget/                    News/player App Widgets and refresh plumbing
```

Key boundaries:

- `HomeActivity` hosts `ReaderScreen` and `PlayerScreen` in a two-page `HorizontalPager` with bottom navigation and a drawer.
- `data/` in the app is for Android-aware repositories, stores, caches and integration glue. Pure feed/domain codecs belong in `shared/commonMain`.
- `player/PlayerService.kt` owns the long-lived playback engine and MediaSession.
- `widget/` uses `RemoteViews`, launcher-safe resources, explicit state stores and WorkManager refresh jobs.

### Distribution source sets

```text
app/src/play/                 Google Cast implementation and Play Services manifest bits
app/src/foss/                 GMS-free no-op Cast twins with the same callable surface
app/src/test/                 JVM/Robolectric Android tests
```

The paired `play`/`foss` Cast files intentionally expose matching APIs so `main` sources stay flavor-agnostic.

## `shared/`: portable Kotlin logic

```text
shared/src/
├── commonMain/kotlin/com/kanarek/
│   ├── data/                 Feed parser, models, M3U/OPML, merge/state helpers
│   ├── player/               Portable player failure/state logic
│   └── ui/                   Portable UI/domain state models
├── androidMain/...           Android platform implementation(s)
├── iosMain/...               iOS platform implementation(s)
└── commonTest/...            Portable unit tests
```

Important shared data code includes `FeedParser`, `NewsItem`, `NewsMerge`, `Headlines`, `M3uCodec`, `Opml`, `Station`, `ReaderFeedSnapshot`, `NewsNotificationConfig`, `Favicons` and URL-safety helpers.

`FeedPlatform.kt` has Android and iOS implementations where platform I/O is unavoidable.

## `worker/`: optional backend

```text
worker/
├── src/
│   ├── index.ts              Routes, feed aggregation and integrations
│   └── article.ts            Clean-reader extraction helpers
├── test/                     Node/Vitest helper tests
├── migrations/              D1 schema migrations
├── package.json              npm dependencies and commands
├── vitest.config.ts          Plain-Node test configuration
├── tsconfig.json             TypeScript configuration
└── wrangler.jsonc            Worker deployment variables and bindings
```

The Worker is not required for ordinary RSS/Atom reading. App code must preserve the on-device fallback when its backend URL is empty or unavailable.

## `docs/`: maintained knowledge

- `ARCHITECTURE.md`: runtime boundaries and data flows.
- `STACK.md`: technologies and their source-of-truth files.
- `STRUCTURE.md`: repository/file ownership map.
- `CONVENTIONS.md`: local implementation rules.
- `INTEGRATIONS.md`: external systems and contracts.
- `TESTING.md`: test layers, commands and CI coverage.
- `CONCERNS.md`: known maintenance risks and unresolved product decisions.
- `DEVELOPMENT.md`, `WORKER.md`, `FDROID.md`, `HISTORY.md`, `DEPENDABOT_TRIAGE.md`: operational/project-specific references.

## `.github/`: automation

Notable workflows:

- `android-ci.yml`: both Android flavors, app tests/lint, shared Android host tests and Gradle dependency graph.
- `kmp-ios-ci.yml`: shared iOS simulator tests on macOS.
- `worker-ci.yml`: npm install, TypeScript typecheck and Vitest.
- `release.yml`: rolling Android release artifacts.
- `mega-linter.yml`: repository linting.
- `kanarek-review.yml`: repository review automation.

## Where new code should go

1. Put platform-independent models/codecs/state machines in `shared/commonMain`.
2. Put Android UI, services, launcher widgets, DataStore and WorkManager glue in `app/src/main`.
3. Put platform-specific implementations of shared contracts in `shared/androidMain` or `shared/iosMain`.
4. Put Google Cast-only code in `app/src/play` and maintain matching GMS-free surfaces in `app/src/foss`.
5. Put backend-only edge logic in `worker/`; never make it mandatory for basic feed reading.
