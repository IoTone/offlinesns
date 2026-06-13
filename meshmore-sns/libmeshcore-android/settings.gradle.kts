// Copyright (c) 2026 IoTone, Inc.
// SPDX-License-Identifier: MIT

// All dependency resolution goes through Google + Maven Central so AGP
// and its transitive deps (lint, d8, etc.) can be resolved.
dependencyResolutionManagement {
    repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS
    repositories {
        google()
        mavenCentral()
    }
}

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    // Pin AGP here so `wrapper` and CI see the same version without
    // needing it in the build script.
    plugins {
        id("com.android.library") version "8.10.1"
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

// libmeshcore lives next door — composite build so :libmeshcore resolves
// locally (no publish step needed between changes to the two modules).
includeBuild("../libmeshcore")

rootProject.name = "libmeshcore-android"
