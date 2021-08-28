plugins {
    id("org.jetbrains.kotlin.jvm") version "1.5.0"
    id("net.bnb1.kradle-lib") version "main-SNAPSHOT"
}

dependencies {
    // Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.2.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.2.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-hocon:1.2.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-properties:1.2.0")
    implementation("com.charleskorn.kaml:kaml:0.28.3")
    // Testing
    testImplementation("com.squareup.okhttp3:okhttp:4.9.1")
}

group = "net.bnb1.hello"
version = "1.0-SNAPSHOT"

kradle {
    tests {
        useKotest()
        useMockk()
    }
}
