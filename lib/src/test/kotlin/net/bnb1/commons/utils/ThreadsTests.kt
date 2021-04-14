package net.bnb1.commons.utils

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import java.util.concurrent.ThreadFactory

class ThreadsTests : FunSpec({

    context("Threads.newFactory") {
        var factory: ThreadFactory? = null;

        test("Create new factory") {
            factory = Threads.newFactory("test")
            factory shouldNotBe null
        }

        test("Use factory to create thread") {
            val thread = factory?.newThread {}!!;
            thread.isDaemon shouldBe true
            thread.name shouldBe "test"
        }
    }
})
