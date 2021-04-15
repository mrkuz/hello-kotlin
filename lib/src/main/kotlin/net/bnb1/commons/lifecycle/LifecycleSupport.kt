package net.bnb1.commons.lifecycle

import java.util.concurrent.atomic.AtomicBoolean
import kotlin.reflect.KProperty

/**
 * Support class for [LifecycleComponent], used for keeping track of the lifecycle state.
 */
class LifecycleSupport(private val componentName: String) {

    private val running = AtomicBoolean()

    constructor(component: LifecycleComponent) : this(component.name())

    /**
     * Invokes [start] and sets running flag.
     */
    fun start(start: () -> Unit) {
        if (!running.compareAndSet(false, true)) {
            throw LifecycleException("$componentName already started")
        }
        start.invoke()
    }

    /**
     * Invokes [stop] and unsets running flag.
     */
    fun stop(stop: () -> Unit) {
        if (!running.compareAndSet(true, false)) {
            throw LifecycleException("$componentName already stopped")
        }
        stop.invoke()
    }

    /**
     * Returns true if the running flag is set.
     */
    fun isRunning() = running.get()

    /**
     * Returns true if the running flag is not set.
     */
    fun isStopped() = !running.get()

    /**
     * Returns true if the running flag is set.
     */
    operator fun getValue(thisRef: Any?, property: KProperty<*>): Boolean {
        return running.get()
    }
}
