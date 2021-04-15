package net.bnb1.commons.utils

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.ints.shouldBeLessThan
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.delay
import kotlin.time.ExperimentalTime

@ExperimentalTime
class StopwatchTests : FunSpec({

    test("Start automatically") {
        val stopwatch = Stopwatch("Test")
        delay(10)
        stopwatch.elapsed() shouldBeGreaterThan 0
        delay(10)
        stopwatch.elapsed() shouldBeGreaterThan 10
    }

    test("Start manually") {
        val stopwatch = Stopwatch("Test", autoStart = false)
        delay(10)
        stopwatch.elapsed() shouldBe 0
        stopwatch.start()
        delay(10)
        stopwatch.elapsed() shouldBeGreaterThan 0
        delay(10)
        stopwatch.elapsed() shouldBeGreaterThan 10
    }

    test("Reset") {
        val stopwatch = Stopwatch("Test")
        delay(60)
        stopwatch.elapsed() shouldBeGreaterThan 50
        stopwatch.reset()
        stopwatch.elapsed() shouldBeLessThan 50
    }
})
