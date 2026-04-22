package io.github.bgmsound.kooma

class TaskContextBuilder {

    private val elements = mutableListOf<ContextElement<*>>()

    fun <T> bind(key: ScopedValue<T>, value: T) {
        elements.add(ContextElement(key, value))
    }

    fun build(): TaskContext = when {
        elements.isEmpty() -> CombinedContextElement(emptyList())
        elements.size == 1 -> elements.first()
        else -> CombinedContextElement(elements)
    }

    companion object {
        fun taskContext(init: TaskContextBuilder.() -> Unit): TaskContext {
            return TaskContextBuilder().apply(init).build()
        }
    }
}