// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("io.realm:realm-gradle-plugin:10.18.0")
        classpath("com.google.android.gms:oss-licenses-plugin:0.10.6")

    }
}

plugins {
    id("com.android.application") version "8.5.2" apply false
}
