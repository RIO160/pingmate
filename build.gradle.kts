// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath ("com.android.tools.build:gradle:8.7.3") // Ensure this matches your Gradle version
        classpath ("com.google.gms:google-services:4.4.2")
        classpath ("com.google.gms:google-services:4.3.13") // or the latest version
// Add this line
    }
}



plugins {
    alias(libs.plugins.android.application) apply false
    id("com.google.gms.google-services") version "4.4.2" apply false

}