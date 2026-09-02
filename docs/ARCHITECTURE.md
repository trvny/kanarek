# Kanarek architecture

Kanarek is one Android application with two main user surfaces and an optional edge backend:

- a news reader and resizable news slideshow widget,
- a background radio/IPTV player and transport-control widget,
- a Cloudflare Worker that accelerates and extends feed/integration work without becoming mandatory for ordinary reading.

The repository also contains a Kotlin Multiplatform `shared` module. Portable parsing, models and state logic live there so Android is not the only viable consumer of the core domain code.

## Component map

| Component | Responsibility |
|---|---|
| `app/` | Android UI, services, persistence, WorkManager jobs and App Widgets |
| `shared/` | Platform-independent domain/parser/state logic plus small platform implementations |
| `worker/` | Optional edge feed processing, discovery/scraping, article extraction, directory/logo lookup and synchronized state |

```text
                         ┌────────────────────────────┐
                         │ shared/commonMain          │
                         │ models, parsers, codecs,   │
                         │ merge/state/UI logic       │
                         └─────────────┬──────────────┘
                                       │
                                       v
HomeActivity: ReaderScreen <----> PlayerScreen
      │                                │
      v                                v
NewsRepository                    PlayerService
      │                           ExoPlayer + MediaSession
      │                                │
      │                                ├── system media controls
      │                                ├── PlayerWidgetProvider
      │                                └── play flavor: CastPlayer
      │
      ├── backend configured ──> Cloudflare Worker
      │                         merge/cache/discover/scrape/state
      │
      └── backend absent/fails ─> on-device FeedParser

NewsRepository / persisted reader state
      │
      └─────────────────────────> KanarekWidgetProvider
```

## Android application shell

`HomeActivity` is the app's single window. `ReaderScreen` and `PlayerScreen` are pages of one `HorizontalPager`, reachable by swipe, bottom navigation or drawer.

The pager keeps its neighbouring page alive. This deliberately preserves the reader's loaded state and the player's service binding when the user switches between the two main surfaces.

App-level Android code owns lifecycle-sensitive concerns such as DataStore, WorkManager, notifications, services, Storage Access Framework integration and launcher widgets. Portable parsing/state logic should not be reintroduced there when it can live in `shared`.

## Reader data flow

`NewsRepository` has two fetch paths:

1. **Worker path:** when a backend URL is configured, it asks the Worker for normalized merged items. It uses ETags and `FeedCache`; a `304 Not Modified` reuses the cached body.
2. **On-device path:** when the backend is blank or the Worker request fails, feeds are downloaded concurrently and parsed by the shared pure-Kotlin `FeedParser`.

A failing feed is isolated from the rest of the batch. Unsafe non-HTTP(S) article links are discarded, results are deduplicated/capped, and a failed refresh does not intentionally erase last-known-good widget data.

### Backend remains optional

The Worker is an optimization and feature extension, not a basic-read dependency. Changes must preserve regular RSS/Atom operation with an empty backend configuration.

## Shared Kotlin boundary

`shared/commonMain` is the source of truth for platform-independent models, parsers, codecs and state transformations, including feed parsing, headline logic, M3U/OPML handling, news merge/configuration, stations and portable UI state.

`shared/androidMain` and `shared/iosMain` contain the platform implementations needed by shared contracts. Shared common tests exercise the portable logic without an Android emulator.

This boundary is intentionally enforced in CI on both Android host and iOS simulator targets.

## Playback architecture

`PlayerService` is a `MediaSessionService` that owns one local ExoPlayer and one MediaSession for the app process. Playback therefore survives Activity navigation and recomposition and remains visible to Android media controls.

The Activity binds directly to the same-process service through a local Binder. Launcher widgets cannot keep such a binder, so they send service actions and receive pushed playback snapshots.

The player supports:

- HLS and DASH through matching Media3 modules,
- per-stream `User-Agent` and `Referer` headers through a resolving data source,
- ICY/media metadata for now-playing text,
- retry/failure state that is surfaced to the UI,
- a video surface for TV streams while keeping radio audio-only,
- Google Cast in the `play` flavor.

The `foss` flavor supplies matching no-op Cast glue so `main` remains GMS-free and flavor-agnostic.

## App Widgets

Both launcher widgets use Android `RemoteViews`, not Compose. Layouts therefore contain only supported launcher-safe view classes.

Widget image loading uses the shared on-disk `WidgetImageCache`; it does not rely on Coil inside the launcher process. Robolectric tests inflate/apply widget `RemoteViews` to catch unsupported views, resource failures and PendingIntent wiring before a launcher reports an opaque add-widget failure.

## Worker architecture

The Cloudflare Worker exposes several independent capabilities:

- feed merge/normalization and Atom/RSS/JSON Feed export,
- feed discovery,
- HTML-to-Atom scraping,
- clean article extraction behind an exact host allowlist,
- Radio Browser station search,
- iptv-org logo lookup,
- optional D1-backed read/subscription/pairing state,
- health checks.

The fast cache is Cloudflare Cache API. Optional KV provides a more durable cross-location cache for discovery/scrape results. Optional D1 stores write-heavy synchronized state. Missing optional bindings disable only their dependent endpoints rather than the whole Worker.

Production Worker deployment is owned by Cloudflare Workers Builds. GitHub Actions typecheck/test the package but intentionally do not duplicate deployment.

## Failure isolation and bounded I/O

External feeds, sites and streams are untrusted and unreliable. Network reads use explicit timeouts/byte limits, URL handling is constrained to HTTP(S), clean-reader redirects remain allowlisted, and one failed source should not sink unrelated sources.

## Configuration and source of truth

- Android/KMP dependency versions: `gradle/libs.versions.toml`.
- Gradle distribution: `gradle/wrapper/gradle-wrapper.properties`.
- SDK/application/release settings: module build files.
- Worker dependencies: `worker/package.json` / lockfile.
- Worker variables/bindings: `worker/wrangler.jsonc`.

The app's default feed list and the Worker's `DEFAULT_FEEDS` are currently a deliberately duplicated runtime contract and must stay synchronized.

Do not copy exact tool/library versions into architecture documentation.

## Further reading

- [Stack](STACK.md)
- [Structure](STRUCTURE.md)
- [Conventions](CONVENTIONS.md)
- [Integrations](INTEGRATIONS.md)
- [Testing](TESTING.md)
- [Concerns](CONCERNS.md)
- [Development](DEVELOPMENT.md)
- [Worker and API](WORKER.md)
- [Dependabot alert triage](DEPENDABOT_TRIAGE.md)
- [Project history](HISTORY.md)
