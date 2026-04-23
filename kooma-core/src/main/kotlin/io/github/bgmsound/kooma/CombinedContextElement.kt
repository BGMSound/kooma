package io.github.bgmsound.kooma

data class CombinedContextElement(
    val elements: List<ContextElement<*>>
) : TaskContext {

    override fun flatten() = elements

    override fun <T> runWith(block: () -> T): T {

        return bind(elements, block)
    }

    private fun <T> bind(remaining: List<ContextElement<*>>, block: () -> T): T {
        if (remaining.isEmpty()) {
            return block()
        }
        val head = remaining.first()
        @Suppress("UNCHECKED_CAST")
        return ScopedValue.where(head.scopedValue as ScopedValue<Any?>, head.value)
            .call<T, Throwable> {
                bind(remaining.drop(1), block)
            }
    }
}