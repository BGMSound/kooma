package io.github.bgmsound.kooma

import java.time.Duration

interface TaskScope : AutoCloseable {
    val isActive: Boolean get() = !Thread.currentThread().isInterrupted

    fun launch(block: TaskScope.() -> Unit)

     fun <T> async(block: TaskScope.() -> T): Deferred<T>

    fun joinAll()

    fun sleep(time: Long) {
        Thread.sleep(time)
    }

    fun sleep(duration: Duration) {
        Thread.sleep(duration.toMillis())
    }

    fun yield() {
        ensureActive()
        Thread.yield()
    }

    fun ensureActive() {
        if (isActive.not()) {
            throw InterruptedException()
        }
    }
}