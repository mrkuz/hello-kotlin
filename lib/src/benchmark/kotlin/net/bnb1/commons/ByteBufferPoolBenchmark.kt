package net.bnb1.commons

import net.bnb1.commons.utils.ByteBufferPool
import org.openjdk.jmh.annotations.*
import org.openjdk.jmh.infra.Blackhole
import java.nio.ByteBuffer
import java.util.concurrent.TimeUnit

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
        pool = ByteBufferPool(1, 512)
    }

    @Benchmark
    fun allocate(blackhole: Blackhole) {
        blackhole {
            ByteBuffer.allocate(512)
        }
    }

    @Benchmark
    fun allocateDirect(blackhole: Blackhole) {
        blackhole {
            ByteBuffer.allocateDirect(512)
        }
    }

    @Benchmark
    fun allocatePool(blackhole: Blackhole) {
        val buffer = pool.allocate(512)
        pool.release(buffer)
    }
}

inline operator fun Blackhole.invoke(consumer: () -> Any?) {
    consume(consumer.invoke())
}
