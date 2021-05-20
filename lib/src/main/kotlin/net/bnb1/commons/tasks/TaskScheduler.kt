package net.bnb1.commons.tasks

import kotlinx.coroutines.*
import net.bnb1.commons.lifecycle.LifecycleComponent
import net.bnb1.commons.lifecycle.LifecycleSupport
import java.util.*
import kotlin.concurrent.schedule

/**
 * Simple task scheduler.
 *
 * The scheduler needs to be [started][start].
 */
@OptIn(ExperimentalCoroutinesApi::class, ObsoleteCoroutinesApi::class)
class TaskScheduler(dispatcher: CoroutineDispatcher = newSingleThreadContext("task")) :
    LifecycleComponent {

    private val scope = CoroutineScope(dispatcher)
    private val support = LifecycleSupport(this)
    private val timer = Timer("scheduler", true)
    private val tasks = mutableListOf<TaskDefinition>()

    override val running by support

    override fun name() = "Task scheduler"

    /**
     * Starts the scheduler and schedules all defined tasks.
     */
    override fun start() = support.start {
        tasks.forEach {
            timer.schedule(it.delay, it.period) {
                scope.launch {
                    it.action.run()
                }
            }
        }
    }

    /**
     * Stops the scheduler.
     */
    override fun stop() = support.stop {
        timer.cancel()
        scope.cancel()
    }

    /**
     * Adds a new [task][definition] to be scheduled.
     */
    fun add(definition: TaskDefinition) {
        tasks.add(definition)
        if (running) {
            timer.schedule(definition.delay, definition.period) {
                scope.launch {
                    definition.action.run()
                }
            }
        }
    }
}
