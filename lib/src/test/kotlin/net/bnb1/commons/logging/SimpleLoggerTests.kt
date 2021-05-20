package net.bnb1.commons.logging

import io.kotest.assertions.timing.continually
import io.kotest.assertions.timing.eventually
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.string.shouldHaveLength
import io.kotest.matchers.string.shouldMatch
import net.bnb1.commons.utils.UTF8
import net.bnb1.commons.utils.milliseconds
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import kotlin.time.ExperimentalTime

@ExperimentalTime
class SimpleLoggerTests : FunSpec({

    isolationMode = IsolationMode.InstancePerTest

    val baos = ByteArrayOutputStream()
    fun output(): String = baos.toString(UTF8.charset()).filterNot { it == '\n' }

    fun pattern(level: LogLevel, name: String, message: String): Regex {
        return Regex("\\d{2}:\\d{2}:\\d{2}\\.\\d{3} \\[.*] ${level.name.padEnd(5)} $name - $message.*")
    }

    context("Logging with string message") {

        val logger = SimpleLogger("Test", LogLevel.DEBUG, PrintStream(baos, true))

        test("Log debug message") {
            logger.debug("This is a test")
            output() shouldMatch pattern(LogLevel.DEBUG, "Test", "This is a test")
        }

        test("Log info message") {
            logger.info("This is a test")
            output() shouldMatch pattern(LogLevel.INFO, "Test", "This is a test")
        }

        test("Log warning") {
            logger.warn("This is a test")
            output() shouldMatch pattern(LogLevel.WARN, "Test", "This is a test")
        }

        test("Log error") {
            logger.error("This is a test")
            output() shouldMatch pattern(LogLevel.ERROR, "Test", "This is a test")
        }
    }

    context("Logging with message supplier") {

        val logger = SimpleLogger("Test", LogLevel.DEBUG, PrintStream(baos, true))

        test("Log debug message") {
            logger.debug { "This is a test" }
            eventually(100.milliseconds) {
                output() shouldMatch pattern(LogLevel.DEBUG, "Test", "This is a test")
            }
        }

        test("Log info message") {
            logger.info { "This is a test" }
            eventually(100.milliseconds) {
                output() shouldMatch pattern(LogLevel.INFO, "Test", "This is a test")
            }
        }

        test("Log warning") {
            logger.warn { "This is a test" }
            eventually(100.milliseconds) {
                output() shouldMatch pattern(LogLevel.WARN, "Test", "This is a test")
            }
        }

        test("Log error") {
            logger.error { "This is a test" }
            eventually(100.milliseconds) {
                output() shouldMatch pattern(LogLevel.ERROR, "Test", "This is a test")
            }
        }
    }

    context("Logging with string message and threshold") {

        val logger = SimpleLogger("Test", LogLevel.INFO, PrintStream(baos, true))

        test("Log debug message") {
            logger.debug("This is a test")
            output() shouldHaveLength 0
        }

        test("Log info message") {
            logger.info("This is a test")
            output() shouldMatch pattern(LogLevel.INFO, "Test", "This is a test")
        }

        test("Log warning") {
            logger.warn("This is a test")
            output() shouldMatch pattern(LogLevel.WARN, "Test", "This is a test")
        }
    }

    context("Logging with message supplier and threshold") {

        val logger = SimpleLogger("Test", LogLevel.INFO, PrintStream(baos, true))

        test("Log debug message") {
            logger.debug { "This is a test" }
            continually(100.milliseconds) {
                output() shouldHaveLength 0
            }
        }

        test("Log info message") {
            logger.info { "This is a test" }
            eventually(100.milliseconds) {
                output() shouldMatch pattern(LogLevel.INFO, "Test", "This is a test")
            }
        }

        test("Log warning") {
            logger.warn { "This is a test" }
            eventually(100.milliseconds) {
                output() shouldMatch pattern(LogLevel.WARN, "Test", "This is a test")
            }
        }
    }
})
