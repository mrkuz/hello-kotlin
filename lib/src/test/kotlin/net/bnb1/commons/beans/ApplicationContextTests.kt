package net.bnb1.commons.beans

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.assertions.timing.continually
import io.kotest.assertions.timing.eventually
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import net.bnb1.commons.event.EventBus
import net.bnb1.commons.lifecycle.LifecycleComponent
import net.bnb1.commons.lifecycle.LifecycleException
import net.bnb1.commons.logging.SimpleLogger
import net.bnb1.commons.tasks.TaskScheduler
import net.bnb1.commons.test.Cat
import net.bnb1.commons.test.TestEvent
import net.bnb1.commons.utils.milliseconds
import kotlin.time.ExperimentalTime

@OptIn(DelicateCoroutinesApi::class)
@ExperimentalTime
class ApplicationContextTests : FunSpec({

    isolationMode = IsolationMode.InstancePerTest

    val container = BeanContainer("Test container", "test")
    val context = ApplicationContext(container)
    var counter = 0

    afterEach {
        if (context.running) {
            context.stop()
        }
    }

    test("Get active profile") {
        context.profile shouldBe "test"
    }

    context("Context stopped") {

        test("Start context") {
            context.running shouldBe false
            context.start()
            context.running shouldBe true
        }

        test("Start context multiple times") {
            context.start()
            shouldThrow<LifecycleException> { context.start() }
        }

        test("Stop context") {
            shouldThrow<LifecycleException> { context.stop() }
        }
    }

    context("Context started") {

        context.start()

        test("Stop context") {
            context.stop()
            context.running shouldBe false
        }

        test("Try to register singleton") {
            shouldThrow<BeanException> { context.single { Cat() } }
        }

        test("Try to invoke function") {
            shouldThrow<BeanException> { context.exec { counter++ } }
        }
    }

    context("ApplicationContext.single") {

        test("Register singleton without profile") {
            context.single { Cat() }
            container.has(Cat::class) shouldBe true
        }

        test("Register singleton with test profile") {
            context.single(profile = "test") { Cat() }
            container.has(Cat::class) shouldBe true
        }

        test("Register singleton with wrong profile") {
            context.single(profile = "prod") { Cat() }
            container.has(Cat::class) shouldBe false
        }

        test("Register multiple beans of same type") {
            context.single { Cat() }
            shouldThrow<BeanException> { context.single { Cat() } }
        }

        test("Override bean") {
            val cat1 = Cat()
            val cat2 = Cat()
            context.single { cat1 }
            context.single(override = true) { cat2 }
            context.start()
            context.get<Cat>() shouldBeSameInstanceAs cat2
        }
    }

    context("ApplicationContext.exec") {

        test("Invoke function without profile") {
            context.exec { counter++ }
            counter shouldBe 0
            context.start()
            counter shouldBe 1
        }

        test("Invoke function with test profile") {
            context.exec(profile = "test") { counter++ }
            counter shouldBe 0
            context.start()
            counter shouldBe 1
        }

        test("Invoke function with wrong profile") {
            context.exec(profile = "prod") { counter++ }
            counter shouldBe 0
            context.start()
            counter shouldBe 0
        }
    }

    context("ApplicationContext.get") {

        test("Try get bean before context starts") {
            context.single { Cat() }
            shouldThrow<BeanException> { context.get<Cat>() }
        }

        test("Get bean after context started") {
            val cat = Cat()
            context.single { cat }
            context.start()
            context.get<Cat>() shouldBeSameInstanceAs cat
        }

        test("Bean not found") {
            context.start()
            shouldThrow<BeanException> { context.get<Cat>() }
        }
    }

    context("ApplicationContext.await") {

        test("Call await before context starts") {
            val cat = Cat()
            context.single { cat }
            context.await<Cat>() shouldBeSameInstanceAs cat
        }

        test("Call await after context started") {
            val cat = Cat()
            context.single { cat }
            context.start()
            context.await<Cat>() shouldBeSameInstanceAs cat
        }

        test("Bean not found") {
            context.start()
            shouldThrow<BeanException> { context.await<Cat>() }
        }
    }

    context("Lifecycle beans") {

        test("Lifecycle bean without autostart") {
            val component = CountingComponent()
            context.single(autoStart = false) { component }
            component.startCounter shouldBe 0
            context.start()
            component.startCounter shouldBe 0
        }

        test("Lifecycle bean with autostart") {
            val component = CountingComponent()
            context.single(autoStart = true) { component }
            component.startCounter shouldBe 0
            context.start()
            component.startCounter shouldBe 1
        }

        test("Shut down") {
            val component = CountingComponent()
            context.single(autoStart = true) { component }
            component.stopCounter shouldBe 0
            context.start()
            context.stop()
            component.stopCounter shouldBe 1
        }
    }

    context("Events") {

        test("Try to subscribe without event bus provided") {
            shouldThrow<BeanException> { context.event<TestEvent> {} }
        }

        test("Subscribe for event") {
            context.single { EventBus() }
            context.event<TestEvent> { counter++ }
            context.start()
            context.get<EventBus>().publish(TestEvent())
            eventually(100.milliseconds) { counter shouldBe 1 }
        }

        test("Subscribe to application events") {
            context.single { EventBus() }
            context.event<ReadyEvent> { counter++ }
            context.event<ShutdownEvent> { counter++ }
            context.start()
            eventually(100.milliseconds) { counter shouldBe 1 }
            context.stop()
            eventually(100.milliseconds) { counter shouldBe 2 }
        }
    }

    context("Scheduling") {

        test("Try to schedule task without scheduler provided") {
            shouldThrow<BeanException> { context.schedule(0, 100) {} }
        }

        test("Schedule task") {
            context.single { TaskScheduler() }
            context.schedule(0, 100) { counter++ }
            counter shouldBe 0
            context.start()
            eventually(50.milliseconds) { counter shouldBe 1 }
            eventually(150.milliseconds) { counter shouldBe 2 }
        }

        test("Schedule task and stop context") {
            context.single { TaskScheduler() }
            context.schedule(100, 100) { counter++ }
            context.start()
            context.stop()
            continually(150.milliseconds) { counter shouldBe 0 }
        }
    }

    context("Logging") {

        test("Try to access logger without provided") {
            shouldThrow<BeanException> { context.logger }
        }

        test("Get logger") {
            context.single { SimpleLogger("Test") }
            context.logger shouldNotBe null
        }
    }

    test("Fail on start") {
        context.exec { throw Exception("Fail") }
        shouldThrow<LifecycleException> { context.start() }
    }

    test("Start and block") {
        context.start()
        val deferred = GlobalScope.async {
            context.block()
            counter++
        }
        counter shouldBe 0
        eventually(100.milliseconds) { context.running shouldBe true }
        context.stop()
        deferred.await()
        counter shouldBe 1
    }

    test("Start context using companion object") {
        val newContext = ApplicationContext.start {
            single { Cat() }
        }
        newContext.running shouldBe true
        newContext.get<Cat>() shouldNotBe null
    }

    test("Start context using DSL") {
        val newContext = start {
            single { Cat() }
        }
        newContext.running shouldBe true
        newContext.get<Cat>() shouldNotBe null
    }
})

class CountingComponent : LifecycleComponent {

    private var _startCounter = 0
    val startCounter
        get() = _startCounter

    private var _stopCounter = 0
    val stopCounter
        get() = _stopCounter

    override val running: Boolean
        get() = _startCounter > 0 && _stopCounter == 0

    override fun name() = "Test component"

    override fun start() {
        _startCounter++
    }

    override fun stop() {
        _stopCounter++
    }
}
