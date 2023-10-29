// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("io.realm:realm-gradle-plugin:10.11.0")
    }
}

plugins {
    id("com.android.application") version "7.4.2" apply false
}
