plugins {
    id("lib-conventions")
}

dependencies {
    // Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.1.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.1.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-hocon:1.1.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-properties:1.1.0")
    implementation("com.charleskorn.kaml:kaml:0.28.3")
    // Testing
    testImplementation("com.squareup.okhttp3:okhttp:4.9.1")
}
