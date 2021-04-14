package net.bnb1.commons.event

/**
 * Listener to be called when event [T] occurs.
 */
fun interface EventListener<T> {
    fun onEvent(event: T)
}