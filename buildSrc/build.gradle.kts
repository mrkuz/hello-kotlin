plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.5.0")
    implementation("org.jetbrains.kotlin:kotlin-allopen:1.5.0")
    implementation("org.jetbrains.kotlin:kotlin-serialization:1.5.0")
    implementation("org.jetbrains.kotlinx:kotlinx-benchmark-plugin:0.3.1")
    implementation("org.jetbrains.dokka:dokka-gradle-plugin:1.4.30")
    implementation("com.github.ben-manes:gradle-versions-plugin:0.38.0")
    implementation("com.github.jengelman.gradle.plugins:shadow:6.1.0")
    implementation("gradle.plugin.com.google.cloud.tools:jib-gradle-plugin:2.8.0")
    implementation("com.adarshr:gradle-test-logger-plugin:2.1.1")
    implementation("org.jlleitschuh.gradle:ktlint-gradle:10.0.0")
}
