package io.github.bgmsound.kooma

import java.time.Duration
import java.util.concurrent.ThreadFactory

fun <R> taskScope(
    timeout: Duration? = null,
    threadFactory: ThreadFactory? = null,
    name: String? = null,
    block: TaskScope.() -> R
): R {
    DefaultTaskScope(timeout, threadFactory, name).use {
        val result = it.block()
        it.joinAll()
        return result
    }
}

fun <R> supervisorScope(
    timeout: Duration? = null,
    threadFactory: ThreadFactory? = null,
    name: String? = null,
    block: TaskScope.() -> R
): R {
    SupervisorTaskScope(timeout, threadFactory, name).use {
        val result = it.block()
        it.joinAll()
        return result
    }
}

fun <R> asyncTaskScope(
    timeout: Duration? = null,
    threadFactory: ThreadFactory? = null,
    name: String? = null,
    isSupervisor: Boolean = false,
    block: TaskScope.() -> R
): Deferred<R> {
    val deferred = Deferred<R>()
    (threadFactory ?: Thread.ofVirtual().factory()).newThread {
        try {
            val scope = if (isSupervisor) {
                SupervisorTaskScope(timeout, threadFactory, name)
            } else {
                DefaultTaskScope(timeout, threadFactory, name)
            }
            scope.use { scope ->
                val result = scope.block()
                scope.joinAll()
                deferred.complete(result)
            }
        } catch (exception: Throwable) {
            deferred.completeExceptionally(exception)
        }
    }.start()
    return deferred
}

fun <R> asyncSupervisorScope(
    timeout: Duration? = null,
    threadFactory: ThreadFactory? = null,
    name: String? = null,
    block: TaskScope.() -> R
): Deferred<R> = asyncTaskScope(timeout, threadFactory, name, true, block)

