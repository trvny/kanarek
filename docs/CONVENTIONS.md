# Conventions

These are repository-specific implementation rules inferred from the maintained code, configuration and project instructions. Preserve them unless a task explicitly changes the architecture.

## General working rules

- Check `main`, open pull requests and recent changes before starting overlapping work.
- Prefer extending an existing structure over creating a parallel one.
- Keep one maintained source of truth per concern.
- Keep one logical change per pull request.
- Do not commit credentials, tokens or private deployment metadata. Public Cloudflare resource identifiers required for reproducible deployment are intentionally allowed in `worker/wrangler.jsonc`.
- Treat generated/build output and MegaLinter `updated_sources` as disposable suggestions, not maintained source.

## Kotlin and Compose

`.editorconfig` defines the local formatting baseline:

- 4-space indentation for Kotlin/KTS,
- 140-character maximum line length,
- final newline and trimmed trailing whitespace,
- Compose `@Composable` functions may use PascalCase; ktlint's generic function-naming rule is disabled to avoid false positives.

Prefer descriptive, focused functions and existing state/data abstractions over adding another global store or duplicate model.

## Kotlin Multiplatform boundary

Use `shared/commonMain` when code:

- is platform-independent,
- models domain state,
- parses/serializes portable formats,
- transforms/merges data,
- can be tested without Android APIs.

Use `shared/androidMain` or `shared/iosMain` for platform implementations needed by shared contracts.

Use `app/main` for Android lifecycle, Compose UI, DataStore, WorkManager, services, notifications, launcher widgets and other Android APIs.

Do not move portable logic back into `app` merely because Android is currently the only shipping UI. The shared module is compiled/tested for both Android and iOS.

## Android application

- `HomeActivity` remains the single application window and navigation shell unless there is a strong reason to change that model.
- Reader and player are sibling pages, not separate application stacks.
- `PlayerService` owns playback. UI and widgets control that service rather than creating independent players.
- Long-running/background refresh belongs in WorkManager or the existing service/worker mechanism, not an Activity coroutine that dies with the screen.
- Settings and persisted Android state should use the existing stores rather than introducing a second persistence framework. The project intentionally avoids Room and Hilt.
- Use the Storage Access Framework for user-selected import/export files; do not request broad storage access.

## App Widgets

Launcher widgets are a constrained subsystem:

- use only `RemoteViews`-supported view classes,
- keep PendingIntents immutable unless Android semantics explicitly require otherwise,
- use the existing activity/service receiver paths for actions and deep links,
- use `WidgetImageCache` rather than Coil from the launcher process,
- preserve last-known-good content when transient refreshes fail,
- add/update Robolectric inflation tests when widget layouts or PendingIntent wiring change.

## Play and foss flavors

Google Cast is isolated to `app/src/play`.

- Any Cast-facing class used from `main` must have a compatible GMS-free twin in `app/src/foss`.
- Proprietary Play Services dependencies must never leak into `main` or the foss dependency graph.
- Build both flavors when touching shared Android code so flavor leakage fails early.

## Media playback

- Keep one playback engine/session owner in `PlayerService`.
- Add stream-format support through the corresponding Media3 module and keep Media3 module versions aligned through the version catalog.
- Preserve per-stream request-header support when changing media source construction.
- Treat stream failure as state to surface/recover from, not as a reason to tear down unrelated UI state.

## Reader/networking

- The Worker must stay optional for ordinary RSS/Atom feeds.
- Backend failure must fall back to on-device parsing where the current contract allows it.
- One broken source must not sink the full source set.
- Bound external reads by timeout/size and reject unsafe URL schemes.
- Keep ETag/304 behavior and last-known-good caching intact when changing backend fetches.

## Worker

- Keep route-specific failure isolated. Optional KV/D1 bindings should disable only dependent features.
- Keep source URL handling bounded and HTTP(S)-only.
- Clean-reader extraction remains behind an exact operator-controlled host allowlist, including redirects.
- Prefer pure helpers for parsing/serialization/security-sensitive transformations so they remain easy to unit-test.
- Production deployment belongs to Cloudflare Workers Builds; GitHub Actions should not add a second competing deploy path.

## Configuration

Use these maintained sources rather than copying values into prose/code:

- versions: `gradle/libs.versions.toml`, Gradle wrapper properties, build files, `worker/package.json`,
- Worker bindings/vars: `worker/wrangler.jsonc`,
- application version: `app/build.gradle.kts`.

`worker/wrangler.jsonc` is also the source of truth for default feeds. After changing `vars.DEFAULT_FEEDS`, run `node .github/scripts/sync-default-feeds.mjs`; it rewrites `NewsRepository.DEFAULT_FEEDS`. Existing Worker CI checks the generated block and also runs when the Android-side block or sync script changes.

## Tests and documentation

- New portable logic should normally receive `commonTest` coverage.
- New Android state/widget behavior should receive JVM/Robolectric coverage where feasible.
- New Worker parsing/security behavior should receive Vitest coverage.
- Update architecture/structure docs when moving ownership between `app`, `shared` and `worker`; stale file maps are worse than no file maps.
