plugins {
    id("org.jetbrains.kotlin.jvm") version "1.6.0"
    id("net.bitsandbobs.kradle") version "main-SNAPSHOT"
}

dependencies {
    implementation(project(":lib"))
}

group = "net.bnb1.hello"
version = "1.0-SNAPSHOT"

kradle {
    kotlinJvmApplication {
        jvm {
            application {
                mainClass("net.bnb1.hello.ApplicationKt")
            }
            packaging {
                uberJar {
                    minimize(true)
                }
            }
            docker {
                ports.add(8080)
                jvmOpts("-Xmx16M -XX:MaxMetaspaceSize=32M -XX:ReservedCodeCacheSize=8M -XX:MaxDirectMemorySize=8M -Xss1M")
                withJvmKill()
            }
        }
    }
}
