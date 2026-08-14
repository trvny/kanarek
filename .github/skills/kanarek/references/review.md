# Review Kanarek

Review touched app and Worker areas together when a shared contract/default crosses the boundary. Read the actual diff, nearby implementation, root `AGENTS.md` and the relevant Android or Worker reference.

Lead with findings that can crash a widget, break playback, lose user data, expose secrets or break the app/Worker contract. Do not manufacture style findings to fill a template.

## Review checklist

### Widgets

- RemoteViews-safe layouts only.
- Direct controls/player actions explicit, immutable and uniquely identifiable.
- News collection template remains mutable for row fill-in intents.
- Last-known-good news survives transient refresh failures.
- Widget images use the shared cache and refresh remains bounded.

### Playback

- One service owns player/media session.
- Activities/widgets control that service rather than creating another player.
- Foreground service, notification and media permissions remain coherent.
- Per-stream user-agent/referrer survives import, edit, persistence and playback.

### Feed and cache behavior

- Worker sources fail independently.
- Stable ETags exclude volatile fetch timestamps and support bodyless `304`.
- Discovery/scraping remains bounded and host-restricted.
- Optional Worker/D1 failure does not disable on-device parsing.
- Intentionally duplicated app/Worker defaults remain synchronized.

### Data, build and configuration

- Pure codecs remain JVM-testable where designed that way.
- M3U import/export preserves supported metadata and stable station identity.
- User files use the Storage Access Framework.
- Default and Polish strings stay in parity.
- Current version catalog, Gradle files, wrapper, workflows and `wrangler.jsonc` are source of truth.
- New lint errors are fixed rather than hidden in the baseline.
- Secrets/private Cloudflare identifiers do not enter app code, docs, skills, logs or PR text.

## Validation and output

Read active workflows to identify the actual CI matrix and cite observed checks on the final head SHA. A local typecheck or green unit test does not prove launcher, playback, notification, device or live Worker behavior.

For each finding include severity, location, impact and smallest practical fix. End with verdict, observed checks, physical/live verification still missing and unresolved review threads or repository-rule limitations.
