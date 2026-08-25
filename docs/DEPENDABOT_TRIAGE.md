# Dependabot alert triage: build-tooling-only, app not affected

> First resolved 2026-08-12; **re-verified 2026-08-20** against the current toolchain
> (AGP 9.3.1, Kotlin 2.4.10, Gradle 9.7.0) with the same conclusion. Exact package and tool
> versions below record what was actually resolved; they are evidence, not current build
> requirements. For current Gradle/AGP/Kotlin versions and wrapper bootstrap, use the repository
> configuration and `.github/workflows/android-ci.yml`.
>
> The original run happened while the app still lived in `trvny/feeds/kanarek/`. Paths below may
> retain the `kanarek/` prefix from that layout; in this repository drop it
> (`settings.gradle.kts`, `app/build.gradle.kts`). The conclusion is unaffected because it
> concerns the dependency graph, not the directory names.

**47 open** Dependabot `maven` alerts (45 on 2026-08-12; two added since — one more
`io.netty:netty-codec-http` and `org.jetbrains.kotlin:kotlin-gradle-plugin`) are attributed to
`settings.gradle.kts`, the only file that touches Maven dependency resolution (its
`pluginManagement`/`dependencyResolutionManagement` blocks). None of the flagged
packages are declared anywhere in `app/build.gradle.kts`; they all arrive
transitively through the Android Gradle Plugin's / Kotlin Gradle plugin's own tooling.
**None of them reach `playReleaseRuntimeClasspath` or `fossReleaseRuntimeClasspath` — the
configurations that back what actually ships in an APK — so the shipped app is not affected.**
`dependency.scope` is `null` on every alert (GitHub doesn't know build-time vs
runtime for this graph), which is why this needed manual resolution rather than a
glance at the alert list.

### Where the alerts live after the repo split

The alerts are filed on **`trvny/feeds`** against `kanarek/settings.gradle.kts`. That path was
**removed from `trvny/feeds`** when this project split out to its own repository
(`trvny/kanarek`, 2026-08-14), so the alerts now point at a manifest that no longer exists in the
repo they are attached to. `trvny/kanarek` itself has **0 open Dependabot alerts**, and since
2026-08-22 that zero means something: the `dependency-graph-submit` job in `android-ci.yml`
submits the Gradle graph on every push to `main` (#36). Before it existed the graph held no JVM
entry at all, so the silence was an absence of data rather than an absence of findings. The
dismissal decision belongs to whoever owns the `trvny/feeds` security tab; this document is the
evidence for it.

## Method

The 2026-08-12 run used the then-current repository toolchain. To reproduce the analysis today,
bootstrap the wrapper exactly as `.github/workflows/android-ci.yml` does from
`gradle/wrapper/gradle-wrapper.properties`, then run:

- `./gradlew buildEnvironment` — resolves the root build's `classpath` configuration,
  i.e. the AGP + Kotlin Gradle plugin dependency graph declared in
  `pluginManagement`/`plugins {}`. This is exactly the graph Dependabot attributes to
  `settings.gradle.kts`.
- `./gradlew :app:dependencies` (all ~40 configurations, unabridged) — resolves every
  configuration of the `app` module, including the release variants that actually
  ship (`playReleaseRuntimeClasspath`, `fossReleaseRuntimeClasspath`), the debug
  variants, the JVM unit-test classpaths (Robolectric), and the Unified Test
  Platform (UTP) configurations Gradle creates to drive `connectedAndroidTest`.

Both were run locally for this triage, not by CI — `android-ci.yml`
only runs its active build/test/lint command and does not invoke either dependency-report task,
and `--stacktrace` prints exception stacktraces, not dependency trees, so no report is currently
archived anywhere.

The 2026-08-20 re-verification confirmed the two dependency-report tasks resolve **without an
Android SDK installed** (dependency resolution needs the plugin and library metadata, not the
SDK — only the actual compile/dex/assemble tasks need it). Bootstrapping Gradle 9.7.0 and running
`buildEnvironment`, `:app:dependencies --configuration <variant>RuntimeClasspath` for all four
`{play,foss}{Release,Debug}RuntimeClasspath` variants, `:app:dependencies` (all configurations),
and `:shared:dependencies` reproduced every finding below on the current toolchain.

## Findings by package family

| Package(s) | Resolved version(s) seen | Configuration(s) that pull it in | Ships in APK? |
|---|---|---|---|
| `org.bouncycastle:bcprov-jdk18on`, `bcpkix-jdk18on` | `1.79` (via AGP's own `builder`/`apkzlib`/`signflinger` tooling and lint); `1.81` (via Robolectric's JVM unit-test graph) | `androidLintTool`, `unified-test-platform-android-test-plugin-result-listener-gradle`, `fossDebugUnitTestRuntimeClasspath`, `playDebugUnitTestRuntimeClasspath` | No |
| `org.apache.httpcomponents:httpclient` | `4.5.6` (lint/UTP tool classpath, unresolved by the app's own conflict resolution); `4.5.6 -> 4.5.14` in the AGP plugin classpath itself | `androidLintTool`, `unified-test-platform-android-test-plugin-result-listener-gradle` | No |
| `org.apache.commons:commons-lang3` | `3.16.0` | `androidLintTool`, `unified-test-platform-android-test-plugin-result-listener-gradle` | No |
| `io.netty:netty-codec`, `netty-codec-http`, `netty-codec-http2`, `netty-common`, `netty-handler`, `netty-handler-proxy` | `4.1.110.Final` (2026-08-20 resolution; a second UTP sub-configuration also pins `4.1.93.Final`) via `io.grpc:grpc-netty:1.69.1` | `unified-test-platform-core`, `unified-test-platform-android-test-plugin-host-emulator-control` (both are Unified Test Platform's own gRPC transport, used only to talk to a local device/emulator while running `connectedAndroidTest`) | No |
| `org.jdom:jdom2` | `2.0.6` | AGP's own `jetifier-processor` (root buildscript `classpath`, not any `:app` configuration) | No |
| `org.bitbucket.b_c:jose4j` | `0.9.5` | AGP's own `bundletool` (root buildscript `classpath`, not any `:app` configuration) | No |
| `org.jetbrains.kotlin:kotlin-gradle-plugin` | `2.4.10` | The Kotlin Gradle plugin itself — the root buildscript `classpath` resolved by `buildEnvironment`. By definition a build-time plugin; it never contributes to any `:app` runtime configuration. | No |

Every one of these configurations is either:

1. **The root buildscript/plugin classpath** (`classpath`, resolved by
   `buildEnvironment`) — the JVM classpath used to *run* AGP and the Kotlin Gradle
   plugin inside the Gradle daemon. It never touches the `app` module's compiled
   output.
2. **A lint or Unified Test Platform tool classpath** (`androidLintTool`,
   `unified-test-platform-*`) — separate JVM processes Gradle spawns to run
   `lint`/`connectedAndroidTest`. These run on the build machine (or against an
   emulator/device over gRPC for UTP), never inside the app process, and are not
   packaged into any APK, debug or release.
3. **A JVM unit-test runtime classpath** (`*DebugUnitTestRuntimeClasspath`, i.e.
   Robolectric) — runs on the JVM under `testPlayDebugUnitTest`/
   `testFossDebugUnitTest`, produces no APK output at all.

`playReleaseRuntimeClasspath` and `fossReleaseRuntimeClasspath` — dumped in full —
contain **zero** matches for any of the package families above. Same for the
debug variants (`playDebugRuntimeClasspath`, `fossDebugRuntimeClasspath`), so even
a debug APK installed for manual testing doesn't carry this code.

## Why this isn't a false alarm to ignore blindly

The versions Dependabot flagged genuinely are vulnerable per the advisories (e.g.
`bcprov-jdk18on:1.79` is inside the `>= 1.74, < 1.84` range for CVE-2026-0636;
`netty-codec-http2:4.1.93.Final` predates essentially every listed patch). The
resolution isn't "the version is fine" — it's "this code never executes as part of
the app a user installs." A tool-classpath CVE would matter if it were remotely
exploitable *during the build itself* (e.g. malicious build inputs reaching a
vulnerable AGP-internal HTTP client), which is a different threat model than "ships
in the APK" and is out of scope for a single-maintainer local/CI build.

## What would change this

- If `app/build.gradle.kts` ever adds a direct or transitive dependency on any of
  these groups via `implementation`/`api` (or any other configuration that flows
  into the release variants), re-run this triage — that would put the code on
  `*ReleaseRuntimeClasspath`. A `debugImplementation`-only addition would land on
  `*DebugRuntimeClasspath` instead and not affect the release-APK conclusion, but
  would still be worth re-checking since a debug build can end up on a device too.
- If AGP bumps its own bundled versions of these libraries (a new AGP release), the
  flagged versions here go stale automatically; re-resolve after any AGP bump.
- Submission now happens here, and it deliberately narrows what it reports:
  `.github/actions/gradle-dependency-graph` passes
  `dependency-graph-include-configurations: ^[a-z]*[Rr]eleaseRuntimeClasspath$`, so
  build-time tooling stays out of the graph rather than arriving with
  `dependency.scope: null`. If that filter is widened, expect build-time findings to
  reappear as alerts — and expect the hand resolution below to be needed again,
  because the `trvny/feeds` attribution that made this triage necessary collapsed
  everything to `settings.gradle.kts` with no scope at all.

## Proposed alert disposition

Dismissal is a maintainer action, not something this triage performs — the
scheduled task that produced this PR was explicitly told not to dismiss, close, or
otherwise mutate alerts; only the repository owner can click dismiss.

"Not affected" here specifically means *not shipped to an end user's device* —
confirmed by full dependency graph resolution showing zero matches on
`playReleaseRuntimeClasspath`/`fossReleaseRuntimeClasspath` on 2026-08-12. It is
**not** a claim that these libraries never execute at all: AGP's own tooling, lint,
UTP, and Robolectric genuinely run these vulnerable versions on the build machine
(and, for UTP, talk to a local emulator/device over gRPC). Whether that residual
build-time exposure is acceptable is a call for the maintainer, not this triage —
the CVEs here are mostly about untrusted network input reaching Netty/BouncyCastle
codecs, which is a plausible concern only if something in this pipeline feeds
attacker-controlled data into AGP's build-time HTTP/TLS/codec paths (this repo's CI
doesn't). With that caveat, GitHub's Dependabot dismissal reasons closest to this
finding is **"risk is not relevant"**, judged per advisory against this build
environment. **"Vulnerable code is not in use"** fits worse than it first looks:
this code does execute, just not on a user's device.

The table above groups by package family and establishes APK reachability only. It
is **not** a per-advisory build-time analysis, so it does not on its own justify
dismissing all 47 as a block — that call is the maintainer's, advisory by advisory.
