# Testing

Kanarek uses three main test layers: portable Kotlin tests, Android JVM/Robolectric tests, and Worker TypeScript tests. CI also compiles the shared module for iOS to keep the multiplatform boundary honest.

## Local commands

### Android + shared Android host tests

From the repository root after generating/obtaining the expected Gradle wrapper:

```bash
./gradlew assemblePlayDebug assembleFossDebug \
  testPlayDebugUnitTest :shared:testAndroidHostTest lintPlayDebug
```

For a narrower app-only run:

```bash
./gradlew testPlayDebugUnitTest
```

### Shared iOS tests

On macOS with the required Apple toolchain:

```bash
./gradlew :shared:iosSimulatorArm64Test
```

### Worker

```bash
cd worker
npm ci
npm run typecheck
npm test
```

## `shared/commonTest`

Portable tests cover logic that should not require Android or a networked backend. Current examples include:

- RSS/Atom parsing and normalization,
- headline handling,
- M3U/M3U8 parsing/serialization,
- OPML round-trips,
- news merging and notification configuration,
- favicon/URL helpers,
- reader snapshots/background-refresh logic,
- player failure-state transitions,
- portable reader/player UI state.

This is the preferred test home for new pure Kotlin behavior.

## `app/src/test`

Android JVM tests cover Android-facing orchestration and regressions without requiring an emulator.

Notable areas include:

- app-side article/read-state/cache logic,
- portable backup and runtime reconciliation,
- notification selection/state,
- player metadata/failure/restoration behavior,
- widget state and refresh coordination,
- widget size classes and player-widget state,
- `RemoteViews` inflation/application under Robolectric.

Robolectric has Android resources enabled so widget tests can apply real layouts/manifests. This catches invalid launcher view classes, missing resources and PendingIntent/layout wiring before testing on a physical launcher.

The current repository tree has no `app/src/androidTest` instrumentation suite; device-specific behavior is therefore primarily covered by compile/build checks, Robolectric and manual/runtime validation.

## Worker tests

`worker/vitest.config.ts` intentionally uses the plain Node environment. Current tests target exported pure helpers and cover areas such as:

- feed parsing/normalization,
- output formats and ETag-related helpers,
- article extraction helpers,
- station/logo mapping,
- discovery/scrape hardening,
- state-related helpers.

This keeps tests fast but does not simulate the full Cloudflare Workers runtime, bindings or Cache API. Runtime-sensitive Worker changes should therefore receive extra scrutiny or a Workers-runtime test when they cannot be reduced to a pure helper.

## CI workflows

### Android CI

`.github/workflows/android-ci.yml` runs on relevant app/shared changes and:

1. sets up JDK 17,
2. reads the required Gradle version from wrapper properties,
3. regenerates wrapper files if absent,
4. assembles both play and foss debug variants,
5. runs play unit tests,
6. runs shared Android host tests,
7. runs Android lint,
8. uploads debug APKs.

Building both flavors is a deliberate test: it catches accidental proprietary GMS references leaking into the foss path.

The workflow separates pull-request dependency-graph generation from trusted `main` submission so untrusted PR build logic never receives a write-capable token.

### KMP iOS CI

`.github/workflows/kmp-ios-ci.yml` runs on macOS for shared/build changes and executes `:shared:iosSimulatorArm64Test`.

This verifies that common code remains compilable/testable for iOS even though the repository currently contains no standalone iOS application target.

### Worker CI

`.github/workflows/worker-ci.yml` runs with Node on Worker changes and performs:

```text
npm ci -> TypeScript typecheck -> Vitest
```

It intentionally does not deploy. Production deployment belongs to Cloudflare Workers Builds.

## What to test when changing an area

| Change | Minimum relevant validation |
|---|---|
| `shared/commonMain` parser/model/state | `commonTest`, Android host test, iOS simulator test |
| Android Compose/data/service code | play unit tests + both flavor builds; add targeted tests |
| Widget layout/state/action | Robolectric widget tests + both flavor builds |
| Cast code | play build/tests plus foss build to prove isolation |
| Worker pure parser/helper | typecheck + Vitest |
| Worker runtime/binding/cache behavior | typecheck + Vitest plus runtime-oriented validation if pure tests cannot cover it |
| Gradle/dependency update | relevant full CI matrix and dependency graph |

## Test design rules

- Prefer deterministic pure tests over network-dependent tests.
- Keep external feeds/directories mocked or represented by fixtures when testing parsing/mapping logic.
- Test failure isolation and empty/boundary inputs, not just happy paths.
- Add regression coverage beside the layer where the bug actually lived.
- Do not weaken tests/lint merely to satisfy a dependency bump; update code/config deliberately.
