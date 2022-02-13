plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.dokka")
    id("net.bitsandbobs.kradle")
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
    kotlinJvmLibrary {
        jvm {
            kotlin {
                codeAnalysis {
                    detekt {
                        configFile("../detekt-config.yml")
                    }
                }
            }
        }
    }
}
