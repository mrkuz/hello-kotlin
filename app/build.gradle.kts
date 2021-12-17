plugins {
    id("org.jetbrains.kotlin.jvm") version "1.6.0"
    id("net.bitsandbobs.kradle-app") version "main-SNAPSHOT"
}

dependencies {
    implementation(project(":lib"))
}

group = "net.bnb1.hello"
version = "1.0-SNAPSHOT"

kradle {
    mainClass("net.bnb1.hello.Application")
    tests {
        useKotest()
        useMockk()
    }
    uberJar {
        minimize(true)
    }
    image {
        ports.add(8080)
        javaOpts("-Xmx16M -XX:MaxMetaspaceSize=32M -XX:ReservedCodeCacheSize=8M -XX:MaxDirectMemorySize=8M -Xss1M")
        withJvmKill()
    }
}
