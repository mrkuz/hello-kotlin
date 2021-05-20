package net.bnb1.commons.tasks

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.assertions.timing.continually
import io.kotest.assertions.timing.eventually
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import net.bnb1.commons.lifecycle.LifecycleException
import net.bnb1.commons.utils.milliseconds
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class TaskSchedulerTests : FunSpec({

    isolationMode = IsolationMode.InstancePerTest

    var counter = 0
    val taskScheduler = TaskScheduler()
    afterEach {
        if (taskScheduler.running) {
            taskScheduler.stop()
        }
    }

    context("Scheduler stopped") {

        test("Start scheduler") {
            taskScheduler.running shouldBe false
            taskScheduler.start()
            taskScheduler.running shouldBe true
        }

        test("Start scheduler multiple times") {
            taskScheduler.start()
            shouldThrow<LifecycleException> { taskScheduler.start() }
        }

        test("Stop scheduler") {
            shouldThrow<LifecycleException> { taskScheduler.stop() }
        }

        test("Add task and start scheduler") {
            taskScheduler.add(TaskDefinition(0, 10_000) { counter++ })
            taskScheduler.start()
            eventually(100.milliseconds) { counter shouldBe 1 }
        }

        test("Start scheduler and add task") {
            taskScheduler.start()
            taskScheduler.add(TaskDefinition(0, 10_000) { counter++ })
            eventually(100.milliseconds) { counter shouldBe 1 }
        }
    }

    context("Scheduler started") {

        taskScheduler.start()

        test("Stop scheduler") {
            taskScheduler.stop()
            taskScheduler.running shouldBe false
        }

        test("Add task") {
            taskScheduler.add(TaskDefinition(0, 10_000) { counter++ })
            eventually(100.milliseconds) { counter shouldBe 1 }
        }

        test("Add delayed task") {
            taskScheduler.add(TaskDefinition(100, 10_000) { counter++ })
            continually(90.milliseconds) { counter shouldBe 0 }
            eventually(100.milliseconds) { counter shouldBe 1 }
        }

        test("Run task multiple times") {
            taskScheduler.add(TaskDefinition(0, 100) { counter++ })
            eventually(50.milliseconds) { counter shouldBe 1 }
            eventually(150.milliseconds) { counter shouldBe 2 }
            eventually(250.milliseconds) { counter shouldBe 3 }
        }

        test("Run task and stop") {
            taskScheduler.add(TaskDefinition(0, 100) { counter++ })
            eventually(50.milliseconds) { counter shouldBe 1 }
            taskScheduler.stop()
            continually(150.milliseconds) { counter shouldBe 1 }
        }
    }
})
