# Kanarek Cloudflare Worker

The Worker lives in `worker/`. It is an optional accelerator and state backend; the Android app must still parse feeds on-device when no backend URL is configured.

Read before changing Worker behavior:

- `worker/src/index.ts` for routes and runtime behavior;
- `worker/wrangler.jsonc` for bindings, vars, compatibility date and deployment identity;
- `worker/package.json` for commands;
- active Worker CI/deployment workflows under `.github/workflows/`.

Do not duplicate binding IDs, account IDs, database UUIDs, default-feed lists or compatibility dates here. Wrangler configuration is the deployment source of truth.

## Load-bearing behavior

- Missing Worker/state bindings fail locally and must not disable on-device parsing or unrelated routes.
- Fetch and parse each source under its own error boundary.
- Derive ETags from stable item content, not volatile fetch time; unchanged content can return a bodyless `304`.
- Discovery prefers declared RSS/Atom alternatives and uses bounded fallback probes. Scraping remains host-restricted, time-bounded and cached.
- Preserve validation, device isolation and migration compatibility for D1-backed state/pairing; do not expose pairing secrets or device state in logs.
- When app and Worker intentionally carry the same defaults, update both source files in one logical change.

## Configuration and secrets

Keep public vars and bindings in `wrangler.jsonc` where appropriate and secret values in Cloudflare secret storage. Do not rename live Worker/KV/D1/R2 resources as a side effect of code changes.

## Validation

From `worker/`:

```bash
npm ci
npm run typecheck
npm test
```

## Deployment

Use the repository's Wrangler configuration and active deployment workflow, or `npm run deploy` only when an explicit manual deployment is requested and the environment is authenticated. After deployment, observe the actual result and smoke-test changed routes. A successful commit or typecheck is not deployment proof.
