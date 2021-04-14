package net.bnb1.commons.monitor

import net.bnb1.commons.properties.ApplicationProperties
import net.bnb1.commons.properties.BuildProperties
import net.bnb1.commons.utils.Bytes
import net.bnb1.commons.utils.Environment
import net.bnb1.commons.utils.UTC
import java.lang.management.BufferPoolMXBean
import java.lang.management.ManagementFactory
import java.lang.management.MemoryPoolMXBean
import java.lang.management.MemoryUsage
import java.time.Instant
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

/**
 * Provides access to application details in human-readable format.
 */
class ApplicationInfo(
    private val applicationProperties: ApplicationProperties,
    private val buildProperties: BuildProperties
) {

    private val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")

    /**
     * Returns following application details:
     *
     * - Build information (version, timestamp, commit id)
     * - Active profile
     * - Kotlin version
     * - JVM vendor and version
     * - JVM arguments
     * - PID
     * - Number of threads
     * - Loaded classes
     * - Heap and non-heap details
     * - Memory pools
     * - Buffer pools
     */
    fun full(): String {
        val runtimeBean = ManagementFactory.getRuntimeMXBean()
        val timestamp = ZonedDateTime
            .ofInstant(Instant.ofEpochSecond(buildProperties.timestamp), UTC.zone())
            .format(formatter)
        val uptime = "${runtimeBean.uptime / 1000}.${runtimeBean.uptime % 1000}s"
        return """
                |Application started: ${applicationProperties.application.name} ($uptime)
                |
                |Version: ${buildProperties.version} (${buildProperties.git.commitId}, $timestamp UTC)
                |Profile: ${Environment.getActiveProfile()}
                |
                |JVM: ${runtimeBean.vmVendor} ${runtimeBean.vmName} (${runtimeBean.vmVersion})
                |Kotlin: ${KotlinVersion.CURRENT}
                |PID: ${runtimeBean.pid}
                |Arguments: ${runtimeBean.inputArguments.joinToString(" ")}
                |
                |${threads()}
                |${classes()}
                |
                |${heap()}
                |${nonHeap()}
                |
                ${memoryPools("|")}
                |
                ${bufferPools("|")}
                |
            """.trimMargin("|")
    }

    /**
     * Returns number of loaded classes.
     */
    fun classes(): String {
        val classLoaderBean = ManagementFactory.getClassLoadingMXBean()
        return "Classes loaded: ${classLoaderBean.loadedClassCount}"
    }

    /**
     * Returns number of current threads.
     */
    fun threads(): String {
        val threadBean = ManagementFactory.getThreadMXBean()
        return "Threads: ${threadBean.threadCount} (${threadBean.daemonThreadCount} daemon)"
    }

    /**
     * Returns current heap usage and limit.
     */
    fun heap(): String {
        val memoryBean = ManagementFactory.getMemoryMXBean()
        val heap = memoryBean.heapMemoryUsage
        return "Heap: ${Bytes.prettyPrint(heap.used)} / ${max(heap)}"
    }

    /**
     * Returns current non-heap usage and limit.
     */
    fun nonHeap(): String {
        val memoryBean = ManagementFactory.getMemoryMXBean()
        val nonHeap = memoryBean.nonHeapMemoryUsage
        return "Non-heap: ${Bytes.prettyPrint(nonHeap.used)} / ${max(nonHeap)}"
    }

    /**
     * Returns list of available memory pools, their usage and limits.
     */
    fun memoryPools(margin: String = ""): String {
        val memoryPoolBeans = ManagementFactory.getMemoryPoolMXBeans()
        return buildString {
            memoryPoolBeans
                .sortedBy(MemoryPoolMXBean::getName)
                .sortedBy(MemoryPoolMXBean::getType)
                .withIndex()
                .forEach {
                    if (it.index > 0) {
                        appendLine()
                    }
                    append(margin)
                    append(it.value.name)
                    append(": ")
                    append(Bytes.prettyPrint(it.value.usage.used))
                    append(" / ")
                    append(max(it.value.usage))
                }
        }
    }

    /**
     * Returns list of available memory pools, their usage and limits.
     */
    fun bufferPools(margin: String = ""): String {
        val bufferPoolBeans = ManagementFactory.getPlatformMXBeans(BufferPoolMXBean::class.java)
        return buildString {
            bufferPoolBeans.sortedBy(BufferPoolMXBean::getName)
                .withIndex()
                .forEach {
                    if (it.index > 0) {
                        appendLine()
                    }
                    append(margin)
                    append("Buffer ")
                    append(it.value.name)
                    append(": ")
                    append(Bytes.prettyPrint(it.value.memoryUsed))
                }
        }
    }

    private fun max(usage: MemoryUsage): String {
        return if (usage.max <= 0) "unlimited" else Bytes.prettyPrint(usage.max)
    }
}