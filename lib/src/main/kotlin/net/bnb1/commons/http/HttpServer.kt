package net.bnb1.commons.http

import kotlinx.coroutines.*
import net.bnb1.commons.lifecycle.LifecycleComponent
import net.bnb1.commons.lifecycle.LifecycleSupport
import net.bnb1.commons.logging.logger
import net.bnb1.commons.utils.Threads
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.nio.channels.ServerSocketChannel
import java.nio.channels.SocketChannel
import java.util.concurrent.Executors

private const val BUFFER_SIZE: Int = 512

/**
 * Simple non-blocking HTTP server, listening on [port] (default 8080).
 */
@Suppress("BlockingMethodInNonBlockingContext")
@OptIn(ExperimentalCoroutinesApi::class, ObsoleteCoroutinesApi::class)
class HttpServer(
    private val port: Int = 8080,
    private val connectionTimeout: Long = 2000,
    private val dispatcher: CoroutineDispatcher = newFixedThreadPoolContext(
        Runtime.getRuntime().availableProcessors(),
        "http-worker"
    )
) : LifecycleComponent {

    private val logger = logger()
    private val support = LifecycleSupport(this)
    private val executor = Executors.newSingleThreadExecutor(Threads.newFactory("http"))
    private val handlers = mutableListOf<HandlerRegistration>()

    private lateinit var selector: Selector
    private lateinit var serverSocket: ServerSocketChannel

    override val running by support

    override fun name() = "HTTP Server"

    /**
     * Starts the HTTP server.
     */
    override fun start() = support.start {
        selector = Selector.open()
        serverSocket = ServerSocketChannel.open()
        serverSocket.bind(InetSocketAddress(port))
        serverSocket.configureBlocking(false)
        serverSocket.register(selector, SelectionKey.OP_ACCEPT)

        logger.debug("Started")
        executor.submit(this::mainLoop)
    }

    /**
     * Stops the HTTP server.
     */
    override fun stop() = support.stop {
        logger.debug("Shutting down")
        serverSocket.close()
        selector.close()
        executor.shutdown()
    }

    /**
     * Registers handler for GET method.
     */
    fun get(path: String, handler: Handler) {
        get(Regex.fromLiteral(path), handler)
    }

    /**
     * Registers handler for GET method.
     */
    infix fun GET(path: String): HandlerRegistration {
        return HandlerRegistration(HttpMethod.GET, Regex.fromLiteral(path), null)
    }

    /**
     * Registers handler for GET method.
     */
    fun get(path: Regex, handler: Handler) {
        handlers.add(HandlerRegistration(HttpMethod.GET, path, handler))
    }

    /**
     * Registers handler for GET method.
     */
    infix fun GET(path: Regex): HandlerRegistration {
        return HandlerRegistration(HttpMethod.GET, path, null)
    }

    /**
     * Registers handler for POST method.
     */
    fun post(path: String, handler: Handler) {
        post(Regex.fromLiteral(path), handler)
    }

    /**
     * Registers handler for GET method.
     */
    infix fun POST(path: String): HandlerRegistration {
        return HandlerRegistration(HttpMethod.POST, Regex.fromLiteral(path), null)
    }

    /**
     * Registers handler for POST method.
     */
    fun post(path: Regex, handler: Handler) {
        handlers.add(HandlerRegistration(HttpMethod.POST, path, handler))
    }

    /**
     * Registers handler for GET method.
     */
    infix fun POST(path: Regex): HandlerRegistration {
        return HandlerRegistration(HttpMethod.POST, path, null)
    }

    private fun mainLoop() {
        while (support.isRunning()) {
            selector.select(connectionTimeout)
            val keys = selector.selectedKeys().iterator()
            while (keys.hasNext()) {
                val key = keys.next()
                keys.remove()
                handleKey(key)
            }

            for (key in selector.keys()) {
                if (key.attachment() != null) {
                    val channel = key.channel() as SocketChannel
                    val attachment = key.attachment() as Attachment
                    val elapsed = System.currentTimeMillis() - attachment.timestamp;
                    if (elapsed > connectionTimeout) {
                        logger.debug("Connection timed out")
                        channel.close()
                        key.cancel()
                    }
                }
            }
        }
    }

    private fun handleKey(key: SelectionKey) {
        if (!key.isValid) {
            return
        }
        when {
            key.isAcceptable -> accept()
            key.isReadable -> read(key)
            key.isWritable -> write(key)
        }
    }

    private fun accept() {
        val channel = serverSocket.accept()
        channel.configureBlocking(false)
        channel.register(selector, SelectionKey.OP_READ, Attachment())
    }

    private fun read(key: SelectionKey) {
        key.interestOps(0)
        GlobalScope.launch(dispatcher) {
            val channel = key.channel() as SocketChannel
            val attachment = key.attachment() as Attachment
            val parser = attachment.parser ?: HttpParser()

            val buffer = allocate(BUFFER_SIZE)
            var disconnected = false
            while (true) {
                val readBytes = channel.read(buffer)
                // Client disconnected
                if (readBytes < 0) {
                    disconnected = true
                    break
                }
                // Nothing left to read
                if (readBytes == 0) {
                    break
                }

                buffer.flip()
                parser.read(buffer)
                buffer.clear()

                if (parser.done) {
                    break
                }
            }

            when {
                disconnected -> {
                    channel.close()
                    key.cancel()
                }
                parser.done -> {
                    attachment.parser = parser
                    channel.register(selector, SelectionKey.OP_WRITE, attachment)
                }
                else -> {
                    attachment.parser = parser
                    channel.register(selector, SelectionKey.OP_READ, attachment)
                }
            }

            selector.wakeup()
        }
    }

    private fun write(key: SelectionKey) {
        key.interestOps(0)
        GlobalScope.launch(dispatcher) {
            val channel = key.channel() as SocketChannel
            val attachment = key.attachment() as Attachment
            var buffer: ByteBuffer? = attachment.writeBuffer

            if (buffer == null) {
                val response = createResponse(attachment)
                val responseLine = "HTTP/1.1 ${response.status.code} ${response.status.message}"
                logger.debug(responseLine)

                val responseString = buildString {
                    append("$responseLine\r\n")
                    append("${HttpHeader.CONNECTION.headerName}: keep-alive\r\n")
                    append("${HttpHeader.CONTENT_TYPE.headerName}: ${response.contentType.type}; charset=UTF-8\r\n")
                    append("${HttpHeader.CONTENT_LENGTH.headerName}: ${response.body?.toByteArray()?.size ?: 0}\r\n")
                    append("\r\n")
                    if (response.body != null) {
                        append(response.body)
                    }
                }

                val bytes = responseString.toByteArray()
                buffer = allocate(bytes.size).also {
                    it.put(bytes)
                    it.flip()
                }
            }

            channel.write(buffer)
            if (buffer.hasRemaining()) {
                attachment.parser = null
                attachment.writeBuffer = buffer
                channel.register(selector, SelectionKey.OP_WRITE, attachment)
            } else {
                attachment.parser = null
                attachment.writeBuffer = null
                channel.register(selector, SelectionKey.OP_READ, Attachment())
            }

            selector.wakeup()
        }
    }

    private fun allocate(capacity: Int): ByteBuffer = ByteBuffer.allocateDirect(capacity)

    private fun createResponse(attachment: Attachment): Response {
        val parserResult = attachment.parser!!.result
        if (!parserResult.valid) {
            return Response(HttpStatus.BAD_REQUEST)
        }

        val handler = handlers
            .filter { it.method == parserResult.method }
            .filter { it.path.matches(parserResult.path) }
            .map { it.handler }
            .firstOrNull()
        return when {
            handler != null -> handler.handle(Response(HttpStatus.OK))
            handlers.any { it.path.matches(parserResult.path) } -> Response(HttpStatus.METHOD_NOT_ALLOWED)
            else -> Response(HttpStatus.NOT_FOUND)
        }
    }

    inner class HandlerRegistration(val method: HttpMethod, val path: Regex, val handler: Handler? = null) {

        infix fun by(handler: Handler) {
            handlers.add(HandlerRegistration(method, path, handler))
        }
    }

}

/**
 * Handler to be invoked on HTTP requests.
 */
fun interface Handler {
    fun handle(response: Response): Response
}

/**
 * Represents HTTP response.
 */
data class Response(
    val status: HttpStatus,
    val contentType: MediaType = MediaType.TEXT_PLAIN,
    val body: String? = null
)

private class Attachment {
    val timestamp = System.currentTimeMillis()
    var parser: HttpParser? = null
    var writeBuffer: ByteBuffer? = null
}
