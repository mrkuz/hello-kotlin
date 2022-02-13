package net.bnb1.commons.lifecycle

/**
 * Used to represent a component with a lifecycle.
 * This means, they can be started and stopped.
 */
interface LifecycleComponent {

    /**
     * Indicates if the component is running.
     */
    val running: Boolean

    /**
     * Returns the name of the component.
     */
    fun name(): String

    /**
     * Start the component.
     */
    fun start()

    /**
     * Stop the component.
     */
    fun stop()
}
