package io.github.bgmsound.kooma

sealed interface TaskContext {

    fun flatten(): List<ContextElement<*>>

    fun <T> runWith(block: () -> T): T

    operator fun plus(other: TaskContext): TaskContext = CombinedContextElement(
        this.flatten() + other.flatten()
    )

}