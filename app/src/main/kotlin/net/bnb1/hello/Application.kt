package net.bnb1.hello

import net.bnb1.commons.beans.ReadyEvent
import net.bnb1.commons.beans.ShutdownEvent
import net.bnb1.commons.beans.start
import net.bnb1.commons.event.EventBus
import net.bnb1.commons.http.HttpServer
import net.bnb1.commons.http.MediaType
import net.bnb1.commons.logging.LogLevel
import net.bnb1.commons.logging.LoggerFactory
import net.bnb1.commons.monitor.ApplicationInfo
import net.bnb1.commons.monitor.Health
import net.bnb1.commons.monitor.HealthStatus
import net.bnb1.commons.properties.ApplicationProperties
import net.bnb1.commons.properties.BuildProperties
import net.bnb1.commons.tasks.TaskScheduler
import net.bnb1.commons.utils.Resources
import net.bnb1.commons.utils.toJson

const val PRINT_HEAP_INFO_SCHEDULE_MS = 30_000L

fun main() {
    val applicationContext = start {
        // Beans
        single { LoggerFactory.create("n.b.o.Application") }
        single { Resources.loadConf<ApplicationProperties>("/application.conf") }
        single { Resources.loadProperties<BuildProperties>("/build.properties") }
        single { ApplicationInfo(await<ApplicationProperties>(), await<BuildProperties>()) }
        single { EventBus() }
        single { TaskScheduler() }
        single(autoStart = true) {
            LoggerFactory.setThreshold(
                "n.b.c.h.HttpServer",
                when (profile) {
                    "dev" -> LogLevel.DEBUG
                    else -> LogLevel.OFF
                }
            )

            HttpServer().apply {
                get("/actuator/health") {
                    it.copy(
                        contentType = MediaType.APPLICATION_JSON,
                        body = Health(HealthStatus.UP).toJson()
                    )
                }
                get("/actuator/info") { it.copy(body = get<ApplicationInfo>().full()) }
                post("/actuator/gc") {
                    System.gc()
                    logger.debug { get<ApplicationInfo>().heap() }
                    it
                }
                post("/actuator/shutdown") {
                    this@start.stop()
                    it
                }
            }
        }
        // Events
        event<ReadyEvent> { logger.debug { get<ApplicationInfo>().full() } }
        event<ShutdownEvent> { logger.debug("Shutting down") }

        // Tasks
        schedule(0L, PRINT_HEAP_INFO_SCHEDULE_MS) { logger.debug { get<ApplicationInfo>().heap() } }
    }

    applicationContext.block()
}
