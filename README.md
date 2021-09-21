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

- Kradle: Swiss army knife for Kotlin development ([Link](https://github.com/mrkuz/kradle))

## Useful Gradle tasks

- List all tasks: `./gradlew tasks`
- Clean: `./gradlew clean`
- Check for dependency updates: `./gradlew showDependencyUpdates`
- Run application: `./gradlew run`
- Continuous run with automatic restarts: `./gradlew dev -t`
- Run ktlint: `./gradlew lint`
- Run tests: `./gradlew test`
- Run benchmarks: `./gradlew runBenchmarks`
- Build project and create JAR: `./gradlew build`
- Create runnabale Uber-JAR: `./gradlew uberJar`
- Build Docker image: `./gradlew buildImage`
- Build documentation: `./gradlew generateDocumentation`
- Update Gradle wrapper: `./gradlew wrapper --gradle-version=X.Y.Z`
