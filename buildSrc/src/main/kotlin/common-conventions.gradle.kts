import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import kotlinx.benchmark.gradle.BenchmarksExtension
import net.bnb1.gradle.ProjectProperties
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.allopen.gradle.AllOpenExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jlleitschuh.gradle.ktlint.KtlintExtension

plugins {
    kotlin("jvm")
    kotlin("plugin.allopen")
    kotlin("plugin.serialization")
    id("com.github.ben-manes.versions")
    id("org.jetbrains.kotlinx.benchmark")
    id("org.jetbrains.dokka")
    id("com.adarshr.test-logger")
    id("org.jlleitschuh.gradle.ktlint")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.0")
    // Testing
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.7.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.7.2")
    testImplementation("io.kotest:kotest-runner-junit5:4.6.0")
    testImplementation("io.kotest:kotest-assertions-core:4.6.0")
    testImplementation("io.mockk:mockk:1.11.0")
}

ProjectProperties.load(rootDir.absolutePath)

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xopt-in=kotlin.RequiresOptIn")
        jvmTarget = "1.8"
    }
}

configure<KtlintExtension> {
    enableExperimentalRules.set(true)
    disabledRules.set(setOf("no-wildcard-imports"))
}

tasks.named<Test>("test").configure {
    useJUnitPlatform()
    testLogging {
        events = setOf(TestLogEvent.SKIPPED, TestLogEvent.PASSED, TestLogEvent.FAILED)
    }
    includes.add("**/*Tests*.class")
}

// JMH requires benchmark classes to be open
configure<AllOpenExtension> {
    annotation("org.openjdk.jmh.annotations.State")
}

configure<BenchmarksExtension> {
    targets {
        register("benchmark")
    }
}

// Create a new source set for benchmarks
val mainSourceSet: SourceSet = sourceSets.getByName("main")
sourceSets.create("benchmark") {
    withConvention(org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet::class) {
        kotlin.srcDirs("src/benchmark/kotlin")
        compileClasspath += mainSourceSet.output + mainSourceSet.compileClasspath
        runtimeClasspath += mainSourceSet.output + mainSourceSet.runtimeClasspath
        dependencies {
            implementation("org.jetbrains.kotlinx:kotlinx-benchmark-runtime:0.3.1")
        }
    }
}

tasks.named<DependencyUpdatesTask>("dependencyUpdates").configure {
    revision = "release"
    checkForGradleUpdate = true
    rejectVersionIf {
        // Exclude milestone builds and RCs
        val milestone = "^.*-M[0-9]+$".toRegex()
        val releaseCandidate = "^.*-RC$".toRegex()
        milestone.matches(this.candidate.version) || releaseCandidate.matches(this.candidate.version)
    }
}

tasks.named<DokkaTask>("dokkaHtml").configure {
    this.dokkaSourceSets.forEach {
        if (File("$projectDir/src/main/kotlin/module.md").exists()) {
            // Use module.md to add module/package documentation
            it.includes.setFrom("src/main/kotlin/module.md")
        }
    }
}
