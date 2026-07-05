#!/usr/bin/env bash
# run.sh — wrapper for ./gradlew that auto-selects a JDK 17–21 on macOS.
#
# AGP 8.x (used by libmeshcore-android) requires JDK 17–21.
# JDK 22+ breaks the AGP jlink step. JDK 11 is too old for Gradle 9.
#
# Usage:
#   ./run.sh                                    # assembleDebug (compile, no device)
#   ./run.sh build                              # compile + lint
#   ./run.sh connectedAndroidTest               # live tests (skipped if no deviceName)
#   ./run.sh connectedAndroidTest T1000-E       # live tests against named radio
#   ./run.sh <any gradlew task> [deviceName]
set -uo pipefail   # -e removed: glob no-match exits silently, use explicit checks

# ---------------------------------------------------------------------------
# Find a JDK 17–21 on macOS. Scans all candidate directories; picks the
# highest major version in [17, 21].
#
# Locations searched:
#   /Library/Java/JavaVirtualMachines/   — Oracle, Azul, Temurin, Homebrew keg
#   ~/Library/Java/JavaVirtualMachines/  — SDKMAN, jenv, per-user installs
#   /opt/homebrew/opt/openjdk@*/         — brew install openjdk@21, etc.
# ---------------------------------------------------------------------------
unset JAVA_HOME
BEST_MAJOR=0
BEST_HOME=""

_check() {
    # _check <candidate_home_dir>
    # Reads the actual java version; updates BEST_* if it's in [17,21].
    local home="$1"
    local java="$home/bin/java"
    [[ -x "$java" ]] || return 0          # not executable — skip, don't exit
    local ver
    ver=$("$java" -version 2>&1 | head -1)
    local major
    major=$(echo "$ver" | grep -oE '"[0-9]+' | head -1 | tr -d '"')
    [[ "$major" =~ ^[0-9]+$ ]] || return 0
    if (( major >= 17 && major <= 21 && major > BEST_MAJOR )); then
        BEST_MAJOR=$major
        BEST_HOME=$home
    fi
}

if [[ "$(uname)" == "Darwin" ]]; then
    # Enable nullglob so unmatched globs expand to nothing (not the literal pattern).
    shopt -s nullglob

    for d in /Library/Java/JavaVirtualMachines/*/Contents/Home; do
        _check "$d"
    done
    for d in "$HOME"/Library/Java/JavaVirtualMachines/*/Contents/Home; do
        _check "$d"
    done
    for d in /opt/homebrew/opt/openjdk@*/libexec/openjdk.jdk/Contents/Home; do
        _check "$d"
    done

    shopt -u nullglob
fi

if [[ -z "$BEST_HOME" ]]; then
    echo "ERROR: No JDK 17–21 found." >&2
    echo "  AGP 8.x requires JDK 17–21; JDK 22+ breaks the jlink build step." >&2
    echo "  Searched:" >&2
    echo "    /Library/Java/JavaVirtualMachines/" >&2
    echo "    ~/Library/Java/JavaVirtualMachines/" >&2
    echo "    /opt/homebrew/opt/openjdk@*/" >&2
    echo "" >&2
    echo "  Fix:  brew install openjdk@21" >&2
    echo "  Then: sudo ln -sfn \$(brew --prefix openjdk@21) /Library/Java/JavaVirtualMachines/openjdk-21.jdk" >&2
    exit 1
fi

export JAVA_HOME="$BEST_HOME"
echo "JAVA_HOME: $JAVA_HOME  (JDK $BEST_MAJOR)"

# ---------------------------------------------------------------------------
# Task and optional BLE device name
# ---------------------------------------------------------------------------
TASK="${1:-assembleDebug}"
DEVICE_ARG=""
if [[ "${2:-}" != "" ]]; then
    # Pass as a project property; the build.gradle.kts forwards it into the
    # Android DSL (required for configuration cache compatibility — the
    # android.testInstrumentationRunnerArguments.* command-line form is not).
    DEVICE_ARG="-PdeviceName=${2}"
fi

# connectedAndroidTest: the connected device and deviceName both vary per run,
# so bypass the configuration cache and always re-run the task.
EXTRA_FLAGS=""
if [[ "$TASK" == *"connectedAndroidTest"* ]]; then
    EXTRA_FLAGS="--no-configuration-cache --rerun-tasks"
fi

echo "Running: ./gradlew $TASK${DEVICE_ARG:+ $DEVICE_ARG}${EXTRA_FLAGS:+ $EXTRA_FLAGS}"
exec ./gradlew "$TASK" ${DEVICE_ARG:+"$DEVICE_ARG"} $EXTRA_FLAGS
