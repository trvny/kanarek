---
name: kanarek
description: Work on the Kanarek Android news-widget and IPTV/radio-player app or its optional Cloudflare Worker in trvny/kanarek. Use for widget, Compose, Media3, M3U/OPML, feed parsing, Worker routes, caching, read state, discovery, scraping, deployment, or review tasks. Read the repository contract and matching reference, then verify current versions, bindings, paths and workflow commands from source.
license: MIT
---

# Kanarek

Kanarek is the standalone `trvny/kanarek` repository:

- `app/`: Kotlin/Compose Android app with news and player widgets;
- `worker/`: optional TypeScript Cloudflare Worker for feed proxying, discovery/scraping and synchronized state.

Read root `AGENTS.md`, then only the matching reference:

| Task | Reference |
|---|---|
| Android, widgets, player, codecs, Gradle | `references/android.md` |
| Worker, routes, caching, bindings, deployment | `references/worker.md` |
| Review | `references/review.md` |

Repository files, manifests, version catalogs, Wrangler configuration and workflow YAML are the source of truth. Exact dependency versions, resource IDs, default feeds, routes and CI commands must not be copied from old instructions without checking them.

## Stable invariants

- The Worker is optional. Blank backend configuration must keep on-device feed parsing functional.
- Home-screen widgets use RemoteViews-safe layouts. Player/direct-control intents are immutable; the explicit news collection template remains mutable so `setOnClickFillInIntent` can supply each article URL.
- Transient feed failure preserves the news widget's last good items.
- Widget images use the shared widget cache.
- Playback has one service-owned player; activities and widgets are clients.
- Per-stream request headers survive import, editing, persistence and playback.
- Worker source failures are isolated; one broken feed must not sink the merged response.
- Conditional requests use a stable item-set ETag and bodyless `304`; volatile fetch timestamps must not invalidate unchanged content.
- Pure feed, OPML, M3U, playlist and model codecs stay free of Android dependencies where JVM tests rely on that.
- File import/export uses the Storage Access Framework.
- Defaults duplicated intentionally by app and Worker stay synchronized.

## Working method

- Inspect current `main`, open PRs and recent changes before editing.
- Keep app and Worker changes separate unless an interface or shared default requires both.
- Do not commit credentials, Cloudflare account identifiers, binding IDs or private deployment metadata.
- Production Worker deployment is owned by Cloudflare Workers Builds from `worker/` on `main`.

## Validation

Read the active Android and Worker workflow files before claiming the full CI matrix. The Gradle wrapper is intentionally untracked; on a fresh clone, use the exact wrapper bootstrap from `.github/workflows/android-ci.yml`, which installs the version parsed from `gradle/wrapper/gradle-wrapper.properties` before running the `wrapper` task.

After that bootstrap, the current full Android CI command is:

```bash
./gradlew assemblePlayDebug assembleFossDebug testPlayDebugUnitTest lintPlayDebug --stacktrace
```

Worker checks run independently:

```bash
(
  cd worker
  npm ci
  npm run typecheck
  npm test
)
```

A local edit, connector write or successful typecheck is not proof of deployment or device behavior.

## Completion

Report the affected half, changed files, observed tests or CI, commit or PR, and anything requiring a physical Android device or live Worker verification. Keep documentation current only when durable user-facing behavior or project assumptions changed.
