package net.bnb1.commons.beans

import kotlinx.coroutines.runBlocking
import net.bnb1.commons.event.EventBus
import net.bnb1.commons.event.EventListener
import net.bnb1.commons.lifecycle.LifecycleComponent
import net.bnb1.commons.lifecycle.LifecycleException
import net.bnb1.commons.lifecycle.LifecycleSupport
import net.bnb1.commons.logging.Logger
import net.bnb1.commons.tasks.TaskDefinition
import net.bnb1.commons.tasks.TaskScheduler
import java.util.concurrent.CountDownLatch

/**
 * Used to simplify application setup.
 */
class ApplicationContext(val beanContainer: BeanContainer) : LifecycleComponent {

    private val support = LifecycleSupport(this)
    private val latch = CountDownLatch(1)

    override val running by support

    override fun name(): String = "Application context"

    /**
     * Starts the application context.
     *
     * Publishes [ReadyEvent]
     */
    override fun start() = support.start {
        beanContainer.start()
        if (beanContainer.has(EventBus::class)) {
            beanContainer[EventBus::class].publish(ReadyEvent)
        }
        if (beanContainer.has(TaskScheduler::class)) {
            beanContainer[TaskScheduler::class].start()
        }
    }

    /**
     * Stops the application context.
     *
     * Publishes [ShutdownEvent]
     */
    override fun stop() = support.stop {
        if (beanContainer.has(TaskScheduler::class)) {
            beanContainer[TaskScheduler::class].stop()
        }
        if (beanContainer.has(EventBus::class)) {
            beanContainer[EventBus::class].publish(ShutdownEvent)
        }

        beanContainer.all().forEach {
            if (it is LifecycleComponent && it.running) {
                it.stop()
            }
        }

        latch.countDown()
    }

    /**
     * Returns active profile.
     */
    val profile: String
        get() = beanContainer.activeProfile

    /**
     * Returns logger.
     *
     * Throws an [Exception] if no [Logger] bean was registered.
     */
    val logger: Logger
        get() = runBlocking {
            beanContainer.await(Logger::class)
        }

    /**
     * Invokes [runnable] if [profile] matches the active profile or is `null`.
     */
    fun exec(profile: String? = null, runnable: Runnable) {
        beanContainer.exec(profile, runnable)
    }

    /**
     * Registers singleton if [profile] matches the active profile or is `null`.
     *
     * If registering a [LifecycleComponent], [autoStart] can be set to start the component on creation.
     */
    inline fun <reified T : Any> single(
        profile: String? = null,
        override: Boolean = false,
        autoStart: Boolean = false,
        noinline supplier: suspend () -> T
    ) {
        beanContainer.single(T::class, profile, override) {
            val bean = supplier()
            if (autoStart && (bean is LifecycleComponent)) {
                bean.start()
            }
            bean
        }
    }

    /**
     * Waits until bean of type [T] is created and returns the instance.
     *
     * Throws an [Exception] if no bean of type is registered.
     */
    suspend inline fun <reified T : Any> await(): T {
        return beanContainer.await(T::class)
    }

    /**
     * Returns bean of type [T].
     *
     * Throws an [Exception] if no bean of type is registered or context is not running.
     */
    inline fun <reified T : Any> get(): T {
        return beanContainer[T::class]
    }

    /**
     * Subscribes [listener] for an specific event of type [T].
     *
     * Throws an [Exception] if no [EventBus] bean was registered.
     */
    suspend inline fun <reified T : Any> event(listener: EventListener<T>) {
        beanContainer.await(EventBus::class).subscribe(T::class, listener)
    }

    /**
     * Schedules a [action] for execution.
     *
     * Throws an [Exception] if no [TaskScheduler] bean was registered.
     */
    suspend fun schedule(delay: Long, period: Long, action: Runnable) {
        beanContainer.await(TaskScheduler::class).add(TaskDefinition(delay, period, action))
    }

    /**
     * Blocks until [stop] is called.
     */
    fun block() {
        if (!running) {
            throw LifecycleException("Container not started")
        }
        latch.await()
    }

    /**
     * Creates and starts a new [ApplicationContext].
     */
    companion object {
        fun start(initialize: suspend ApplicationContext.() -> Unit): ApplicationContext {
            val beanContainer = BeanContainer("Application context")
            val applicationContext = ApplicationContext(beanContainer)
            runBlocking { initialize(applicationContext) }
            applicationContext.start()
            return applicationContext
        }
    }
}

/**
 * Creates and starts a new [ApplicationContext].
 */
fun start(initialize: suspend ApplicationContext.() -> Unit): ApplicationContext = ApplicationContext.start {
    initialize(this)
}

/**
 * Notifies the start of an [ApplicationContext].
 */
object ReadyEvent

/**
 * Notifies the shutdown of an [ApplicationContext].
 */
object ShutdownEvent
