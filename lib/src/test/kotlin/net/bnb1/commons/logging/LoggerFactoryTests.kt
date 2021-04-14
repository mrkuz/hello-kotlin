package net.bnb1.commons.logging

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlin.time.ExperimentalTime

@ExperimentalTime
class LoggerFactoryTests : FunSpec({

    context("LoggerFactory.create") {

        context("Create new logger with default threshold") {
            val logger = LoggerFactory.create("Test")
            logger.shouldBeInstanceOf<SimpleLogger>()
            logger.threshold shouldBe LogLevel.DEBUG
        }

        context("Create new logger with INFO threshold") {
            LoggerFactory.setThreshold("Test", LogLevel.INFO)
            val logger = LoggerFactory.create("Test")
            logger.shouldBeInstanceOf<SimpleLogger>()
            logger.threshold shouldBe LogLevel.INFO
        }

        context("Create new logger with OFF threshold") {
            LoggerFactory.setThreshold("Test", LogLevel.OFF)
            val logger = LoggerFactory.create("Test")
            logger.shouldBeInstanceOf<NullLogger>()
            logger.threshold shouldBe LogLevel.OFF
        }
    }

    test("Generate logger name") {
        val name = LoggerFactory.generateName(LoggerFactoryTests::class)
        name shouldBe "n.b.c.l.LoggerFactoryTests"
    }

    context("Extension function") {

        test("Create logger property") {
            class TestClass {
                val logger = logger()
            }

            TestClass().logger.shouldBeInstanceOf<SimpleLogger>()
        }
    }
})