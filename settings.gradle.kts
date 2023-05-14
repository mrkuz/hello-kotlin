pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenLocal()
    }
}

plugins {
    id("org.jetbrains.kotlin.jvm") version "1.8.21" apply false
    id("org.jetbrains.dokka") version "1.6.10" apply false
    id("net.bitsandbobs.kradle") version "main-SNAPSHOT" apply false
}

rootProject.name = "hello-kotlin"
include("app", "lib")
