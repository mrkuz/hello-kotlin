package net.bnb1.commons.logging

object NullLogger : Logger {

    override val threshold: LogLevel
        get() = LogLevel.OFF

    override fun debug(message: () -> String) {}
    override fun debug(message: String) {}
    override fun info(message: () -> String) {}
    override fun info(message: String) {}
    override fun warn(message: () -> String) {}
    override fun warn(message: String) {}
    override fun error(message: () -> String, throwable: Throwable?) {}
    override fun error(message: String, throwable: Throwable?) {}
}