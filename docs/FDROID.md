# Submitting Kanarek to F-Droid

Working notes for the fdroiddata merge request. Nothing here is submitted yet.

## Why this repository exists

Kanarek used to live in `trvny/feeds/kanarek/`, next to an unrelated Python project
(`feedseek`). F-Droid can build from a subdirectory, but the metadata, the issue tracker
link, the tags and the changelog would all have pointed at a repository that is mostly not
this app. The history was extracted with `git subtree split --prefix=kanarek`, so every
commit that ever touched the app is preserved here; `v1.0.0` and `v1.0.1` were re-tagged on
the extracted commits with the identical trees.

## Draft metadata

To go in `metadata/com.kanarek.yml` in a fork of
[fdroiddata](https://gitlab.com/fdroid/fdroiddata). Verify every field against the
[metadata reference](https://f-droid.org/docs/Build_Metadata_Reference/) before filing;
this is a draft written from the repository, not from a successful build.

```yaml
Categories:
  - Internet
  - Multimedia
License: MIT
AuthorName: trvny
SourceCode: https://github.com/trvny/kanarek
IssueTracker: https://github.com/trvny/kanarek/issues
Changelog: https://github.com/trvny/kanarek/releases

AutoName: Kanarek

RepoType: git
Repo: https://github.com/trvny/kanarek.git

Builds:
  - versionName: 1.0.1
    versionCode: 2
    commit: v1.0.1
    subdir: app
    gradle:
      - foss

AutoUpdateMode: Version v%v
UpdateCheckMode: Tags ^v[0-9.]+$
CurrentVersion: 1.0.1
CurrentVersionCode: 2
```

`gradle: [foss]` is the point of the two product flavors: `play` pulls in Google Cast via
GMS, `foss` does not. F-Droid must build `foss`.

## The Gradle wrapper is missing on purpose

Only `gradle/wrapper/gradle-wrapper.properties` is tracked; there is no `gradle-wrapper.jar`
and no `gradlew`. That is deliberate: a committed jar is a prebuilt binary, which F-Droid's
scanner objects to. fdroidserver runs its own Gradle wrapper and reads the version out of
`gradle-wrapper.properties`, so the build should not need `gradlew` from the repository.
**Check this against a real `fdroid build` before submitting**. If it turns out fdroidserver
does want `gradlew`, the fix is a `prebuild:` step that installs/uses the exact Gradle version
from `gradle-wrapper.properties` before running its `wrapper` task, not committing the jar.
CI here follows the same version-source rule in `.github/workflows/android-ci.yml`.

## Still to do before filing

- **Screenshots and an icon** under `app/src/main/fastlane/metadata/android/<locale>/images/`
  (`icon.png`, `featureGraphic.png`, `phoneScreenshots/1.png`…). Text metadata exists in
  `en-US` and `pl-PL`; the listing will look bare without images.
- A real `fdroid build` / `fdroid lint` run. Neither is possible on the maintainer's Windows
  machine (no Android SDK platform installed), so this needs CI or another box.

The optional `worker/` backend is maintained in this repository but is not part of the APK.
Production deployment is connected directly to `trvny/kanarek` through Cloudflare Workers
Builds, so F-Droid packaging does not depend on the old `trvny/feeds` repository.

## The other F-Droid submission

`trvny/WiFi-Automatic` is a separate, older attempt:
[fdroiddata!41475](https://gitlab.com/fdroid/fdroiddata/-/merge_requests/41475), closed
2026-07-25 because the fork kept upstream's application ID. Unrelated to Kanarek, but the
same reviewer and the same process.
