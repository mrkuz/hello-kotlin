package net.bnb1.commons.beans

import kotlinx.coroutines.*
import net.bnb1.commons.lifecycle.LifecycleComponent
import net.bnb1.commons.lifecycle.LifecycleException
import net.bnb1.commons.lifecycle.LifecycleSupport
import net.bnb1.commons.logging.logger
import net.bnb1.commons.utils.Environment
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.reflect.KClass

/**
 * Inversion of control container.
 */
@OptIn(ExperimentalCoroutinesApi::class, ObsoleteCoroutinesApi::class)
class BeanContainer(
    private val name: String,
    val activeProfile: String = Environment.getActiveProfile(),
    private val dispatcher: CoroutineDispatcher = Dispatchers.Unconfined
) : LifecycleComponent {

    private val logger = logger()
    private val support = LifecycleSupport(this)

    private val factories = mutableMapOf<KClass<*>, BeanFactory<Any>>()
    private val beans = mutableMapOf<KClass<*>, Deferred<Any>>()
    private val runnables = mutableListOf<Runnable>()

    private val failed = AtomicBoolean()

    override val running by support

    override fun name() = name

    /**
     * Starts container, creates all registered beans, resolves dependencies
     * and injects the requested components.
     */
    @Suppress("DeferredResultUnused")
    override fun start() = support.start {
        runBlocking { factories.keys.forEach { resolve(it) } }
        runBlocking {
            runnables.forEach {
                GlobalScope.launch(dispatcher) {
                    try {
                        it.run()
                    } catch (e: Exception) {
                        logger.error("Run failed", e)
                        failed.set(true)
                    }
                }
            }
        }
        runBlocking { factories.keys.forEach { beans[it]?.await() } }

        if (failed.get()) {
            throw LifecycleException("Starting container failed")
        }
    }

    /**
     * Currently no-op.
     */
    override fun stop() = support.stop {
    }

    /**
     * Invokes [runnable] if [profile] matches the active profile or is `null`.
     */
    fun exec(profile: String? = null, runnable: Runnable) {
        if (!checkProfile(profile)) {
            return
        }
        runnables += runnable
    }

    /**
     * Registers singleton if [profile] matches the active profile or is `null`.
     */
    fun <T : Any> single(
        clazz: KClass<T>,
        profile: String? = null,
        override: Boolean = false,
        supplier: BeanFactory<T>
    ) {
        if (!checkProfile(profile)) {
            return
        }
        if (!override && has(clazz)) {
            throw BeanException("Bean of type '${clazz.qualifiedName}' already exists")
        }
        factories[clazz] = supplier
    }

    /**
     * Returns true if container has a bean of type [clazz].
     */
    fun <T : Any> has(clazz: KClass<T>): Boolean {
        return resolve(clazz) != null
    }

    /**
     * Waits until bean of type [clazz] is created and returns the instance.
     *
     * Throws an [Exception] if no bean of type is registered.
     */
    suspend fun <T : Any> await(clazz: KClass<T>): T {
        val deferred = resolve(clazz) ?: throw BeanException("${clazz.simpleName} not available")
        return deferred.await()
    }

    /**
     * Returns bean of type [clazz]
     *
     * Throws an [Exception] if no bean of type is registered or container is not running.
     */
    operator fun <T : Any> get(clazz: KClass<T>): T {
        if (support.isStopped()) {
            throw BeanException("Container is not running")
        }
        val deferred = resolve(clazz) ?: throw BeanException("${clazz.simpleName} not available")
        return deferred.getCompleted()
    }

    /**
     * Returns all containing beans.
     */
    fun all(): List<Any> {
        if (support.isStopped()) {
            throw BeanException("Container is not running")
        }
        return beans.values.map { it.getCompleted() }.toList()
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T : Any> resolve(clazz: KClass<T>): Deferred<T>? {
        val key = factories.keys.find { clazz.java.isAssignableFrom(it.java) } ?: return null
        return beans.computeIfAbsent(key) {
            GlobalScope.async(dispatcher) {
                try {
                    val bean = factories[key]!!.invoke()
                    if (bean === Unit) throw Exception("Unable to create kotlin.Unit")
                    bean
                } catch (e: Exception) {
                    logger.error("Create '${clazz.qualifiedName}' failed", e)
                    failed.set(true)
                }
            }
        } as Deferred<T>
    }

    private fun checkProfile(profile: String?): Boolean {
        if (support.isRunning()) {
            throw BeanException("Container is already running")
        }
        return profile == null || activeProfile == profile
    }
}