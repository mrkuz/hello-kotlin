package net.bnb1.commons.utils

import net.bnb1.commons.logging.LoggerFactory

/**
 * Simple stopwatch logging with DEBUG level.
 */
class Stopwatch(val name: String, autoStart: Boolean = true) {

    private val logger = LoggerFactory.create(LoggerFactory.generateName(Stopwatch::class) + ":$name")
    private var started = 0L
    private var checkpoint = 0L

    init {
        if (autoStart) {
            start()
        }
    }

    /**
     * Start stopwatch.
     */
    fun start() {
        logger.debug("Started")
        started = System.currentTimeMillis()
        checkpoint = started
    }

    /**
     * Return and log elapsed time.
     */
    fun elapsed(message: String = "Elapsed"): Int {
        val lastCheckpoint = checkpoint
        checkpoint = System.currentTimeMillis()
        val sinceStart = if (started == 0L) 0L else checkpoint - started
        val sinceLastCheckpoint = if (lastCheckpoint == 0L) 0L else checkpoint - lastCheckpoint
        logger.debug("$message: ${sinceStart}ms (${sinceLastCheckpoint}ms)")
        return sinceStart.toInt()
    }

    /**
     * Reset stop watch
     */
    fun reset() {
        logger.debug("Reset")
        started = System.currentTimeMillis()
        checkpoint = started
    }
}
