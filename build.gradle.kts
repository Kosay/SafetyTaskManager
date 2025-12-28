// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    // Android Application and Library plugins
    id("com.android.application") version "8.7.0" apply false
    id("com.android.library") version "8.7.0" apply false

    // Kotlin Android plugin
    id("org.jetbrains.kotlin.android") version "1.9.24" apply false

    // Google Services plugin (Required for Firebase)
    id("com.google.gms.google-services") version "4.4.2" apply false
    // REMOVED: Hilt and KSP plugins to keep the project lightweight and fast
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin") version "2.0.1" apply false
}