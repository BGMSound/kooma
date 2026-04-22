package io.github.bgmsound.kooma

data class ContextElement<T>(
    internal val scopedValue: ScopedValue<T>,
    internal val value: T
) : TaskContext {

    override fun flatten() = listOf(this)

    override fun <T> runWith(block: () -> T): T {
        return ScopedValue
            .where(scopedValue, value)
            .call<T, Throwable>(block)
    }
}