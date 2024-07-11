// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("io.realm:realm-gradle-plugin:10.18.0")
    }
}

plugins {
    id("com.android.application") version "8.5.0" apply false
}
