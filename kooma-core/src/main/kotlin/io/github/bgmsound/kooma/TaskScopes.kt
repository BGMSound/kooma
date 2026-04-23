package io.github.bgmsound.kooma

import java.time.Duration
import java.util.concurrent.ThreadFactory

private fun <R> runScoped(
    scope: TaskScope,
    block: TaskScope.() -> R
): R {
    return scope.use { scope ->
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

fun <R> taskScope(
    context: TaskContext? = null,
    timeout: Duration? = null,
    threadFactory: ThreadFactory? = null,
    name: String? = null,
    block: TaskScope.() -> R
): R {
    val run = { runScoped(DefaultTaskScope(timeout, threadFactory, name), block) }
    return context?.runWith(run) ?: run()
}

fun <R> supervisorTaskScope(
    context: TaskContext? = null,
    timeout: Duration? = null,
    threadFactory: ThreadFactory? = null,
    name: String? = null,
    block: TaskScope.() -> R
): R {
    val run = { runScoped(SupervisorTaskScope(timeout, threadFactory, name), block) }
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
            println("timeout = $timeout")
            if (isSupervisor) {
                supervisorTaskScope(context, timeout, threadFactory, name, block)
                    .let { deferred.complete(it) }
            } else {
                taskScope(context, timeout, threadFactory, name, block)
                    .let { deferred.complete(it) }
            }
        } catch (exception: Throwable) {
            println("Async task scope failed: ${exception::class.java.name}")
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

