# Integrations

Kanarek deliberately keeps most integrations behind small boundaries so basic reading/playback can continue when an optional service is unavailable.

## Cloudflare Worker

Fresh installs leave the reader backend URL empty, so ordinary feeds are fetched and parsed on-device by default. The deployed Worker at `https://kanarek.travny.workers.dev` is the built-in fallback only for Worker-dependent integrations such as discovery, station search and logo lookup; reader feed aggregation and clean article extraction use it only when a backend URL is explicitly configured.

The Worker provides:

- merged/normalized feed responses and Atom/RSS/JSON Feed export,
- feed discovery and HTML-to-Atom scraping,
- clean article extraction,
- station directory and channel-logo lookup,
- optional synchronized read/subscription/pairing state,
- health checks.

### Cloudflare resources

- **Cache API:** fast edge caching for feed/discovery work.
- **KV (`SCRAPE_KV`):** optional durable cross-location discovery/scrape cache.
- **D1 (`STATE_DB`):** optional synchronized read state, subscriptions and pairing.
- **Workers Builds:** production deployment from `worker/` changes on `main`.

Worker resource names/IDs and public deployment configuration live in `worker/wrangler.jsonc`. Secret material must not be committed.

## Feed sources

The default reader configuration currently includes a mixture of direct publishers/services and generated feeds, including:

- Google News Poland,
- Euronews Poland,
- Antyweb,
- generated feeds hosted from the `feedseek` project.

`worker/wrangler.jsonc` is the maintained source of truth for default feeds. `.github/scripts/sync-default-feeds.mjs` renders `vars.DEFAULT_FEEDS` into `NewsRepository.DEFAULT_FEEDS`, and existing Worker CI rejects drift between the two representations.

User-added RSS/Atom sources do not require the Worker. They can be fetched and parsed on-device.

## Feed discovery and article extraction

`/discover` inspects pages for advertised feeds and common fallback feed paths.

`/scrape` converts suitable HTML listings to Atom, optionally using caller-provided selectors. The resulting URL can then behave like any other feed in Kanarek.

`/article` performs clean-reader extraction only for exact hosts allowed by `ARTICLE_ALLOWED_HOSTS`. An empty allowlist disables that endpoint. Redirects must remain within the allowlist.

## Radio Browser

The Worker exposes a station search proxy backed by the community Radio Browser directory. App-side `StationDirectory` owns the Android-facing integration and maps results into Kanarek's station model.

External directory availability and station health are not under Kanarek's control, so failures must remain isolated from locally stored stations and unrelated playback.

## iptv-org

Missing TV/radio logos can be resolved through the iptv-org channel catalog. Existing playlist-provided logos take precedence; lookup is a fallback rather than a canonical override.

## Google Cast

Google Cast support exists only in the `play` flavor:

- Media3 Cast bridges remote playback onto the Player abstraction,
- Android MediaRouter provides route/device discovery UI,
- Google Play Services Cast Framework provides Cast sessions.

The default Cast receiver fetches streams itself, so Kanarek's local per-stream `User-Agent` and `Referer` overrides do not apply while casting. Streams that depend on those headers may work locally but fail on the Cast target.

The `foss` flavor has compatible no-op Cast implementations and no proprietary GMS dependency.

## Android media system

`PlayerService` publishes a MediaSession, integrating playback with Android media notifications, lock-screen/system controls and external media controllers.

The app Activity uses a local Binder for same-process control. Launcher widgets use explicit service actions instead of a long-lived controller/binder.

## Android App Widgets

Kanarek exposes two launcher integrations:

- news slideshow widget,
- radio/TV transport-control widget.

They communicate through `RemoteViews`, receivers/services, persisted widget state and WorkManager refreshes. Launcher capabilities vary, so the code includes fallbacks for behavior such as slideshow auto-advance.

## Storage Access Framework

OPML and M3U/M3U8 import/export use Android's Storage Access Framework. Users choose the target/source document through the system picker, avoiding broad filesystem permissions.

## F-Droid

The `foss` product flavor is the F-Droid-compatible build surface:

- no Google Cast/Play Services dependency,
- release signing may be absent locally so F-Droid can sign downstream,
- metadata and build notes live under `app/fastlane/metadata/` and `docs/FDROID.md`.

## GitHub platform services

- GitHub Actions builds/tests Android/KMP/Worker code and rolling Android artifacts.
- CodeQL default setup provides code scanning.
- Dependabot manages Gradle/npm/GitHub Actions dependency updates.
- The Android CI explicitly submits the Gradle dependency graph on trusted `main` pushes so Dependabot can see JVM dependencies.

## Integration rule

When adding another provider or service, keep the provider-specific adapter at the edge and map data into the existing Kanarek model. Do not create a second source of truth for stations, articles, reader state or playback state merely to accommodate one integration.
