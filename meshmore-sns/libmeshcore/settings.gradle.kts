plugins {
    // Auto-provisions the Java 17 toolchain when it isn't installed
    // locally (the gradle-recommended resolver).
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "libmeshcore"
