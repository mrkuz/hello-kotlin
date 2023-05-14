package net.bnb1.commons.logging

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import java.io.PrintStream
import java.io.PrintWriter
import java.io.StringWriter
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@OptIn(DelicateCoroutinesApi::class)
private val scope = CoroutineScope(newSingleThreadContext("logger"))

const val MAX_LEVEL_CHARS = 5

/**
 * Simple logger implementation printing the messages to `stdout`.
 *
 * Pattern used: `%date{HH:mm:ss.SSS} [%thread] %-5level %logger - %message`
 */
class SimpleLogger(
    private val name: String,
    override val threshold: LogLevel = LogLevel.DEBUG,
    private val output: PrintStream = System.out
) : Logger {

    private val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS")

    override fun debug(message: () -> String) {
        if (isBelowThreshold(LogLevel.DEBUG)) return
        val thread = Thread.currentThread().name
        scope.launch { log(thread, LogLevel.DEBUG, message()) }
    }

    override fun debug(message: String) {
        if (isBelowThreshold(LogLevel.DEBUG)) return
        log(Thread.currentThread().name, LogLevel.DEBUG, message)
    }

    override fun info(message: () -> String) {
        if (isBelowThreshold(LogLevel.INFO)) return
        val thread = Thread.currentThread().name
        scope.launch { log(thread, LogLevel.INFO, message()) }
    }

    override fun info(message: String) {
        if (isBelowThreshold(LogLevel.INFO)) return
        log(Thread.currentThread().name, LogLevel.INFO, message)
    }

    override fun warn(message: () -> String) {
        if (isBelowThreshold(LogLevel.WARN)) return
        val thread = Thread.currentThread().name
        scope.launch { log(thread, LogLevel.WARN, message()) }
    }

    override fun warn(message: String) {
        if (isBelowThreshold(LogLevel.WARN)) return
        log(Thread.currentThread().name, LogLevel.WARN, message)
    }

    fun error(message: () -> String) {
        if (isBelowThreshold(LogLevel.ERROR)) return
        val thread = Thread.currentThread().name
        scope.launch { log(thread, LogLevel.ERROR, message(), null) }
    }

    override fun error(message: () -> String, throwable: Throwable?) {
        if (isBelowThreshold(LogLevel.ERROR)) return
        val thread = Thread.currentThread().name
        scope.launch { log(thread, LogLevel.ERROR, message(), throwable) }
    }

    override fun error(message: String, throwable: Throwable?) {
        if (isBelowThreshold(LogLevel.ERROR)) return
        log(Thread.currentThread().name, LogLevel.ERROR, message, throwable)
    }

    private fun isBelowThreshold(level: LogLevel): Boolean {
        return level < threshold
    }

    private fun log(thread: String, level: LogLevel, message: String, throwable: Throwable? = null) {
        val line = buildString {
            append(ZonedDateTime.now().format(formatter))
            append(" [")
            append(thread)
            append("] ${level.name.padEnd(MAX_LEVEL_CHARS)} ")
            append(name)
            append(" - ")
            append(message)

            throwable?.let {
                val writer = StringWriter()
                it.printStackTrace(PrintWriter(writer))
                appendLine()
                append(writer.toString())
            }
        }
        output.println(line)
    }
}
