#!/usr/bin/env bash
# Single source for the Gradle version used by every job in this repository's CI.
#
# gradle-wrapper.properties is the source of truth because that is the file Dependabot
# bumps. Hard-coding the version in a workflow would make those bumps a no-op: setup-gradle
# would install the old version and the wrapper regeneration would rewrite the properties
# file back, so CI would go green without ever building against the new Gradle.
#
# Writes `version=<x>` to $GITHUB_OUTPUT. The charset in the regex rejects shell
# metacharacters, which matters because on a pull request from a fork the properties file
# is attacker-controlled.
set -euo pipefail

version=$(sed -n 's#^distributionUrl=.*/gradle-\([0-9][A-Za-z0-9.-]*\)-\(bin\|all\)\.zip$#\1#p' \
  gradle/wrapper/gradle-wrapper.properties)
if [ -z "$version" ]; then
  echo "::error::could not parse a Gradle version out of gradle-wrapper.properties"
  exit 1
fi
echo "version=$version" >> "$GITHUB_OUTPUT"
