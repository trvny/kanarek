# Kanarek Android app

The Android project lives at repository root with the application under `app/`. Read the current source tree rather than relying on a class list copied into this reference.

Before changing Android behavior, inspect:

- `app/src/main/AndroidManifest.xml`;
- the touched widget provider, RemoteViews service, player service, repository, codec and Compose screen;
- `app/src/main/res/layout/` and `app/src/main/res/xml/` for widget changes;
- `gradle/libs.versions.toml`, Gradle build files and `gradle.properties` for toolchain changes;
- `.github/workflows/android-ci.yml` for the current CI command.

## News and player widgets

- Use only RemoteViews-supported layout classes.
- Keep direct controls and player actions explicit and immutable. Give each player action unique identity so `FLAG_UPDATE_CURRENT` does not collapse controls.
- Keep the news collection template explicit but mutable so the launcher can merge each row's `setOnClickFillInIntent` URL into `ArticleRedirectActivity`.
- Keep the news-click trampoline that turns the fill-in intent into a safe browser launch.
- Preserve last-known-good items when a transient refresh fails or returns unusable data.
- Route widget images through the shared widget cache.
- Keep refresh work bounded and respect current battery, network and visibility constraints.

## Player ownership

- Keep one player and media session owned by the playback service.
- Activities and widgets control that service; do not create another player per screen or widget.
- Preserve foreground media playback, manifest permissions and notification behavior.
- The player widget receives pushed state rather than polling.
- Keep unstable Media3 implementation types behind the service boundary.

## Per-stream headers and playlists

User agent and referrer data must survive M3U parsing, model/persistence, station editing, playlist replacement and player request construction. Preserve stable station identity on re-import and cover supported round-trips with JVM tests.

## Pure codecs and file access

Feed, OPML, M3U, playlist and related model codecs stay pure Kotlin where existing tests depend on it. Use the Storage Access Framework for user-selected import/export; do not add broad storage permissions or assume stable filesystem paths.

## Build configuration

The version catalog, Gradle properties, wrapper/workflow configuration and module build files are the source of truth. Do not copy exact AGP, Kotlin, Gradle, SDK or Media3 versions into skills. Treat the lint baseline as accepted existing findings, not a bin for new errors.

## Localization and secrets

Keep default and Polish string resources synchronized for user-facing keys. Backend credentials and private feed configuration remain server-side.

## Validation

Use the active command from `.github/workflows/android-ci.yml`. The wrapper scripts are intentionally not tracked, so on a fresh clone first derive the current version from `gradle/wrapper/gradle-wrapper.properties` and generate them, as CI does:

```bash
GRADLE_VERSION=$(sed -n 's#^distributionUrl=.*/gradle-\([0-9][A-Za-z0-9.-]*\)-\(bin\|all\)\.zip$#\1#p' gradle/wrapper/gradle-wrapper.properties)
gradle wrapper --gradle-version "$GRADLE_VERSION" --no-daemon
./gradlew assemblePlayDebug assembleFossDebug testPlayDebugUnitTest lintPlayDebug --stacktrace
```

If the workflow changes, follow it instead. Report emulator, launcher, notification, playback or device-specific behavior as physically tested or unverified.
