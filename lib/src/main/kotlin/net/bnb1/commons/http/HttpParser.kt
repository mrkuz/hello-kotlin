package net.bnb1.commons.http

import net.bnb1.commons.utils.UTF8
import java.nio.ByteBuffer

private const val CR = '\r'.code.toByte()
private const val LF = '\n'.code.toByte()

/**
 * Simple HTTP parser.
 *
 * Only supports basic POST and GET requests.
 */
class HttpParser(bufferSize: Int = 512) {

    /** Indicates if parser is done. */
    var done: Boolean = false; private set

    /** Represents the result. */
    var result = Result(); private set

    private var buffer = allocate(bufferSize)
    private var lastByte: Byte = 0

    private var linePos = 0
    private var headersDone = false
    private var contentLength = 0

    /**
     * Read bytes from [source].
     */
    fun read(source: ByteArray) {
        read(ByteBuffer.wrap(source))
    }

    /**
     * Read bytes from [source].
     */
    fun read(source: ByteBuffer) {
        if (done) return
        ensureCapacity(source)

        while (!done && source.remaining() > 0) {
            val currentByte = source.get()
            buffer.put(currentByte)

            if (!headersDone) {
                if (lastByte == CR && currentByte == LF) {
                    buffer.flip()
                    val line = UTF8.charset().decode(buffer).toString().trim()
                    processLine(line)
                    buffer.clear()
                }
            } else {
                if (buffer.position() == contentLength) {
                    val body = ByteArray(buffer.position())
                    buffer.flip()
                    buffer.get(0, body)
                    buffer.clear()

                    result = result.copy(body = body)
                    done = true
                    break
                }
            }

            lastByte = currentByte
        }
    }

    private fun processLine(line: String) {
        if (linePos == 0) {
            @Suppress("SwallowedException")
            try {
                val (method, path, version) = line.split(" ")
                result = result.copy(method = HttpMethod.valueOf(method), path = path)
            } catch (e: IllegalArgumentException) {
                done = true
                result = result.copy(valid = false)
            } catch (e: IndexOutOfBoundsException) {
                done = true
                result = result.copy(valid = false)
            }
            linePos++
            return
        }

        if (line.isBlank()) {
            done = contentLength == 0
            result = result.copy(valid = true)
            headersDone = true
            linePos++
            return
        }

        @Suppress("SwallowedException")
        try {
            val (name, value) = line.split(":", limit = 2)
            if (name.trim().equals(HttpHeader.CONTENT_LENGTH.headerName, ignoreCase = true)) {
                contentLength = value.trim().toInt()
            }
        } catch (e: IndexOutOfBoundsException) {
            done = true
            result = result.copy(valid = false)
        }
        linePos++
    }

    private fun ensureCapacity(source: ByteBuffer) {
        if (source.remaining() <= buffer.remaining()) {
            return
        }

        var newCapacity = buffer.capacity() * 2
        val used = buffer.position()
        while ((newCapacity - used) < source.remaining()) {
            newCapacity *= 2
        }
        buffer.flip()
        val expanded = allocate(newCapacity)
        expanded.order(buffer.order())
        expanded.put(buffer)
        buffer = expanded
    }

    private fun allocate(capacity: Int) = ByteBuffer.allocateDirect(capacity)

    /**
     * Represents parser result.
     */
    class Result(
        val valid: Boolean = false,
        val method: HttpMethod = HttpMethod.UNKNOWN,
        val path: String = "/",
        val body: ByteArray = ByteArray(0)
    ) {
        fun copy(
            valid: Boolean = this.valid,
            method: HttpMethod = this.method,
            path: String = this.path,
            body: ByteArray = this.body
        ) = Result(valid, method, path, body)
    }
}
