package net.bnb1.commons.event

import kotlinx.coroutines.*
import kotlin.reflect.KClass

/**
 * Simple event bus.
 */
@OptIn(ExperimentalCoroutinesApi::class, ObsoleteCoroutinesApi::class)
class EventBus(private val dispatcher: CoroutineDispatcher = newSingleThreadContext("event")) {

    private val map = mutableMapOf<KClass<*>, MutableList<EventListener<*>>>()

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
            GlobalScope.launch(dispatcher) { (it as EventListener<T>).onEvent(event) }
        }
    }
}
