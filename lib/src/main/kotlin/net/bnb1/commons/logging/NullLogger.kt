package net.bnb1.commons.logging

object NullLogger : Logger {

    override val threshold: LogLevel
        get() = LogLevel.OFF

    override fun debug(message: () -> String) = Unit
    override fun debug(message: String) = Unit
    override fun info(message: () -> String) = Unit
    override fun info(message: String) = Unit
    override fun warn(message: () -> String) = Unit
    override fun warn(message: String) = Unit
    override fun error(message: () -> String, throwable: Throwable?) = Unit
    override fun error(message: String, throwable: Throwable?) = Unit
}
