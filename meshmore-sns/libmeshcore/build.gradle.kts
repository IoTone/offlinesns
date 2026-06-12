// Copyright (c) 2026 IoTone, Inc.
// SPDX-License-Identifier: MIT
plugins {
    `java-library`
    `maven-publish`
}

group = "io.iotone.meshcore"
version = "0.1.0"

java {
    // Java 17: modern language level (records, sealed types, pattern
    // switch) while remaining consumable by Android (AGP 8+ desugars
    // records/sealed for all supported API levels) and by any
    // JDK 17+ server runtime.
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
    withSourcesJar()
    withJavadocJar()
}

repositories {
    mavenCentral()
}

dependencies {
    // Ed25519 / X25519 via the BouncyCastle *lightweight* math API
    // (org.bouncycastle.math.ec.rfc8032 / rfc7748) — used directly,
    // WITHOUT registering the JCE provider, so there is no conflict
    // with Android's preinstalled (stripped) BouncyCastle.
    api("org.bouncycastle:bcprov-jdk18on:1.81")

    testImplementation(platform("org.junit:junit-bom:5.11.4"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    // Golden-vector JSON parsing (test-only).
    testImplementation("org.json:json:20240303")
}

tasks.test {
    useJUnitPlatform()
}

tasks.javadoc {
    (options as StandardJavadocDocletOptions).apply {
        encoding = "UTF-8"
        charSet = "UTF-8"
        // The javadocs are part of the deliverable: fail the build on
        // any missing/invalid doc comment rather than warning.
        addBooleanOption("Xwerror", true)
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            pom {
                name = "libmeshcore"
                description =
                    "Pure-Java client codec/crypto for the MeshCore " +
                    "companion-radio protocol (Android- and server-ready)."
                url = "https://github.com/iotone/meshmore-sns"
                licenses {
                    license {
                        name = "MIT"
                        url = "https://opensource.org/licenses/MIT"
                    }
                }
            }
        }
    }
}
