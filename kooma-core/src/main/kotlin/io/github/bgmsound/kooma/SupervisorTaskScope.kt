package io.github.bgmsound.kooma

import java.time.Duration
import java.util.concurrent.StructuredTaskScope
import java.util.concurrent.ThreadFactory

class SupervisorTaskScope(
    timeout: Duration? = null,
    threadFactory: ThreadFactory? = null,
    name: String? = null
) : AbstractTaskScope<SupervisorTaskScope>(timeout, threadFactory, name) {

    override val joiner: StructuredTaskScope.Joiner<Any, Void> = StructuredTaskScope.Joiner.awaitAll()

    override fun newScope(
        timeout: Duration?,
        threadFactory: ThreadFactory?,
        name: String?
    ): TaskScope {
        return DefaultTaskScope(timeout, threadFactory, name)
    }
}