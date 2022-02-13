plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.dokka")
    id("net.bitsandbobs.kradle")
}

dependencies {
    implementation(project(":lib"))
}

group = "net.bnb1.hello"
version = "1.0-SNAPSHOT"

kradle {
    kotlinJvmApplication {
        jvm {
            kotlin {
                codeAnalysis {
                    detekt {
                        configFile("../detekt-config.yml")
                    }
                }
            }
            application {
                mainClass("net.bnb1.hello.ApplicationKt")
            }
            packaging {
                uberJar {
                    minimize(true)
                }
            }
            docker {
                imageName("hello-kotlin")
                ports.add(8080)
                jvmOpts("-Xmx16M -XX:MaxMetaspaceSize=32M -XX:ReservedCodeCacheSize=8M -XX:MaxDirectMemorySize=8M -Xss1M")
                withJvmKill()
            }
        }
    }
}
