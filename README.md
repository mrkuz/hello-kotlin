# General

Simple example application to explore the Kotlin programming language.

This project is set up as Gradle multi-project build, consisting of two subprojects:

- app: The actual application
- lib: Common utilities and functions used by the application

# Application

Starts a simple HTTP server providing following endpoints:

- `GET /actuator/health` - Returns OK
- `GET /actuator/info` - Returns some infos like version, commit, JVM version, etc.
- `POST /actuator/gc` - Run GC
- `POST /actuator/shutdown` - Shutdown application

# Library

> The code was written for educational purposes.
> Don't expect full-flegded production-ready implementations.

Provides basic building blocks for applications, including

- Logging
- Loading HOCON, YAML and property files from classpath
- Dependency injection
- Profiles
- Event bus
- Task scheduling
- NIO HTTP server
- Development mode

## Profiles

Profiles can be used to control the behaviour of the application in different environments. The active profile is read
from the environment variable `BNB1_PROFILE`. If not set, 'default' is used.

`gradle run` activates the profile 'dev'.

## Development mode

Development mode watches the source directory for changes and stops the application if a file is modified, added or
deleted. This is intended to be used with `gradle run -t` to trigger compilation and restart on code changes.

# Testing

Following libraries are used:

- Kotest ([Link](https://kotest.io/))
- MockK ([Link](https://mockk.io/))

# Build

The build process is configured using Gradle Kotlin DSL.

## Used Gradle plugins

- Versions: Checks for dependency updates ([Link](https://plugins.gradle.org/plugin/com.github.ben-manes.versions)))
- Dokka: Creates KDoc documentation ([Link](https://plugins.gradle.org/plugin/org.jetbrains.dokka))
- Shadow: Creates Fat Jar ([Link](https://plugins.gradle.org/plugin/com.github.johnrengelman.shadow))
- Jib: Containerizes app ([Link](https://plugins.gradle.org/plugin/com.google.cloud.tools.jib))
- Test Logger: Prints test logs to console ([Link](https://plugins.gradle.org/plugin/com.adarshr.test-logger))
- kotlinx.benchmark: Runs JMH benchmarks ([Link](https://plugins.gradle.org/plugin/org.jetbrains.kotlinx.benchmark))
- Ktlint Gradle: Runs ktlint ([Link](https://plugins.gradle.org/plugin/org.jlleitschuh.gradle.ktlint))

## Custom extensions

- There is a file 'project.properties' in the root direcotry, which is used for build configuration
- Building the project creates a file 'build.properties', with information like timestamp, commit, ...

## Useful Gradle tasks

- List all tasks: `./gradlew tasks`
- Clean: `./gradlew clean`
- Check for dependency updates: `./gradlew dependencyUpdates`
- Run application: `./gradlew run`
- Continuous run: `./gradlew run -t`
- Run ktlint: `./gradlew ktlintCheck`
- Run tests: `./gradlew test`
- Run benchmarks: `./gradlew benchmark`
- Build project and create JAR: `./gradlew build`
- Build Docker image: `./gradlew jibDockerBuild`
- Build documentation: `./gradlew dokkaHtml`
- Update Gradle wrapper: `./gradlew wrapper --gradle-version=X.Y.Z`
