package net.bnb1.commons.lifecycle

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class LifecycleSupportTests : FunSpec({

    isolationMode = IsolationMode.InstancePerTest

    val support = LifecycleSupport("Test component")
    afterEach {
        if (support.isRunning()) {
            support.stop {}
        }
    }

    context("Component stopped") {

        test("Start component") {
            support.isRunning() shouldBe false
            support.start {}
            support.isRunning() shouldBe true
        }

        test("Start component multiple times") {
            support.start {}
            shouldThrow<LifecycleException> { support.start {} }
        }

        test("Stop component") {
            shouldThrow<LifecycleException> { support.stop {} }
        }
    }

    context("Component started") {

        support.start {}

        test("Stop component") {
            support.isRunning() shouldBe true
            support.stop {}
            support.isRunning() shouldBe false
        }
    }
})