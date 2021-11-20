plugins {
    id("lib-conventions")
}

dependencies {
    // Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.3.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-hocon:1.3.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-properties:1.3.1")
    implementation("com.charleskorn.kaml:kaml:0.37.0")
    // Testing
    testImplementation("com.squareup.okhttp3:okhttp:4.9.2")
}
