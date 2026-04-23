package io.github.bgmsound.kooma

import java.time.Duration
import java.util.concurrent.ThreadFactory

fun <R> taskScope(
    context: TaskContext? = null,
    timeout: Duration? = null,
    threadFactory: ThreadFactory? = null,
    name: String? = null,
    block: TaskScope.() -> R
): R {
    val run: () -> R = {
        DefaultTaskScope(timeout, threadFactory, name).use { scope ->
            try {
                val result = scope.block()
                scope.joinAll()
                result
            } catch (exception: InterruptedException) {
                scope.joinAll()
                throw exception.cause ?: exception
            }
        }
    }
    return context?.runWith(run) ?: run()
}

fun <R> supervisorTaskScope(
    context: TaskContext? = null,
    timeout: Duration? = null,
    threadFactory: ThreadFactory? = null,
    name: String? = null,
    block: TaskScope.() -> R
): R {
    val run: () -> R = {
        SupervisorTaskScope(timeout, threadFactory, name).use { scope ->
            try {
                val result = scope.block()
                scope.joinAll()
                result
            } catch (exception: InterruptedException) {
                scope.joinAll()
                throw exception.cause ?: exception
            }
        }
    }
    return context?.runWith(run) ?: run()
}

fun <R> asyncTaskScope(
    context: TaskContext? = null,
    timeout: Duration? = null,
    threadFactory: ThreadFactory? = null,
    name: String? = null,
    isSupervisor: Boolean = false,
    block: TaskScope.() -> R
): Deferred<R> {
    val deferred = Deferred<R>()
    (threadFactory ?: Thread.ofVirtual().factory()).newThread {
        try {
            val run: () -> Unit = {
                val scope = if (isSupervisor) {
                    SupervisorTaskScope(timeout, threadFactory, name)
                } else {
                    DefaultTaskScope(timeout, threadFactory, name)
                }
                scope.use { scope ->
                    try {
                        val result = scope.block()
                        scope.joinAll()
                        deferred.complete(result)
                    } catch (exception: InterruptedException) {
                        scope.joinAll()
                        throw exception.cause ?: exception
                    }
                }
            }
            context?.runWith(run) ?: run()
        } catch (exception: Throwable) {
            deferred.completeExceptionally(exception)
        }
    }.start()
    return deferred
}

fun <R> asyncSupervisorScope(
    context: TaskContext? = null,
    timeout: Duration? = null,
    threadFactory: ThreadFactory? = null,
    name: String? = null,
    block: TaskScope.() -> R
): Deferred<R> = asyncTaskScope(context, timeout, threadFactory, name, true, block)

