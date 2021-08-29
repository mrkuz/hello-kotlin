package net.bnb1.commons.dev

import net.bnb1.commons.logging.logger
import net.bnb1.commons.utils.Environment
import net.bnb1.commons.utils.Threads
import java.math.BigInteger
import java.nio.file.*
import java.security.DigestInputStream
import java.security.MessageDigest
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.system.exitProcess

/**
 * Development mode watches a specified path for file changes.
 * On every change, it stops the application.
 *
 * Can be used with continues builds, to rebuild and restart the application on change.
 *
 * For example Gradle: `gradle run -t`
 */
object DevelopmentMode {

    private val started = AtomicBoolean()
    private val logger = logger()

    private val executor = Executors.newSingleThreadExecutor(Threads.newFactory("dev"))

    private lateinit var patterns: List<Regex>
    private val keys = mutableListOf<WatchKey>()
    private val hashes = mutableMapOf<Path, String>()

    /**
     * Start development mode.
     */
    fun start(
        path: Path = Path.of(Environment.getCurrentWorkingDirectory()),
        patterns: List<Regex> = listOf(Regex(".*\\.kt"))
    ) {
        if (!started.compareAndSet(false, true)) {
            return
        }

        this.patterns = patterns
        val watcher = FileSystems.getDefault().newWatchService()
        registerRecursive(path, watcher)

        logger.debug("Watching: $path")
        executor.submit { watch(watcher) }
    }

    private fun registerRecursive(path: Path, watcher: WatchService) {
        if (!path.toFile().isDirectory) {
            return
        }
        path.toFile().walkTopDown().forEach {
            if (it.isDirectory) {
                val key = it.toPath().register(
                    watcher,
                    StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_DELETE,
                    StandardWatchEventKinds.ENTRY_MODIFY
                )
                keys.add(key)
            } else {
                val file = it.toPath()
                if (shouldHandle(file)) {
                    hashes[file] = md5(file)
                }
            }
        }
    }

    private fun watch(watcher: WatchService) {
        while (true) {
            val key = watcher.take()
            for (event in key.pollEvents()) {
                val kind = event.kind()
                if (kind === StandardWatchEventKinds.OVERFLOW) {
                    continue
                }

                val directory = (key.watchable() as Path)
                val fileName = (event.context() as Path)
                val absolutePath = Path.of(directory.toString(), fileName.toString())
                if (!shouldHandle(absolutePath)) {
                    continue
                }
                if (kind === StandardWatchEventKinds.ENTRY_MODIFY && !compareAndUpdateHash(absolutePath)) {
                    continue
                }

                logger.debug("Shutting down")
                exitProcess(0)
            }
            key.reset()
        }
    }

    private fun shouldHandle(file: Path): Boolean {
        if (patterns.isEmpty()) {
            return true
        }
        return patterns.any { file.toString().matches(it) }
    }

    private fun compareAndUpdateHash(file: Path): Boolean {
        val hash = md5(file)
        if (hash == hashes[file]) {
            return false
        }
        hashes[file] = hash
        return true
    }

    @Suppress("MagicNumber")
    private fun md5(file: Path): String {
        val md5 = MessageDigest.getInstance("MD5")
        file.toFile().inputStream().use { DigestInputStream(it, md5).readAllBytes() }
        return BigInteger(1, md5.digest())
            .toString(16)
            .padStart(32, '0')
    }
}
