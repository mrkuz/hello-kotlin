package net.bnb1.commons.utils

import java.nio.ByteBuffer
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicInteger

/**
 * Simple [ByteBuffer] pool.
 */
class ByteBufferPool(
    startSize: Int = 0,
    private val defaultCapacity: Int = 1024
) {

    private val _size = AtomicInteger()
    private val buffers = ConcurrentLinkedQueue<ByteBuffer>()

    init {
        _size.set(startSize)
        repeat(startSize) {
            buffers.add(allocateInternal(defaultCapacity))
        }
    }

    val size: Int
        get() = _size.get()

    val free: Int
        get() = buffers.size

    /**
     * Allocate byte buffer from the pool.
     * The capacity of the buffer will be at least the passed [capacity].
     */
    fun allocate(capacity: Int = defaultCapacity): ByteBuffer {
        var result: ByteBuffer? = null
        val unusedBuffers = mutableListOf<ByteBuffer>()
        while (!buffers.isEmpty()) {
            val buffer = buffers.poll() ?: break
            if (buffer.capacity() >= capacity) {
                result = buffer
                break
            } else {
                unusedBuffers.add(buffer)
            }
        }
        buffers.addAll(unusedBuffers)
        if (result != null) {
            return result
        }
        _size.incrementAndGet()
        return allocateInternal(capacity)
    }

    /**
     * Release byte buffer back to the pool.
     */
    fun release(buffer: ByteBuffer, zero: Boolean = false) {
        buffer.clear()
        if (zero) {
            while (buffer.position() < buffer.capacity()) {
                buffer.put(0)
            }
            buffer.clear()
        }
        buffers.offer(buffer)
    }

    private fun allocateInternal(capacity: Int): ByteBuffer = ByteBuffer.allocateDirect(capacity)
}
