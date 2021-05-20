package net.bnb1.commons.event

import kotlinx.coroutines.*
import net.bnb1.commons.lifecycle.LifecycleComponent
import net.bnb1.commons.lifecycle.LifecycleSupport
import kotlin.reflect.KClass

/**
 * Simple event bus.
 */
@OptIn(ExperimentalCoroutinesApi::class, ObsoleteCoroutinesApi::class)
class EventBus(dispatcher: CoroutineDispatcher = newSingleThreadContext("event")) : LifecycleComponent {

    private val scope = CoroutineScope(dispatcher)
    private val support = LifecycleSupport(this)
    private val map = mutableMapOf<KClass<*>, MutableList<EventListener<*>>>()

    override val running by support

    override fun name(): String = "Event bus"
    override fun start() = support.start {}
    override fun stop() = support.stop { scope.cancel() }

    /**
     * Subscribes [listener] for an event of type [clazz].
     */
    fun <T : Any> subscribe(clazz: KClass<T>, listener: EventListener<T>) {
        if (clazz !in map) {
            map[clazz] = mutableListOf()
        }
        map[clazz]?.add(listener)
    }

    /**
     * Publishes an event.
     */
    @Suppress("UNCHECKED_CAST")
    fun <T : Any> publish(event: T) {
        map[event::class]?.forEach {
            scope.launch { (it as EventListener<T>).onEvent(event) }
        }
    }
}
