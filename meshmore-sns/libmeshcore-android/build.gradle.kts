// Copyright (c) 2026 IoTone, Inc.
// SPDX-License-Identifier: MIT
plugins {
    id("com.android.library")
    `maven-publish`
}

group = "io.iotone.meshcore"
version = "0.1.0"

android {
    namespace = "io.iotone.meshcore.android"

    // AndroidXR is based on Android 15 (API 35); minSdk 26 covers all
    // modern Android targets back to 8.0 Oreo (BLE reliable from 26+).
    compileSdk = 35
    defaultConfig {
        minSdk = 26
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        // Device name for the live test — pass via project property so it is
        // baked into the DSL (required for configuration cache compatibility):
        //   ./run.sh connectedAndroidTest T1000-E
        //   — or directly —
        //   ./gradlew connectedAndroidTest -PdeviceName=T1000-E
        val deviceName = project.findProperty("deviceName") as String? ?: ""
        if (deviceName.isNotEmpty()) {
            testInstrumentationRunnerArguments["deviceName"] = deviceName
        }
        consumerProguardFiles("consumer-rules.pro")
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
    }
    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

dependencies {
    // Protocol layer — resolved via composite build, no publish needed.
    api("io.iotone.meshcore:libmeshcore")

    // Nordic Android BLE Library: robust BLE connection management on
    // Android/AndroidXR (service discovery, MTU negotiation, retry,
    // threading). Wraps android.bluetooth.BluetoothGatt.
    api("no.nordicsemi.android:ble:2.10.1")

    // Instrumented test dependencies (not packaged in the AAR).
    androidTestImplementation("androidx.test:runner:1.6.2")
    androidTestImplementation("androidx.test:rules:1.6.1")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    // JSON for fixture export assertions.
    androidTestImplementation("org.json:json:20240303")
}

publishing {
    publications {
        register<MavenPublication>("release") {
            afterEvaluate { from(components["release"]) }
            pom {
                name = "libmeshcore-android"
                description =
                    "Android/AndroidXR BLE transport + live integration tests " +
                    "for libmeshcore (MeshCore companion-radio protocol)."
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
