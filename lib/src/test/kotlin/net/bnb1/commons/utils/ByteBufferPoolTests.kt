package net.bnb1.commons.utils

import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

private const val CAPACITY: Int = 256

class ByteBufferPoolTests : FunSpec({

    isolationMode = IsolationMode.InstancePerTest

    test("Create new pool") {
        val pool = ByteBufferPool(2, CAPACITY)
        pool.size shouldBe 2
        pool.free shouldBe 2
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    test("Concurrent allocation") {
        val pool = ByteBufferPool(100, CAPACITY)
        val executor = Executors.newFixedThreadPool(8)
        val latch = CountDownLatch(100)
        val buffers = Collections.newSetFromMap(ConcurrentHashMap<Int, Boolean>())
        for (i in 0 until latch.count) {
            executor.submit {
                val buffer = pool.allocate()
                buffers.add(buffer.identityHashCode())
                latch.countDown()
            }
        }

        latch.await(100, TimeUnit.MILLISECONDS)
        pool.size shouldBe 100
        buffers.size shouldBe 100
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    test("Concurrent allocation and release") {
        val pool = ByteBufferPool(1, CAPACITY)
        val executor = Executors.newFixedThreadPool(8)
        val latch = CountDownLatch(100)
        val buffers = Collections.synchronizedSet(mutableSetOf<Int>())
        for (i in 0 until latch.count) {
            executor.submit {
                val buffer = pool.allocate()
                buffers.add(buffer.identityHashCode())
                pool.release(buffer)
                latch.countDown()
            }
        }

        latch.await(100, TimeUnit.MILLISECONDS)
        pool.size shouldBe buffers.size
    }

    test("Fill buffer with zeros on release") {
        val pool = ByteBufferPool(1, CAPACITY)
        var buffer = pool.allocate(CAPACITY)
        val id = buffer.identityHashCode()
        buffer.position() shouldBe 0
        buffer.put(1)
        buffer.position() shouldBe 1
        buffer.get(0) shouldBe 1

        pool.release(buffer, zero = true)
        buffer = pool.allocate()
        buffer.identityHashCode() shouldBe id
        buffer.position() shouldBe 0
        buffer.get(0) shouldBe 0
    }

    context("Pool with start size of 2") {

        val pool = ByteBufferPool(2, CAPACITY)

        test("Allocate buffer smaller than default capacity") {
            val buffer = pool.allocate(CAPACITY / 2)
            buffer.capacity() shouldBe CAPACITY
            pool.free shouldBe 1
        }

        test("Allocate buffer with default capacity") {
            val buffer = pool.allocate(CAPACITY)
            buffer.capacity() shouldBe CAPACITY
            pool.free shouldBe 1
        }

        test("Allocate buffer bigger than default capacity") {
            val buffer = pool.allocate(CAPACITY * 2)
            buffer.capacity() shouldBe CAPACITY * 2
            pool.size shouldBe 3
            pool.free shouldBe 2
        }

        test("Grow pool") {
            pool.allocate()
            pool.allocate()
            pool.free shouldBe 0
            pool.allocate()
            pool.size shouldBe 3
            pool.free shouldBe 0
        }

        test("Release buffer") {
            val buffer = pool.allocate()
            pool.free shouldBe 1
            pool.release(buffer)
            pool.free shouldBe 2
        }
    }
})
