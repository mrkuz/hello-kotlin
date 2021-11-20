import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import kotlinx.benchmark.gradle.BenchmarksExtension
import kotlinx.benchmark.gradle.JvmBenchmarkTarget
import net.bnb1.gradle.ProjectProperties
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.gradle.jvm.tasks.Jar
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
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.2")
    // Testing
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
    testImplementation("io.kotest:kotest-runner-junit5:4.6.3")
    testImplementation("io.kotest:kotest-assertions-core:4.6.3")
    testImplementation("io.mockk:mockk:1.12.1")
}

ProjectProperties.load(rootDir.absolutePath)

val jvmVersion = "11"

tasks.withType<JavaCompile>().configureEach {
    sourceCompatibility = jvmVersion
    targetCompatibility = jvmVersion
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xopt-in=kotlin.RequiresOptIn")
        jvmTarget = jvmVersion
    }
}

configure<KtlintExtension> {
    enableExperimentalRules.set(true)
    disabledRules.set(setOf("no-wildcard-imports"))
    version.set("0.43.0")
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
        register("benchmark") {
            (this as JvmBenchmarkTarget).jmhVersion = "1.33"
        }
    }
}

// Create a new source set for benchmarks
val mainSourceSet: SourceSet = sourceSets.getByName("main")
sourceSets.create("benchmark") {
    @Suppress("DEPRECATION")
    withConvention(org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet::class) {
        kotlin.srcDirs("src/benchmark/kotlin")
        compileClasspath += mainSourceSet.output + mainSourceSet.compileClasspath
        runtimeClasspath += mainSourceSet.output + mainSourceSet.runtimeClasspath
        dependencies {
            implementation("org.jetbrains.kotlinx:kotlinx-benchmark-runtime:0.3.1")
        }
    }
}

afterEvaluate {
    tasks.named<Jar>("benchmarkBenchmarkJar").configure {
        // Required workaround. Otherwise, running the benchmarks will complain because of
        // duplicate META-INF/versions/9/module-info.class
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
    }
}

tasks.named<DependencyUpdatesTask>("dependencyUpdates").configure {
    revision = "release"
    checkForGradleUpdate = true
    rejectVersionIf {
        // Exclude milestone builds and RCs
        val alpha = "^.*-alpha[.-]?[0-9]*$".toRegex()
        val milestone = "^.*[.-]M[.-]?[0-9]+$".toRegex()
        val releaseCandidate = "^.*-RC[.-]?[0-9]*$".toRegex()
        alpha.matches(candidate.version)
            || milestone.matches(candidate.version)
            || releaseCandidate.matches(candidate.version)
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
