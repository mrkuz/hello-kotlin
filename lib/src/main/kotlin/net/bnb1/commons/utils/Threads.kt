package net.bnb1.commons.utils

import java.util.concurrent.ThreadFactory

/**
 * Provide utility functions related to threads.
 */
object Threads {

    /**
     * Creates new [ThreadFactory], using the provided [name] for new threads.
     * The factory always returns daemon threads.
     */
    fun newFactory(name: String): ThreadFactory {
        return ThreadFactory { runnable ->
            val thread = Thread(runnable)
            thread.isDaemon = true
            thread.name = name
            thread
        }
    }
}
