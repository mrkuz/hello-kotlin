package net.bnb1.commons.event

import io.kotest.assertions.timing.continually
import io.kotest.assertions.timing.eventually
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import net.bnb1.commons.test.TestEvent
import net.bnb1.commons.utils.milliseconds
import kotlin.time.ExperimentalTime

@ExperimentalTime
class EventBusTests : FunSpec({

    isolationMode = IsolationMode.InstancePerTest

    val eventBus = EventBus()
    var counter = 0

    test("Subscribe once and publish once") {
        eventBus.subscribe(TestEvent::class) { counter++ }
        eventBus.publish(TestEvent())
        eventually(100.milliseconds) { counter shouldBe 1 }
    }

    test("Subscribe multiple times and publish once") {
        for (i in 1..10) {
            eventBus.subscribe(TestEvent::class) { counter++ }
        }
        eventBus.publish(TestEvent())
        eventually(100.milliseconds) { counter shouldBe 10 }
    }

    test("Subscribe once and publish multiple times") {
        eventBus.subscribe(TestEvent::class) { counter++ }
        for (i in 1..10) {
            eventBus.publish(TestEvent())
        }
        eventually(100.milliseconds) { counter shouldBe 10 }
    }

    test("Publish event without subscription") {
        eventBus.subscribe(TestEvent::class) { counter++ }
        eventBus.publish(UnsubscribedEvent())
        continually(100.milliseconds) { counter shouldBe 0 }
    }
})

class UnsubscribedEvent
