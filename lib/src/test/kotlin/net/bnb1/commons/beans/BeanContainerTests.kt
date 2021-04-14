package net.bnb1.commons.beans

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import net.bnb1.commons.lifecycle.LifecycleException
import net.bnb1.commons.test.Cat

class BeanContainerTests : FunSpec({

    isolationMode = IsolationMode.InstancePerTest

    val container = BeanContainer("Test container", "test")
    var counter = 0

    afterEach {
        if (container.running) {
            container.stop()
        }
    }

    test("Get active profile") {
        container.activeProfile shouldBe "test"
    }

    context("Container stopped") {

        test("Start container") {
            container.running shouldBe false
            container.start()
            container.running shouldBe true
        }

        test("Start container multiple times") {
            container.start()
            shouldThrow<LifecycleException> { container.start() }
        }

        test("Stop container") {
            shouldThrow<LifecycleException> { container.stop() }
        }
    }

    context("Container started") {

        container.start()

        test("Stop container") {
            container.stop()
            container.running shouldBe false
        }

        test("Try to register singleton") {
            shouldThrow<BeanException> { container.single(Cat::class) { Cat() } }
        }

        test("Try to invoke function") {
            shouldThrow<BeanException> { container.exec { counter++ } }
        }
    }

    context("BeanContainer.single") {

        test("Register singleton without profile") {
            container.single(Cat::class) { Cat() }
            container.has(Cat::class) shouldBe true
        }

        test("Register singleton with test profile") {
            container.single(Cat::class, profile = "test") { Cat() }
            container.has(Cat::class) shouldBe true
        }

        test("Register singleton with wrong profile") {
            container.single(Cat::class, profile = "prod") { Cat() }
            container.has(Cat::class) shouldBe false
        }

        test("Register multiple beans of same type") {
            container.single(Cat::class) { Cat() }
            shouldThrow<BeanException> { container.single(Cat::class) { Cat() } }
        }

        test("Override bean") {
            val cat1 = Cat()
            val cat2 = Cat()
            container.single(Cat::class) { cat1 }
            container.single(Cat::class, override = true) { cat2 }
            container.start()
            container[Cat::class] shouldBeSameInstanceAs cat2
        }
    }

    context("BeanContainer.exec") {

        test("Invoke function without profile") {
            container.exec { counter++ }
            counter shouldBe 0
            container.start()
            counter shouldBe 1
        }

        test("Invoke function with test profile") {
            container.exec(profile = "test") { counter++ }
            counter shouldBe 0
            container.start()
            counter shouldBe 1
        }

        test("Invoke function with wrong profile") {
            container.exec(profile = "prod") { counter++ }
            counter shouldBe 0
            container.start()
            counter shouldBe 0
        }
    }

    context("BeanContainer.get") {

        test("Try get bean before container starts") {
            container.single(Cat::class) { Cat() }
            shouldThrow<BeanException> { container[Cat::class] }
        }

        test("Get bean after container started") {
            val cat = Cat()
            container.single(Cat::class) { cat }
            container.start()
            container[Cat::class] shouldBeSameInstanceAs cat
        }

        test("Bean not found") {
            container.start()
            shouldThrow<BeanException> { container[Cat::class] }
        }
    }

    context("BeanContainer.all") {

        test("Try get all beans before container starts") {
            container.single(Cat::class) { Cat() }
            shouldThrow<BeanException> { container.all() }
        }

        test("Get all beans after container started") {
            container.single(Cat::class) { Cat() }
            container.start()
            container.all().size shouldBe 1
        }
    }

    context("BeanContainer.await") {

        test("Call await before container starts") {
            val cat = Cat()
            container.single(Cat::class) { cat }
            container.await(Cat::class) shouldBeSameInstanceAs cat
        }

        test("Call await after container started") {
            val cat = Cat()
            container.single(Cat::class) { cat }
            container.start()
            container.await(Cat::class) shouldBeSameInstanceAs cat
        }

        test("Bean not found") {
            container.start()
            shouldThrow<BeanException> { container.await(Cat::class) }
        }
    }

    test("Fail on start") {
        container.exec { throw Exception("Fail") }
        shouldThrow<LifecycleException> { container.start() }
    }
})
