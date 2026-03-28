package io.github.bgmsound.kooma

import java.time.Duration
import java.util.concurrent.Callable
import java.util.concurrent.StructuredTaskScope
import java.util.concurrent.ThreadFactory

abstract class AbstractTaskScope<T: TaskScope>(
    private val timeout: Duration? = null,
    private val threadFactory: ThreadFactory? = null,
    private val name: String? = null
) : TaskScope {

    @Volatile
    private var internalScope: StructuredTaskScope<Any, Void>? = null
    protected abstract val joiner: StructuredTaskScope.Joiner<Any, Void>
    private val lock = Any()

    protected abstract fun newScope(
        timeout: Duration?,
        threadFactory: ThreadFactory?,
        name: String?
    ): TaskScope

    override fun launch(block: TaskScope.() -> Unit) {
        getOrCreateInternalScope().fork(Callable {
            newScope(timeout, threadFactory, name).use { scope ->
                scope.block()
                scope.joinAll()
            }
        })
    }

    override fun <T> async(block: TaskScope.() -> T): Deferred<T> {
        val deferred = Deferred<T>()
        getOrCreateInternalScope().fork(Callable {
            try {
                newScope(timeout, threadFactory, name).use { scope ->
                    val result = scope.block()
                    scope.joinAll()
                    deferred.complete(result)
                }
            } catch (exception: Throwable) {
                deferred.completeExceptionally(exception)
                throw exception
            }
        })
        return deferred
    }

    override fun joinAll() {
        try {
            internalScope?.join()
        } catch (exception: StructuredTaskScope.FailedException) {
            throw exception.cause ?: exception
        }
    }

    override fun close() {
        internalScope?.close()
    }

    private fun getOrCreateInternalScope(): StructuredTaskScope<Any, Void> {
        return internalScope ?: synchronized(lock) {
            val newScope = StructuredTaskScope.open(joiner) { configuration ->
                configuration
                    .let { if (threadFactory != null) it.withThreadFactory(threadFactory) else it }
                    .let { if (name != null) it.withName(name) else it }
                    .let { if (timeout != null) it.withTimeout(timeout) else it }
            }
            internalScope = newScope
            return newScope
        }
    }
}