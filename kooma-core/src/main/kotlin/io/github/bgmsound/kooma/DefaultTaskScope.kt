package io.github.bgmsound.kooma

import java.time.Duration
import java.util.concurrent.StructuredTaskScope
import java.util.concurrent.ThreadFactory

class DefaultTaskScope(
    timeout: Duration? = null,
    threadFactory: ThreadFactory? = null,
    name: String? = null,
) : AbstractTaskScope<DefaultTaskScope>(timeout, threadFactory, name) {

    override val joiner: StructuredTaskScope.Joiner<Any, Void> = StructuredTaskScope.Joiner.awaitAllSuccessfulOrThrow()

    override fun newScope(
        timeout: Duration?,
        threadFactory: ThreadFactory?,
        name: String?
    ): DefaultTaskScope {
        return DefaultTaskScope(timeout, threadFactory, name)
    }
}