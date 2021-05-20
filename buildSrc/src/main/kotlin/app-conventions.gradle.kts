import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.google.cloud.tools.jib.gradle.JibExtension
import net.bnb1.gradle.Git
import net.bnb1.gradle.projectProperty

plugins {
    id("com.github.johnrengelman.shadow")
    id("com.google.cloud.tools.jib")
    id("common-conventions")
    application
}

tasks.named<JavaExec>("run").configure {
    // Allows the application to figure out, that we are running in development environment
    environment("BNB1_PROFILE", "dev")
    // Speed up start when developing
    jvmArgs = listOf("-Xverify:none", "-XX:TieredStopAtLevel=1")
}

tasks.named<ShadowJar>("shadowJar").configure {
    archiveVersion.set(projectProperty("version"))
    archiveClassifier.set(projectProperty("archiveClassifier"))
    // Remove unused dependencies
    minimize()
}

// Create build.properties with details about the latest build
tasks.create("generateBuildProperties") {
    // Ensure that this task is always executed
    outputs.upToDateWhen { false }
    doLast {
        val file = File("$buildDir/resources/main/build.properties")
        file.parentFile.mkdirs()
        file.printWriter().use {
            it.println("version=${projectProperty("version")}")
            it.println("timestamp=${System.currentTimeMillis() / 1000}")
            it.println("git.commit-id: ${Git.commitHash()}")
        }
    }
}

tasks.named("processResources").configure {
    dependsOn("generateBuildProperties")
}

configure<JavaApplication> {
    mainClass.set(projectProperty("mainClass"))
    @Suppress("DEPRECATION")
    mainClassName = mainClass.get()
}

configure<JibExtension> {
    from {
        image = projectProperty("baseImage")
    }
    to {
        image = "${rootProject.name}:latest"
        tags = setOf(projectProperty("version"))
    }
    // Include all files from extra in the image
    extraDirectories {
        paths {
            path {
                setFrom("$rootDir/extra/")
                into = "/app/extra/"
            }
        }
    }
    container {
        ports = listOf(projectProperty("containerPort"))
        jvmFlags = projectProperty("containerJvmFlags").split(" ")
    }
}