# Concerns

This is a maintenance-risk map, not a bug list. Items here are worth remembering when changing nearby code.

## 1. Default feed configuration can drift

**Risk:** medium.

The default feed list currently exists in both Android `NewsRepository.DEFAULT_FEEDS` and Worker `DEFAULT_FEEDS` in `wrangler.jsonc`.

That duplication is intentional today because both on-device and backend modes need defaults, but it creates a coupled contract. Updating only one side changes behavior depending on whether the Worker is enabled.

**Current mitigation:** documentation explicitly requires both lists to stay synchronized.

**Preferred direction:** if a simple maintained source can generate both representations without complicating F-Droid/local builds, converge on it rather than adding a third copy.

## 2. Worker tests do not exercise the Workers runtime

**Risk:** medium.

`worker/vitest.config.ts` runs tests in plain Node. This is appropriate for the current pure helper tests, but it cannot validate Cloudflare-specific behavior such as real bindings, Cache API semantics, `HTMLRewriter` runtime differences or D1/KV integration wiring.

**Current mitigation:** runtime-facing code is kept thin and many transformations are exported as pure helpers; Worker CI typechecks the package and Cloudflare Workers Builds owns deployment.

**Watch for:** changes whose correctness depends on Workers runtime behavior rather than pure parsing. Those are candidates for Cloudflare's Vitest/Workers test environment instead of pretending a Node unit test covers them.

## 3. Several files are large orchestration hotspots

**Risk:** medium.

The current tree contains a few intentionally broad files, notably `worker/src/index.ts`, `ReaderComponents.kt`, `PlayerComponents.kt` and `PlayerService.kt`.

Large size is not itself a defect, but these files combine many routes/components/state transitions and therefore have a larger regression radius.

**Guideline:** extract only along an existing concern boundary when a change benefits from it. Do not create a parallel architecture merely to make files shorter.

## 4. External feeds, directories and streams are inherently unstable

**Risk:** medium operationally, low architecturally.

Kanarek depends on third-party publishers, generated feeds, Radio Browser, iptv-org metadata and individual radio/IPTV stream endpoints. Hosts can disappear, redirect, rate-limit, change HTML or require new headers without a Kanarek release.

**Current mitigation:** bounded reads, per-source failure isolation, on-device fallback, persisted stations, last-known-good widget data, optional directory/logo lookups and per-stream header support.

**Watch for:** fixes that special-case one provider inside a shared model. Keep provider quirks at the integration edge where possible.

## 5. Android is the shipping UI; iOS is currently only a shared target

**Risk:** product-direction ambiguity.

The KMP module builds/tests Android and iOS targets, but this repository contains no standalone iOS application project. The shared boundary is real and CI-enforced, yet some Android-facing repositories/stores still naturally remain in `app/`.

[ASK USER] Is iOS intended to become a shipping Kanarek client, or is the current iOS target mainly a portability guard for shared logic? The answer changes how aggressively new domain/data work should be pushed into `shared`.

Until that is decided, prefer portable `commonMain` code when it is naturally platform-independent, but do not force Android-specific lifecycle/storage code through abstractions solely for hypothetical reuse.

## 6. Release signing after organization migration is external state

**Risk:** operational.

`docs/DEVELOPMENT.md` notes that standalone GitHub release signing depends on repository secrets in `twojstar/kanarek`. Secret values cannot be verified from repository contents.

**Current mitigation:** Gradle release signing is optional so unsigned/F-Droid downstream builds remain possible.

**Watch for:** assuming a successful unsigned/local release build proves GitHub's signed rolling release path is configured.

## 7. Launcher widget behavior depends partly on host launchers

**Risk:** low to medium.

App Widgets run inside another process/UI host and support only a constrained `RemoteViews` vocabulary. Launcher implementations also differ in auto-advance and resize behavior.

**Current mitigation:** launcher-safe layouts, explicit size classes, fallback auto-advance behavior, bounded image cache and Robolectric tests that apply real `RemoteViews`.

**Watch for:** Compose-only components or unsupported view/resource assumptions leaking into widget layouts.

## 8. Backend optionality is an architectural invariant

**Risk:** high if accidentally broken.

The Worker adds substantial functionality, which makes it tempting to rely on normalized Worker responses everywhere. Basic RSS/Atom reading must still work with no backend and should fall back on-device when a backend request fails under the current contract.

**Current mitigation:** `NewsRepository` explicitly contains both paths and shared `FeedParser` remains Android-usable.

Treat a change that makes the Worker mandatory for ordinary feeds as an architectural/product change, not a routine refactor.

## Recently corrected drift

The architecture documentation previously listed portable parser/model files as if they still lived under `app/src/main`. They now live in `shared/commonMain`. The codebase-knowledge refresh corrects that map; future module moves should update these docs in the same change.
