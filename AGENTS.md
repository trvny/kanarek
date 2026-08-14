# AGENTS.md

Kanarek is an Android reader/player and its supporting Cloudflare Worker. Feed generation belongs to `trvny/feedseek`; do not add Feedseek code or sources here.

## Repository

- `app/`: Kotlin/Compose Android app, news widgets, radio/IPTV player and player widget.
- `worker/`: optional TypeScript Worker for feed proxying, discovery/scraping and synchronized state.
- The Worker is optional. An empty backend configuration must keep on-device feed parsing functional.
- Keep app and Worker changes separate unless a shared contract or default requires both.
- Generated/build output is not maintained source.

## Working method

- Check `main`, open pull requests and recent changes before overlapping work.
- Read the active workflow and project files instead of relying on remembered versions or commands.
- Keep one maintained source of truth per concern.
- Never commit credentials, tokens, account IDs or private deployment metadata. Public Wrangler binding/resource IDs required for reproducible deployment belong in `worker/wrangler.jsonc`.
- Treat `megalinter-reports/updated_sources` as suggestions and apply only intended fixes.

## GitHub

Use `gptomek[bot]` for commits, comments, review replies and reactions when available. Open pull requests as `trvny` so automatic reviews run. Treat automated reviews as advisory and apply valid findings directly.

Keep one logical change per pull request. Truly trivial low-risk fixes may go directly to `main`. Merge only when relevant checks are green on the final head and actionable review threads are resolved; prefer squash merge.
