plugins {
    id("org.jetbrains.kotlin.jvm") version "1.5.0"
    id("net.bnb1.kradle-app") version "main-SNAPSHOT"
}

dependencies {
    implementation(project(":lib"))
}

group = "net.bnb1.hello"
version = "1.0-SNAPSHOT"

kradle {
    tests {
        useKotest()
        useMockk()
    }
    image {
        withJvmKill()
        ports.add(8080)
        javaOpts.set("-Xmx16M -XX:MaxMetaspaceSize=32M -XX:ReservedCodeCacheSize=8M -XX:MaxDirectMemorySize=8M -Xss1M")
    }
}

application {
    mainClass.set("net.bnb1.hello.ApplicationKt")
}
