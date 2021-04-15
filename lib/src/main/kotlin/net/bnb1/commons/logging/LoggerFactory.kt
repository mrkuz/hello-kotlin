package net.bnb1.commons.logging

import java.io.PrintStream
import kotlin.reflect.KClass

/**
 * Factory used to create logger instances.
 */
object LoggerFactory {

    private val thresholds: MutableMap<String, LogLevel> = mutableMapOf()

    /**
     * Set threshold for logger.
     *
     * This method has to be called before creating a new logger.
     * Thresholds of existing loggers won't be changed
     */
    fun setThreshold(name: String, threshold: LogLevel) {
        thresholds[name] = threshold
    }

    /**
     * Creates new logger.
     */
    fun create(name: String, output: PrintStream = System.out): Logger {
        val threshold = thresholds.getOrDefault(name, LogLevel.DEBUG)
        if (threshold == LogLevel.OFF) {
            return NullLogger
        }
        return SimpleLogger(name, threshold, output)
    }

    /**
     * Generate logger name based on class.
     */
    fun generateName(clazz: KClass<*>): String {
        return clazz.java.packageName.split(".")
            .asSequence()
            .filter { it.isNotEmpty() }
            .map { it[0] }
            .joinToString(separator = ".") { it.toString() } + "." + clazz.simpleName
    }
}

/**
 * Extension function which creates a logger named based on the receiver class.
 *
 * Example: `com.demo.Application` will become `c.d.Application`
 */
@Suppress("unused")
inline fun <reified T> T.logger(
    output: PrintStream = System.out
): Logger {
    val name = LoggerFactory.generateName(T::class)
    return LoggerFactory.create(name, output)
}
