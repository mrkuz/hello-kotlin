package net.bnb1.commons.logging

interface Logger {

    val threshold: LogLevel

    /**
     * Prints debug message.
     */
    fun debug(message: () -> String)

    /**
     * Prints debug message.
     */
    fun debug(message: String)

    /**
     * Prints info message.
     */
    fun info(message: () -> String)

    /**
     * Prints info message.
     */
    fun info(message: String)

    /**
     * Prints warning.
     */
    fun warn(message: () -> String)

    /**
     * Prints warning.
     */
    fun warn(message: String)

    /**
     * Prints error message.
     */
    fun error(message: () -> String, throwable: Throwable? = null)

    /**
     * Prints error message.
     */
    fun error(message: String, throwable: Throwable? = null)
}