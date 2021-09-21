package net.bnb1.commons

import net.bnb1.commons.utils.ByteBufferPool
import org.openjdk.jmh.annotations.*
import org.openjdk.jmh.infra.Blackhole
import java.nio.ByteBuffer
import java.util.concurrent.TimeUnit

const val DEFAULT_CAPACITY = 512

@State(Scope.Benchmark)
@Fork(1)
@Warmup(iterations = 0)
@Measurement(iterations = 10, time = 10, timeUnit = TimeUnit.MILLISECONDS)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
class KtsTestBenchmark {

    private lateinit var pool: ByteBufferPool

    @Setup
    fun init() {
        pool = ByteBufferPool(1, DEFAULT_CAPACITY)
    }

    @Benchmark
    fun allocate(blackhole: Blackhole) {
        blackhole {
            ByteBuffer.allocate(DEFAULT_CAPACITY)
        }
    }

    @Benchmark
    fun allocateDirect(blackhole: Blackhole) {
        blackhole {
            ByteBuffer.allocateDirect(DEFAULT_CAPACITY)
        }
    }

    @Benchmark
    fun allocatePool() {
        val buffer = pool.allocate(DEFAULT_CAPACITY)
        pool.release(buffer)
    }
}

inline operator fun Blackhole.invoke(consumer: () -> Any?) {
    consume(consumer.invoke())
}
