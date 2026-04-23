package io.github.bgmsound.kooma

import java.util.concurrent.CompletableFuture
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicReference

class Deferred<T> {

    private val latch = CountDownLatch(1)

    @Volatile
    private var outcome: Result<T>? = null
    private val futureRef = AtomicReference<CompletableFuture<T>?>(null)

    internal fun complete(value: T) {
        outcome = Result.success(value)
        latch.countDown()
        futureRef.get()?.complete(value)
    }

    internal fun completeExceptionally(exception: Throwable) {
        outcome = Result.failure(exception)
        latch.countDown()
        futureRef.get()?.completeExceptionally(exception)
    }

    fun await(): T {
        latch.await()
        return outcome!!.getOrThrow()
    }

    fun asCompletableFuture(): CompletableFuture<T> {
        val future = futureRef.updateAndGet { current ->
            current ?: CompletableFuture<T>()
        }!!
        outcome?.let { result ->
            result.fold(
                { future.complete(it) },
                { future.completeExceptionally(it) }
            )
        }
        return future
    }
}