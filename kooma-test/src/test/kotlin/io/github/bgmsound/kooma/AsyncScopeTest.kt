package kooma.io.github.bgmsound.kooma

import io.github.bgmsound.kooma.asyncTaskScope
import io.github.bgmsound.kooma.taskScope
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import kotlin.system.measureTimeMillis
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AsyncScopeTest {

    @Test
    fun `asyncScope should return a Deferred immediately without blocking the current thread`() {
        val creationTime = measureTimeMillis {
            // Execute a heavy scope that takes 1 second internally
            asyncTaskScope {
                val task1 = async { sleep(500); "A" }
                val task2 = async { sleep(1000); "B" }
                "${task1.await()}${task2.await()}"
            }
            // Since await() hasn't been called yet, this code should execute immediately!
        }

        // The scope internals take 1 second, but asyncScope itself should return immediately (within ~50ms)
        assertTrue(creationTime < 100, "Current thread was blocked! Elapsed time: ${creationTime}ms")
    }

    @Test
    fun `await should return the final result after all internal tasks of asyncScope are completed`() {
        val deferred = asyncTaskScope {
            val a = async { sleep(100); 10 }
            val b = async { sleep(200); 20 }
            a.await() + b.await()
        }

        // Wait on the main thread for the final result of the scope running in the background thread
        val result = deferred.await()
        assertEquals(30, result)
    }

    @Test
    fun `exceptions thrown inside asyncScope should be safely propagated to the external await caller`() {
        val deferred = asyncTaskScope {
            sleep(100)
            throw IllegalArgumentException("Background scope error!")
        }

        // The internal error should be unwrapped and thrown when calling await() on the main thread
        val exception = assertThrows<IllegalArgumentException> {
            deferred.await()
        }
        assertEquals("Background scope error!", exception.message)
    }

    @Test
    fun `asyncTaskScope should isolate exceptions from parent scope`() {
        assertDoesNotThrow {
            taskScope {
                val isolated = asyncTaskScope {
                    sleep(100)
                    throw IllegalArgumentException("isolated error")
                }

                // This normal task should complete successfully, unaffected by the isolated scope's exception
                val normal = async { sleep(100); 42 }
                assertEquals(42, normal.await())

                // The exception from the isolated scope should only be thrown when we await it, and should not affect the parent scope or sibling tasks.
                assertThrows<IllegalArgumentException> { isolated.await() }
            }
        }
    }
}