plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.sqldelight)
}

kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "11"
            }
        }
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            // Compose Multiplatform
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(compose.ui)
            implementation(compose.components.resources)

            // Architecture & Lifecycle
            implementation(libs.androidx.lifecycle.viewmodel.compose)
            implementation(libs.androidx.lifecycle.runtime.compose)

            // Dependency Injection (Koin)
            implementation(libs.koin.core)
            implementation(libs.koin.compose)

            // SQLDelight
            implementation(libs.sqldelight.coroutines)
            implementation(libs.sqldelight.primitive.adapters)

            // Serialization & Dates
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.coroutines.core)

            // QR Code Generation
            implementation(libs.qrcode.kotlin)
        }

        androidMain.dependencies {
            implementation(libs.androidx.core.ktx)
            implementation(libs.androidx.activity.compose)
            implementation(libs.koin.android)
            implementation(libs.sqldelight.android.driver)

            // CameraX & ML Kit for QR scanning on Android
            implementation(libs.androidx.camera.camera2)
            implementation(libs.androidx.camera.lifecycle)
            implementation(libs.androidx.camera.view)
            implementation(libs.androidx.camera.core)
            implementation(libs.mlkit.barcode)

            // Biometric Auth on Android
            implementation(libs.androidx.biometric)

            // Coil
            implementation(libs.coil.compose)
        }

        iosMain.dependencies {
            implementation(libs.sqldelight.native.driver)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}

android {
    namespace = "com.tindatrack.shared"
    compileSdk = 35

    defaultConfig {
        minSdk = 24
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

sqldelight {
    databases {
        create("TindaDatabase") {
            packageName.set("com.tindatrack.db")
            srcDirs("src/commonMain/sqldelight")
        }
    }
}
